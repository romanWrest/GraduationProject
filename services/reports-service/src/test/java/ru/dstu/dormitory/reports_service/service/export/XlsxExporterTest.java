package ru.dstu.dormitory.reports_service.service.export;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class XlsxExporterTest {

    private final XlsxExporter exporter = new XlsxExporter();

    @Test
    void produces_valid_xlsx_with_freeze_pane_and_data() throws Exception {
        TableReport report = TableReport.builder()
                .name("appliances")
                .title("Тест")
                .headers(List.of("Колонка", "Значение"))
                .rows(List.<List<Object>>of(List.of("Чайник", 1500), List.of("Лампа", 60)))
                .build();

        byte[] bytes = exporter.export(report);

        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = wb.getSheet("appliances");
            assertThat(sheet).isNotNull();
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("Колонка");
            assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("Чайник");
            assertThat(sheet.getRow(1).getCell(1).getNumericCellValue()).isEqualTo(1500);
            // Freeze pane
            assertThat(sheet.getPaneInformation()).isNotNull();
            assertThat((int) sheet.getPaneInformation().getHorizontalSplitPosition()).isEqualTo(1);
        }
    }
}
