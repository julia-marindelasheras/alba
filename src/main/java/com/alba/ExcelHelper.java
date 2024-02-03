package com.alba;

import org.apache.poi.xssf.usermodel.*;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumn;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumns;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableStyleInfo;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelHelper {

    protected static void createXlslWithSelectedTerm(Term.Dates term){
        XSSFWorkbook workbook = new XSSFWorkbook();
        String whichTerm = term.getName();
        createSheet("Total transactions " + whichTerm, workbook);
        createSheet("Not found", workbook);
        createSheet("Found", workbook);

        try (FileOutputStream outputStream = new FileOutputStream("Alba"+ whichTerm +".xlsx")) {
            workbook.write(outputStream);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    public static void excelTable() {
//        XSSFWorkbook workbook = new XSSFWorkbook();
//        createSheet("Members not Found", workbook);
//        createSheet("Bank deposits without numMember", workbook);
//        createSheet("Members Found and payments", workbook);
//
//        try (FileOutputStream outputStream = new FileOutputStream("AlbaTest.xlsx")) {
//            workbook.write(outputStream);
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    private static void createSheet(String nameSheet, XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet(nameSheet);

        XSSFTable table = sheet.createTable(null);
        CTTable cttable = table.getCTTable();

        cttable.setDisplayName("Table1");
        cttable.setId(1);
        cttable.setName("Test");
        cttable.setRef("A1:C11");
        cttable.setTotalsRowShown(false);

        CTTableStyleInfo styleInfo = cttable.addNewTableStyleInfo();
        styleInfo.setName("TableStyleMedium2");
        styleInfo.setShowColumnStripes(false);
        styleInfo.setShowRowStripes(true);

        CTTableColumns columns = cttable.addNewTableColumns();
        columns.setCount(3);
        for (int i = 1; i <= 3; i++) {
            CTTableColumn column = columns.addNewTableColumn();
            column.setId(i);
            column.setName("Column" + i);
        }

        for (int r = 0; r < 2; r++) {
            XSSFRow row = sheet.createRow(r);
            for (int c = 0; c < 3; c++) {
                XSSFCell cell = row.createCell(c);
                if (r == 0) { //first row is for column headers
                    cell.setCellValue("Column" + (c + 1)); //content **must** be here for table column names
                } else {
                    cell.setCellValue("Data R" + (r + 1) + "C" + (c + 1));
                }
            }
        }
    }
}
