package ru.dstu.dormitory.consumables_service.aspect;

/**
 * Стратегия маскирования аргументов при логировании.
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
