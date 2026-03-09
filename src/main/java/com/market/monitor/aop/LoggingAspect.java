package com.market.monitor.aop;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * AOP logging aspect — wraps controller, service, and client method calls.
 * Logs method entry (DEBUG), execution time (DEBUG), and exceptions (ERROR).
 * No business logic is touched; this is purely cross-cutting.
 */
@Aspect
@Component
@Log4j2
public class LoggingAspect {

    @Pointcut("execution(* com.market.monitor.controller..*(..))" +
              " || execution(* com.market.monitor.service..*(..))" +
              " || execution(* com.market.monitor.client..*(..))")
    public void applicationLayer() {}

    @Around("applicationLayer()")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        String cls    = pjp.getTarget().getClass().getSimpleName();
        String method = pjp.getSignature().getName();
        Object[] args = pjp.getArgs();

        log.debug("[{}#{}] >>> args: {}", cls, method, Arrays.toString(args));
        long start = System.currentTimeMillis();

        try {
            Object result = pjp.proceed();
            log.debug("[{}#{}] <<< {}ms", cls, method, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable t) {
            log.error("[{}#{}] FAILED after {}ms — {}", cls, method,
                    System.currentTimeMillis() - start, t.getMessage());
            throw t;
        }
    }
}