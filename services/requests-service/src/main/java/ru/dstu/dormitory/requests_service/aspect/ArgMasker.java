package ru.dstu.dormitory.requests_service.aspect;

/**
 * Стратегия маскирования аргументов при логировании.
 */
public interface ArgMasker {

    Object mask(String paramName, Object value);
}
