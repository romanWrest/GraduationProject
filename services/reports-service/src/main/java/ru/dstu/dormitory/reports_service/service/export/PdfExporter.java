package ru.dstu.dormitory.reports_service.service.export;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import ru.dstu.dormitory.reports_service.exception.ExportRenderException;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class PdfExporter implements ReportExporter {

    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final TemplateEngine templateEngine;

    @Override
    public ExportFormat format() {
        return ExportFormat.PDF;
    }

    @Override
    public byte[] export(TableReport report) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            String html = render(report);

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Не удалось сформировать PDF: {}", e.getMessage());
            throw new ExportRenderException("Не удалось сформировать PDF", e);
        }
    }

    private String render(TableReport report) {
        Context ctx = new Context(Locale.of("ru"));
        ctx.setVariable("report", new RenderableReport(report));
        ctx.setVariable("generatedAt", DATETIME.format(LocalDateTime.now()));
        return templateEngine.process("reports/table", ctx);
    }

    /**
     * Обёртка над TableReport, форматирующая ячейки в строки до Thymeleaf —
     * чтобы шаблон не парсил Instant/LocalDate.
     */
    public static final class RenderableReport {
        private final TableReport delegate;
        private final List<List<String>> rows;

        public RenderableReport(TableReport delegate) {
            this.delegate = delegate;
            this.rows = new ArrayList<>(delegate.getRows().size());
            for (List<Object> row : delegate.getRows()) {
                List<String> formatted = new ArrayList<>(row.size());
                for (Object v : row) {
                    formatted.add(format(v));
                }
                rows.add(formatted);
            }
        }

        public String getTitle() {
            return delegate.getTitle();
        }

        public String getSubtitle() {
            return delegate.getSubtitle();
        }

        public List<String> getHeaders() {
            return delegate.getHeaders();
        }

        public List<List<String>> getRows() {
            return rows;
        }

        public java.util.Map<String, Object> getTotals() {
            return delegate.getTotals();
        }

        private static String format(Object value) {
            if (value == null) {
                return "";
            }
            if (value instanceof Instant i) {
                return DATETIME.format(LocalDateTime.ofInstant(i, ZoneId.systemDefault()));
            }
            if (value instanceof LocalDateTime ldt) {
                return DATETIME.format(ldt);
            }
            if (value instanceof LocalDate ld) {
                return DATE.format(ld);
            }
            return value.toString();
        }
    }
}
