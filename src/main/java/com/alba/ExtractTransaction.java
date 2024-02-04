package com.alba;

import com.alba.utils.DateHelper;
import com.alba.utils.ExcelHelper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;


public class ExtractTransaction {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int option;

        do {
            System.out.println("Menu:");
            System.out.println("1. 1 Term results");
            System.out.println("2. 2 Term results");
            System.out.println("3. 3 Term results");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");

            option = scanner.nextInt();

            switch (option) {
                case 1:
                    System.out.println("You chose option 1");
                    processing(Term.Dates.FIRST_TERM);
                    break;
                case 2:
                    System.out.println("You chose option 2");
                    processing(Term.Dates.SECOND_TERM);
                    break;
                case 3:
                    System.out.println("You chose option 3");
                    processing(Term.Dates.THIRD_TERM);
                    break;
                case 4:
                    break;
                default:
                    System.out.println("Invalid option. Please choose again.");
            }

        } while (option != 4);
        scanner.close();
    }

    private static void processing(Term.Dates term) {
        XSSFWorkbook document = ExcelHelper.createXlslWithSelectedTerm(term);
        moveTransactionsToSheet(term, document);

        try (FileOutputStream outputStream = new FileOutputStream("Alba" + term.getName() + ".xlsx")) {
            document.write(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static void moveTransactionsToSheet(Term.Dates dates, XSSFWorkbook document) {
        try {
            FileInputStream file = new FileInputStream("/Users/jd185241/dev/alba/src/main/resources/Statement_Aug23_Feb24.xlsx");
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheetAt(0);

            Iterator<Row> rowIterator = sheet.iterator();
            int i = 0;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Cell transaction_date = row.getCell(0);

                try {
                    Date transactionDate = transaction_date.getDateCellValue();
                    if (transactionDate == null) {
                        continue;
                    }
                    LocalDate localDate = DateHelper.convertToLocalDateViaInstant(transactionDate);
                    if (Term.isDateInRange(dates, localDate)) {
                        ExcelHelper.copyRow(document, row,i++);
                    }
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
