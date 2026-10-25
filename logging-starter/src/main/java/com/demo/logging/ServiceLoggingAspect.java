package com.demo.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.Arrays;

@Aspect
public class ServiceLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(ServiceLoggingAspect.class);

    private final LoggingProperties properties;

    public ServiceLoggingAspect(LoggingProperties properties) {
        this.properties = properties;
    }

    @Around("execution(public * *(..)) && @within(org.springframework.stereotype.Service)")
    public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        Level level = properties.getLevel();
        log.atLevel(level).log("Method: {}, arguments: {}", method, Arrays.deepToString(joinPoint.getArgs()));

        long start = System.nanoTime();
        try {
            Object result = joinPoint.proceed();
            log.atLevel(level).log("Method: {}, result: {}", method, result);
            return result;
        } finally {
            double duration = (System.nanoTime() - start) / 1_000_000.0;
            log.atLevel(level).log("Method: {}, execution time: {} ms", method, duration);
        }
    }
}
