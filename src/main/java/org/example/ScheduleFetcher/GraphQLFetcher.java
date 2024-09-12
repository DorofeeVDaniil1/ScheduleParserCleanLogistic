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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            case STRING -> ScheduleProcessor.processSchedule(cell.getStringCellValue());
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


 class ScheduleProcessor {

     public static String processSchedule(String input) {
         input = replaceDays(input);
         // Паттерны для обработки чисел с текстом и других вариантов, например, "Ежедневно"
         Pattern pattern1 = Pattern.compile("\\d+,\\s*\\d+\\s+[А-Я]{2}");

         // Обработка чисел с текстом (например, 1 , 3 СБ)
         Matcher matcher1 = pattern1.matcher(input);
         if (matcher1.find()) {
             String label = matcher1.group(3);  // Извлекаем текст (например, СБ)
             String[] numbers = matcher1.group(0).split("\\s*,\\s*");  // Извлекаем числа

             // Строим результат, добавляя текст к каждому числу
             StringBuilder result = new StringBuilder();
             for (String number : numbers) {
                 result.append(number.split("\\s")[0]).append("-").append(label).append(",");
             }
             result.setLength(result.length() - 1);  // Убираем последнюю запятую
             return result.toString();
         }

         return input;

     }

     public static String replaceDays(String input) {
         // Массив для замены дней недели на аббревиатуры
         String[][] dayMap = {
                 {"понедельник", "ПН"},
                 {"вторник", "ВТ"},
                 {"среда", "СР"},
                 {"четверг", "ЧТ"},
                 {"пятница", "ПТ"},
                 {"суббота", "СБ"},
                 {"воскресенье", "ВС"},
                 {"Ежедневно","ПН,ВТ,СР,ЧТ,ПТ,СБ,ВС"},
                 {"ежедневно","ПН,ВТ,СР,ЧТ,ПТ,СБ,ВС"},
                 {"число",""}
         };

         // Проходим по каждому дню недели и заменяем его аббревиатурой
         for (String[] day : dayMap) {
             String fullDay = day[0];   // Полное название дня
             String abbreviation = day[1];  // Аббревиатура

             // Заменяем все вхождения дня недели (без учета регистра), включая символы перед/после слова
             input = input.replaceAll("(?i)" + fullDay, abbreviation);
         }

         return input;
     }


}