package com.demo.senla.config;

import com.demo.logging.LoggingAutoConfiguration;
import com.demo.logging.LoggingProperties;
import com.demo.logging.ServiceLoggingAspect;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.StandardEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class LoggingStarterTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(context -> {
                var propertySources = context.getEnvironment().getPropertySources();
                propertySources.remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
                propertySources.remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
            })
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
}
