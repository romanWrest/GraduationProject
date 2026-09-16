package ru.dstu.dormitory.notifications_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateInputException;
import ru.dstu.dormitory.notifications_service.aspect.annotation.LogMethod;
import ru.dstu.dormitory.notifications_service.exception.TemplateNotFoundException;
import ru.dstu.dormitory.notifications_service.util.LogPatterns;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Обёртка над Thymeleaf для рендеринга email-шаблонов.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateService {

    private static final String TEMPLATE_PREFIX = "email/";

    private final TemplateEngine templateEngine;

    /**
     * Отрендерить шаблон по имени (без расширения, относительно {@code templates/email/}).
     */
    @LogMethod(value = "Рендеринг шаблона", logArgs = {"name"})
    public String render(String name, Map<String, Object> variables) {
        long start = System.currentTimeMillis();
        Context ctx = new Context();
        ctx.setLocale(java.util.Locale.forLanguageTag("ru"));
        ctx.setVariable("charset", StandardCharsets.UTF_8.name());
        if (variables != null) {
            variables.forEach(ctx::setVariable);
        }
        try {
            String result = templateEngine.process(TEMPLATE_PREFIX + name, ctx);
            log.debug(LogPatterns.TEMPLATE_RENDERED, name, System.currentTimeMillis() - start);
            return result;
        } catch (TemplateInputException ex) {
            log.error(LogPatterns.TEMPLATE_NOT_FOUND, name);
            throw new TemplateNotFoundException(name, ex);
        }
    }
}
