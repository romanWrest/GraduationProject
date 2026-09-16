package ru.dstu.dormitory.api_gateway.aspect;

/**
 * Стратегия маскирования аргументов при логировании.
 */
public interface ArgMasker {

    Object mask(String paramName, Object value);
}
