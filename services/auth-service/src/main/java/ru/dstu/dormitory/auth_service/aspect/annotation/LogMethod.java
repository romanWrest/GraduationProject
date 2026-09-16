package ru.dstu.dormitory.auth_service.aspect.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для автоматического логирования методов через AOP.
 *
 * <p>Пример использования:</p>
 * <pre>
 * {@code @LogMethod(value = "Регистрация", logArgs = {"request"}, maskArgs = {"password"}, logResult = true)}
 * public AuthResponse register(AuthRequest request) { ... }
 * </pre>
 *
 * @see ru.dstu.dormitory.auth_service.aspect.LoggingAspect
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogMethod {

    /**
     * Пользовательское сообщение, добавляемое к логу при входе в метод.
     */
    String value() default "";

    /**
     * Имена параметров, которые нужно логировать.
     * Если пусто — логируются все параметры (кроме замаскированных).
     */
    String[] logArgs() default {};

    /**
     * Имена параметров, которые нужно маскировать при логировании.
     * Маскированные параметры логируются, но их значения скрываются.
     */
    String[] maskArgs() default {};

    /**
     * Логировать ли результат выполнения метода.
     * По умолчанию {@code false} — результат не попадёт в лог.
     */
    boolean logResult() default false;
}