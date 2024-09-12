package org.example.ScheduleFetcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

import static org.example.BearToken.BearTocken.getIdToken;

// Class to handle file operations
class ExcelHandler {
    public static final String INPUT_FILE_PATH = "src/main/resources/schedule.xlsx";  // Excel file path

    public static Workbook loadWorkbook() throws IOException {
        FileInputStream fis = new FileInputStream(INPUT_FILE_PATH);
        return new XSSFWorkbook(fis);
    }

    public static void saveWorkbook(Workbook workbook) throws IOException {
        FileOutputStream fos = new FileOutputStream(INPUT_FILE_PATH);
        workbook.write(fos);
        fos.close();
    }

    public static void clearSheet(Sheet sheet) {
        for (int i = sheet.getLastRowNum(); i >= 0; i--) {
            Row row = sheet.getRow(i);
            if (row != null) sheet.removeRow(row);
        }
    }

    public static Sheet getOrCreateSheet(Workbook workbook, String sheetName) {
        Sheet sheet = workbook.getSheet(sheetName);
        return (sheet != null) ? sheet : workbook.createSheet(sheetName);
    }

    public static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING -> {
                String string_value =cell.getStringCellValue();  // Если тип ячейки строка, возвращаем строковое значение
                if (string_value.equals("Ежедневно")){
                    yield "ПН,ВТ,СР,ЧТ,ПТ,СБ,ВС";
                }
                else yield string_value;

            }
            case NUMERIC -> {
                double numericValue = cell.getNumericCellValue();
                // Проверяем, является ли число целым
                if (numericValue == (long) numericValue) {
                    yield String.valueOf((long) numericValue);  // Преобразуем в строку без дробной части
                } else {
                    yield String.valueOf(numericValue);  // Преобразуем в строку с дробной частью
                }
            }
            default -> "";  // Для всех других типов возвращаем пустую строку
        };
    }
}

// Class to handle writing correct and error data to sheets
class DataWriter {

    public static void writeDataToSheet(Workbook workbook, String lk_code, String schedule, String volume, String amount, boolean isError, String latitude, String longitude) {
        Sheet sheet = isError ? ExcelHandler.getOrCreateSheet(workbook, "Ошибки в расписании")
                : ExcelHandler.getOrCreateSheet(workbook, "Корректные данные");

        int nextRowIndex = sheet.getLastRowNum() + 1;
        Row row = sheet.createRow(nextRowIndex);

        if (isError) {
            addErrorRow(row, lk_code, schedule);
        } else {
            addCorrectRow(row, lk_code, schedule, amount, volume);
        }
    }

    private static void addErrorRow(Row row, String area_id, String schedule) {
        row.createCell(0).setCellValue("Не соответствует шаблону");
        row.createCell(1).setCellValue(area_id);
        row.createCell(2).setCellValue(schedule);
    }

    private static void addCorrectRow(Row row, String lk_code, String schedule, String amount, String volume) {
        row.createCell(0).setCellValue(lk_code);
        row.createCell(1).setCellValue(schedule);
        row.createCell(2).setCellValue(String.format(
                "select update_schedule(lk_code_:='%s',amount_:=%s,volume_:=%s,schedule_id_:='%s');",
                lk_code, amount, volume, schedule));
    }
}

// Class to handle GraphQL operations
public class GraphQLFetcher {

    private static final String URL = "https://amur.mytko.ru/app/graphql";

    private final HttpClient client;
    private final ObjectMapper objectMapper;
    private final String authToken;

    public GraphQLFetcher(String authToken) {
        this.client = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.authToken = authToken;
    }

    public String sendRequest(String schedule) throws IOException, InterruptedException {
        String graphqlQuery = objectMapper.writeValueAsString(new GraphQLRequest(
                "query createOrGetFromScheduleNotation($request: String) {" +
                        " createOrGetFromScheduleNotation(request: $request) {" +
                        "   id, name, records { id, timeFrom, timeTo, interval { id, name, orderInMonth, value, type {id} } }, startDate" +
                        " }}",
                new GraphQLVariables(schedule)
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .header("Authorization", "Bearer " + authToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(graphqlQuery))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return parseResponse(response.body());
    }

    private String parseResponse(String responseBody) throws IOException {
        JsonNode rootNode = objectMapper.readTree(responseBody);
        JsonNode dataNode = rootNode.path("data").path("createOrGetFromScheduleNotation");
        return dataNode.path("id").asText();
    }
}

// GraphQL request and variables classes
class GraphQLRequest {
    public String query;
    public GraphQLVariables variables;

    public GraphQLRequest(String query, GraphQLVariables variables) {
        this.query = query;
        this.variables = variables;
    }
}

class GraphQLVariables {
    public String request;

    public GraphQLVariables(String request) {
        this.request = request;
    }
}
