package ru.dstu.dormitory.api_gateway.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.dstu.dormitory.api_gateway.aspect.annotation.LogMethod;
import ru.dstu.dormitory.api_gateway.util.LogUtil;

import static ru.dstu.dormitory.api_gateway.aspect.annotation.LogMessages.*;

/**
 * Аспект автоматического логирования методов, помеченных {@link LogMethod}.
 */
@Slf4j
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class LoggingAspect {

    private final ArgMasker argMasker;

    @Around("@annotation(ru.dstu.dormitory.api_gateway.aspect.annotation.LogMethod)")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature sig = (MethodSignature) joinPoint.getSignature();
        String methodName = sig.toShortString();
        LogMethod logMethod = sig.getMethod().getAnnotation(LogMethod.class);

        String message = logMethod.value().isEmpty() ? null : logMethod.value();
        String prettyArgs = buildArgsIfNeeded(joinPoint, sig, logMethod);

        logEntry(methodName, message, prettyArgs);

        long start = System.currentTimeMillis();
        Object result = execute(joinPoint, methodName);
        long elapsed = System.currentTimeMillis() - start;

        logExit(methodName, elapsed, result, logMethod.logResult());

        return result;
    }

    private String buildArgsIfNeeded(ProceedingJoinPoint joinPoint,
                                     MethodSignature sig,
                                     LogMethod logMethod) {
        boolean shouldLog = logMethod.logArgs().length > 0 || logMethod.maskArgs().length > 0;
        if (!shouldLog || joinPoint.getArgs().length == 0) {
            return null;
        }

        String[] paramNames = ((CodeSignature) joinPoint.getSignature()).getParameterNames();
        return LogUtil.buildPrettyArgs(joinPoint.getArgs(), paramNames, logMethod, argMasker);
    }

    private void logEntry(String methodName, String message, String prettyArgs) {
        if (message != null && prettyArgs != null) {
            log.info(METHOD_START_NAME_MESSAGE_ARGUMENTS, methodName, message, prettyArgs);
        } else if (message != null) {
            log.info(METHOD_START_NAME_MESSAGE, methodName, message);
        } else if (prettyArgs != null) {
            log.info(METHOD_START_NAME_ARGUMENTS, methodName, prettyArgs);
        } else {
            log.info(METHOD_START_NAME, methodName);
        }
    }

    private void logExit(String methodName, long elapsed, Object result, boolean logResult) {
        if (logResult && result != null) {
            log.info(METHOD_END_WITH_RESULT, methodName, elapsed, LogUtil.prettifyPOJO(result));
        } else {
            log.info(METHOD_END, methodName, elapsed);
        }
    }

    private Object execute(ProceedingJoinPoint joinPoint, String methodName) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            log.error(METHOD_ERROR, methodName, e.getMessage());
            throw e;
        }
    }
}
