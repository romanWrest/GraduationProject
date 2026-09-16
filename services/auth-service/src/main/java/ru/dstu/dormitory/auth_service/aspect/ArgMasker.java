package ru.dstu.dormitory.auth_service.aspect;

/**
 * Стратегия маскирования аргументов при логировании.
 * Реализация может использовать разные правила маскирования
 * в зависимости от имени параметра и типа значения.
 *
 */
public interface ArgMasker {

    /**
     * Маскирование значения аргумента.
     *
     * @param paramName имя параметра метода
     * @param value     значение аргумента
     * @return замаскированное значение
     */
    Object mask(String paramName, Object value);
}