package org.example.ScheduleFetcher;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.example.Configuration.Config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Scanner;

import static org.example.BearToken.BearTocken.getIdToken;

// Main class to orchestrate the process
public class MainApp {


    public static void main(String[] args) throws IOException, InterruptedException {


        Scanner sc = new Scanner(System.in);
        System.out.println("Введите домен для сайта");
        String domain = sc.next();
        System.out.println("Введите Логин пользователя");
        String username = sc.next();
        System.out.println("Введите Пароль пользователя");
        String password = sc.next();
        System.out.println("Введите ID расписания  по заявке");
        String id_po_zayavke = sc.next();
        //Создаем объект config для подключения к базе
        Config config = new Config(domain,username,password,id_po_zayavke);



        System.out.println("Введите путь для сохранения файла (например: C:/Users/ИмяПользователя/Documents):");
        String filePath = (sc.next()+"\\"+"schedule.xlsx");
        ExcelFileCreatorWithInput.createExselFile(filePath);



        System.out.println("Вставьте расписание в лист 1 lk_code\tamount\tvolume\tlatitude\tlongitude\t?column?\tview_\n");

        System.out.println("Начать запуск Y/N");
        String trigger = sc.next();


        //Получаем JWT токен. Нужно вставить Логин и пароль в классе BearToken
        String authToken = getIdToken(config);

        while (true){
            if (trigger.equals("Y")){


            GraphQLFetcher graphQLFetcher = new GraphQLFetcher(authToken,config);

            //Открываем Exsel
            try (Workbook workbook = ExcelHandler.loadWorkbook(filePath)) {


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

                    boolean isError = Objects.equals(id, config.getPO_ZAYAVKE()) && !Objects.equals(schedule.toLowerCase(), "по заявке");
                    DataWriter.writeDataToSheet(workbook, lk_code, isError ? schedule : id, volume, amount, isError, latitude, longitude);
                    System.out.println(lk_code+"$"+id+"%"+schedule);

                }

                // Save the workbook after processing all rows
                ExcelHandler.saveWorkbook(workbook,filePath);
            }

            System.out.println("Повторить запуск? Y/N");
            trigger = sc.next();
            }
            else break;

        }

    }
}
