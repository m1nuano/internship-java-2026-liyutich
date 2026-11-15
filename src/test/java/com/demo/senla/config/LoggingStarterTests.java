package com.demo.senla.config;

import com.demo.logging.LoggingAutoConfiguration;
import com.demo.logging.LoggingProperties;
import com.demo.logging.ServiceLoggingAspect;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class LoggingStarterTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LoggingAutoConfiguration.class));

    @Test
    void createsAspectByDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ServiceLoggingAspect.class);
            assertThat(context.getBean(LoggingProperties.class).getLevel()).isEqualTo(Level.INFO);
        });
    }

    @Test
    void doesNotCreateAspectWhenDisabled() {
        contextRunner.withPropertyValues("service-logging.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(ServiceLoggingAspect.class));
    }

    @Test
    void readsConfiguredLogLevel() {
        contextRunner.withPropertyValues("service-logging.level=DEBUG")
                .run(context -> assertThat(context.getBean(LoggingProperties.class).getLevel())
                        .isEqualTo(Level.DEBUG));
    }

    @Test
    void keepsUserDefinedAspect() {
        ServiceLoggingAspect customAspect = new ServiceLoggingAspect(new LoggingProperties());
        contextRunner.withBean(ServiceLoggingAspect.class, () -> customAspect)
                .run(context -> {
                    assertThat(context).hasSingleBean(ServiceLoggingAspect.class);
                    assertThat(context.getBean(ServiceLoggingAspect.class)).isSameAs(customAspect);
                });
    }
}
