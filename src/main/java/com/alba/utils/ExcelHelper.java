package com.alba.utils;

import com.alba.Term;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelHelper {

    public static XSSFWorkbook createXlslWithSelectedTerm(Term.Dates term) {
        XSSFWorkbook workbook = new XSSFWorkbook();

        workbook.createSheet("Total transactions " + term.getName());
        workbook.createSheet("Not found");
        workbook.createSheet("Found");

        return workbook;
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

    public static void copyRow(XSSFWorkbook workbook, Row sourceRow, int destinationRowNum) {

        Row newRow = workbook.getSheetAt(0).createRow(destinationRowNum);
        for (int i = 0; i < sourceRow.getLastCellNum(); i++) {
            // Grab a copy of the old/new cell
            Cell oldCell = sourceRow.getCell(i);
            Cell newCell = newRow.createCell(i);

            // If the old cell is null jump to next cell
            if (oldCell == null) {
                continue;
            }

            // Copy style from old cell and apply to new cell
            XSSFCellStyle newCellStyle = workbook.createCellStyle();
            newCellStyle.cloneStyleFrom(oldCell.getCellStyle());

            newCell.setCellStyle(newCellStyle);

            // If there is a cell comment, copy
            if (oldCell.getCellComment() != null) {
                newCell.setCellComment(oldCell.getCellComment());
            }

            // If there is a cell hyperlink, copy
            if (oldCell.getHyperlink() != null) {
                newCell.setHyperlink(oldCell.getHyperlink());
            }

            // Set the cell data type
            newCell.setCellType(oldCell.getCellType());

            // Set the cell data value
            switch (oldCell.getCellType()) {
                case BLANK:
                    newCell.setCellValue(oldCell.getStringCellValue());
                    break;
                case BOOLEAN:
                    newCell.setCellValue(oldCell.getBooleanCellValue());
                    break;
                case ERROR:
                    newCell.setCellErrorValue(oldCell.getErrorCellValue());
                    break;
                case FORMULA:
                    newCell.setCellFormula(oldCell.getCellFormula());
                    break;
                case NUMERIC:
                    newCell.setCellValue(oldCell.getNumericCellValue());
                    break;
                case STRING:
                    newCell.setCellValue(oldCell.getRichStringCellValue());
                    break;
            }
        }
    }

//    private static void createSheet(String nameSheet, XSSFWorkbook workbook) {
//        XSSFSheet sheet = workbook.createSheet(nameSheet);
//
//        XSSFTable table = sheet.createTable(null);
//        CTTable cttable = table.getCTTable();
//
//        cttable.setDisplayName("Table1");
//        cttable.setId(1);
//        cttable.setName("Test");
//        cttable.setRef("A1:C11");
//        cttable.setTotalsRowShown(false);
//
//        CTTableStyleInfo styleInfo = cttable.addNewTableStyleInfo();
//        styleInfo.setName("TableStyleMedium2");
//        styleInfo.setShowColumnStripes(false);
//        styleInfo.setShowRowStripes(true);
//
//        CTTableColumns columns = cttable.addNewTableColumns();
//        columns.setCount(3);
//        for (int i = 1; i <= 3; i++) {
//            CTTableColumn column = columns.addNewTableColumn();
//            column.setId(i);
//            column.setName("Column" + i);
//        }
//
//        for (int r = 0; r < 2; r++) {
//            XSSFRow row = sheet.createRow(r);
//            for (int c = 0; c < 3; c++) {
//                XSSFCell cell = row.createCell(c);
//                if (r == 0) { //first row is for column headers
//                    cell.setCellValue("Column" + (c + 1)); //content **must** be here for table column names
//                } else {
//                    cell.setCellValue("Data R" + (r + 1) + "C" + (c + 1));
//                }
//            }
//        }
//    }
}
