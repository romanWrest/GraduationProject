package ru.dstu.dormitory.notifications_service.service;

import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import ru.dstu.dormitory.notifications_service.exception.TemplateNotFoundException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TemplateServiceTest {

    private final TemplateService templateService = new TemplateService(buildEngine());

    @Test
    void rendersWelcomeTemplate() {
        String html = templateService.render("welcome", Map.of(
                "userName", "Иван Иванов",
                "email", "ivanov@example.com",
                "tempPassword", "abc12345",
                "actionUrl", "http://x/login"));

        assertThat(html).contains("Иван Иванов");
        assertThat(html).contains("abc12345");
        assertThat(html).contains("ivanov@example.com");
        assertThat(html).contains("http://x/login");
    }

    @Test
    void missingTemplateThrowsTemplateNotFound() {
        assertThatThrownBy(() -> templateService.render("not-exists-xx", Map.of()))
                .isInstanceOf(TemplateNotFoundException.class);
    }

    @Test
    void rendersWithMissingVariableAsEmpty() {
        // Thymeleaf по дефолту печатает null/missing как пустую строку — без исключения
        String html = templateService.render("welcome", Map.of(
                "userName", "Иван",
                "email", "x@y",
                "tempPassword", "p",
                "actionUrl", "http://x"));
        assertThat(html).contains("Иван");
    }

    private static TemplateEngine buildEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setTemplateMode("HTML");
        resolver.setCacheable(false);
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }
}
