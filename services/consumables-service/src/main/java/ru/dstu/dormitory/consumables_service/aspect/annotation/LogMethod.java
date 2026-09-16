package ru.dstu.dormitory.consumables_service.aspect.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для автоматического логирования методов через AOP.
 *
 * @see ru.dstu.dormitory.consumables_service.aspect.LoggingAspect
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
     */
    String[] maskArgs() default {};

    /**
     * Логировать ли результат выполнения метода.
     */
    boolean logResult() default false;
}
