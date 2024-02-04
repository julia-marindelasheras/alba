package com.alba.utils;

import com.alba.Term;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.*;

public class ExcelHelper {

    public static XSSFWorkbook createXlslWithSelectedTerm(Term.Dates term) {
        XSSFWorkbook workbook = new XSSFWorkbook();

        workbook.createSheet("Total transactions " + term.getName());
        workbook.createSheet("Not found");
        workbook.createSheet("Found");

        return workbook;
    }

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

    public static void copyDataInASheet(XSSFSheet sheet, int rownum, String extractBankDescription, Double paid) {
        XSSFRow row = sheet.createRow(rownum);
        XSSFCell description = row.createCell(0);
        description.setCellValue(extractBankDescription);
        XSSFCell moneyPaid = row.createCell(1);
        moneyPaid.setCellValue(paid);
    }

    public static void copyDataInASheet(XSSFSheet sheet, int rownum, String numSocio, String parentName, Double amount, Double paid) {
        XSSFRow row = sheet.createRow(rownum);
        XSSFCell numSocioCell = row.createCell(0);
        numSocioCell.setCellValue(numSocio);
        XSSFCell parentNameCell = row.createCell(1);
        parentNameCell.setCellValue(parentName);
        XSSFCell moneyToPaidCell = row.createCell(2);
        moneyToPaidCell.setCellValue(amount);
        XSSFCell moneyPaidCell = row.createCell(3);
        moneyPaidCell.setCellValue(paid);
    }
}
