package ru.dstu.dormitory.reports_service.service.export;

import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.dstu.dormitory.reports_service.exception.ExportRenderException;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
public class CsvExporter implements ReportExporter {

    private static final byte[] UTF8_BOM = new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public ExportFormat format() {
        return ExportFormat.CSV;
    }

    @Override
    public byte[] export(TableReport report) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            out.write(UTF8_BOM);
            try (Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
                 CSVWriter csv = new CSVWriter(writer,
                         ',',
                         ICSVWriter.DEFAULT_QUOTE_CHARACTER,
                         ICSVWriter.DEFAULT_ESCAPE_CHARACTER,
                         ICSVWriter.DEFAULT_LINE_END)) {

                csv.writeNext(report.getHeaders().toArray(String[]::new));
                for (List<Object> row : report.getRows()) {
                    String[] line = new String[row.size()];
                    for (int i = 0; i < row.size(); i++) {
                        line[i] = format(row.get(i));
                    }
                    csv.writeNext(line);
                }
            }
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Не удалось сформировать CSV: {}", e.getMessage());
            throw new ExportRenderException("Не удалось сформировать CSV", e);
        }
    }

    private String format(Object value) {
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
