package ru.dstu.dormitory.reports_service.service.export;

import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PdfExporterTest {

    /**
     * Используем {@link SpringTemplateEngine} (как в продакшене), потому что
     * Thymeleaf 3.1 не подтягивает OGNL транзитивно — стандартный движок
     * без Spring требует ognl на classpath.
     */
    private static TemplateEngine templateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }

    @Test
    void produces_non_empty_pdf_with_magic_bytes() {
        PdfExporter exporter = new PdfExporter(templateEngine());
        TableReport report = TableReport.builder()
                .name("test")
                .title("Тест")
                .subtitle("Период")
                .headers(List.of("A", "B"))
                .rows(List.<List<Object>>of(List.of("первый", "второй")))
                .build();

        byte[] bytes = exporter.export(report);

        assertThat(bytes).isNotEmpty();
        assertThat(bytes.length).isGreaterThan(100);
        // %PDF magic
        assertThat((char) bytes[0]).isEqualTo('%');
        assertThat((char) bytes[1]).isEqualTo('P');
        assertThat((char) bytes[2]).isEqualTo('D');
        assertThat((char) bytes[3]).isEqualTo('F');
    }
}
