package ru.dstu.dormitory.reports_service.service.export;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import ru.dstu.dormitory.reports_service.exception.ExportRenderException;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class XlsxExporter implements ReportExporter {

    @Override
    public ExportFormat format() {
        return ExportFormat.XLSX;
    }

    @Override
    public byte[] export(TableReport report) {
        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet(report.getName() == null ? "Report" : report.getName());
            CreationHelper helper = wb.getCreationHelper();

            CellStyle headerStyle = headerStyle(wb);
            CellStyle numberStyle = wb.createCellStyle();
            numberStyle.setDataFormat(helper.createDataFormat().getFormat("#,##0"));
            CellStyle dateStyle = wb.createCellStyle();
            dateStyle.setDataFormat(helper.createDataFormat().getFormat("DD.MM.YYYY"));
            CellStyle dateTimeStyle = wb.createCellStyle();
            dateTimeStyle.setDataFormat(helper.createDataFormat().getFormat("DD.MM.YYYY HH:MM"));

            // Заголовки
            Row header = sheet.createRow(0);
            for (int i = 0; i < report.getHeaders().size(); i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(report.getHeaders().get(i));
                cell.setCellStyle(headerStyle);
            }

            // Данные
            int rowIdx = 1;
            for (List<Object> row : report.getRows()) {
                Row r = sheet.createRow(rowIdx++);
                for (int i = 0; i < row.size(); i++) {
                    Cell cell = r.createCell(i);
                    Object v = row.get(i);
                    if (v == null) {
                        continue;
                    }
                    if (v instanceof Number n) {
                        cell.setCellValue(n.doubleValue());
                        cell.setCellStyle(numberStyle);
                    } else if (v instanceof LocalDate ld) {
                        cell.setCellValue(Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                        cell.setCellStyle(dateStyle);
                    } else if (v instanceof Instant inst) {
                        cell.setCellValue(Date.from(inst));
                        cell.setCellStyle(dateTimeStyle);
                    } else if (v instanceof LocalDateTime ldt) {
                        cell.setCellValue(Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()));
                        cell.setCellStyle(dateTimeStyle);
                    } else if (v instanceof Boolean b) {
                        cell.setCellValue(b);
                    } else {
                        cell.setCellValue(v.toString());
                    }
                }
            }

            // Auto-size + freeze pane
            for (int i = 0; i < report.getHeaders().size(); i++) {
                sheet.autoSizeColumn(i);
            }
            sheet.createFreezePane(0, 1);

            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Не удалось сформировать XLSX: {}", e.getMessage());
            throw new ExportRenderException("Не удалось сформировать XLSX", e);
        }
    }

    private CellStyle headerStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }
}
