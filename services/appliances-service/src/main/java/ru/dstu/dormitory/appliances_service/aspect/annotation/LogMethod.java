package ru.dstu.dormitory.appliances_service.aspect.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для автоматического логирования методов через AOP.
 *
 * @see ru.dstu.dormitory.appliances_service.aspect.LoggingAspect
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogMethod {

    String value() default "";

    String[] logArgs() default {};

    String[] maskArgs() default {};

    boolean logResult() default false;
}
