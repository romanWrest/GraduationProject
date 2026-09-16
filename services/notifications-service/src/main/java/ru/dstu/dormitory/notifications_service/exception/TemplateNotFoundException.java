package ru.dstu.dormitory.notifications_service.exception;

public class TemplateNotFoundException extends RuntimeException {

    public TemplateNotFoundException(String name) {
        super("Шаблон письма не найден: %s".formatted(name));
    }

    public TemplateNotFoundException(String name, Throwable cause) {
        super("Шаблон письма не найден: %s".formatted(name), cause);
    }
}
