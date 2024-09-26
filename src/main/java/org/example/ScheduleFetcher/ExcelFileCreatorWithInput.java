package org.example.ScheduleFetcher;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class ExcelFileCreatorWithInput {

    public static void createExselFile(String fullPath) {

        // Создаем новый рабочий лист
        Workbook workbook = new XSSFWorkbook();

        // Создаем лист с именем "Данные"
        Sheet sheet = workbook.createSheet("Данные");

        // Создаем строку
        Row row = sheet.createRow(0);

        // Создаем ячейки и добавляем данные в первую строку
        Cell cell1 = row.createCell(0);
        cell1.setCellValue("lk_code");

        Cell cell2 = row.createCell(1);
        cell2.setCellValue("amount");

        Cell cell3 = row.createCell(2);
        cell3.setCellValue("volume");

        Cell cell4 = row.createCell(3);
        cell4.setCellValue("latitude");

        Cell cell5 = row.createCell(4);
        cell5.setCellValue("longitude");

        Cell cell6 = row.createCell(5);
        cell6.setCellValue("schedule");

        Cell cell7 = row.createCell(6);
        cell7.setCellValue("address");

        // Добавляем тестовую строку с данными
        Row row2 = sheet.createRow(1);
        row2.createCell(0).setCellValue("38113729");
        row2.createCell(1).setCellValue(4);
        row2.createCell(2).setCellValue("0,75");
        row2.createCell(3).setCellValue("54,50705");
        row2.createCell(4).setCellValue("100,77592");
        row2.createCell(5).setCellValue("ПН");
        row2.createCell(6).setCellValue("с. Азей, Российская, 43А");

        Row row3 = sheet.createRow(2);
        row3.createCell(0).setCellValue("38113731");
        row3.createCell(1).setCellValue(1);
        row3.createCell(2).setCellValue("8");
        row3.createCell(3).setCellValue("54,51243");
        row3.createCell(4).setCellValue("100,76598");
        row3.createCell(5).setCellValue("Каждый вторник месяца");
        row3.createCell(6).setCellValue("с. Азей, Российская, 1А");

        // Создаем файл и записываем данные
        try (FileOutputStream fileOut = new FileOutputStream(new File(fullPath))) {
            workbook.write(fileOut);
            System.out.println("Файл Excel успешно создан: " + fullPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Закрываем workbook
        try {
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
