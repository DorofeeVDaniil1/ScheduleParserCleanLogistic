package org.example.ScheduleFetcher;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.util.Objects;

import static org.example.BearToken.BearTocken.getIdToken;

// Main class to orchestrate the process
public class MainApp {

    private static final String PO_ZAYAVKE = "0f7490b1-0062-408f-b430-ea92580c451f";

    public static void main(String[] args) throws IOException, InterruptedException {
        String authToken = getIdToken();
        GraphQLFetcher graphQLFetcher = new GraphQLFetcher(authToken);

        try (Workbook workbook = ExcelHandler.loadWorkbook()) {
            Sheet sheet = workbook.getSheetAt(0);
            Sheet wrongSheet = ExcelHandler.getOrCreateSheet(workbook, "Ошибки в расписании");
            Sheet correctSheet = ExcelHandler.getOrCreateSheet(workbook, "Корректные данные");

            ExcelHandler.clearSheet(wrongSheet);
            ExcelHandler.clearSheet(correctSheet);
            System.out.println("lk_code $ schedule_id");
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                String lk_code = ExcelHandler.getCellValueAsString(row.getCell(0));  // Col A
                String amount = ExcelHandler.getCellValueAsString(row.getCell(1));   // Col D
                String volume = ExcelHandler.getCellValueAsString(row.getCell(2));   // Col E
                String latitude = ExcelHandler.getCellValueAsString(row.getCell(3)); // Col B
                String longitude = ExcelHandler.getCellValueAsString(row.getCell(4));// Col C
                String schedule = ExcelHandler.getCellValueAsString(row.getCell(5)); // Col F

                // Fetch GraphQL response
                String id = graphQLFetcher.sendRequest(schedule);

                boolean isError = Objects.equals(id, PO_ZAYAVKE) && !Objects.equals(schedule.toLowerCase(), "по заявке");
                DataWriter.writeDataToSheet(workbook, lk_code, isError ? schedule : id, volume, amount, isError, latitude, longitude);
                System.out.println(lk_code+"$"+id+"%"+schedule);

            }

            // Save the workbook after processing all rows
            ExcelHandler.saveWorkbook(workbook);
        }
    }
}
