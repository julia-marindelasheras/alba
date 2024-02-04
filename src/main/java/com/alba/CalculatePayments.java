package com.alba;

import com.alba.utils.ExcelHelper;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CalculatePayments {

    private final static Map<String, Double> map = new HashMap<>();
    private static Map<String, Socio>  mapSociosQuotas = new HashMap<>();
    private final static Map<String, Double> notFoundSocios = new HashMap<>();

    private static double quota = 63.0;

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

            System.out.println("Please enter quota:");
            String input = scanner.nextLine();  // This will read a full line of text
            quota = Double.parseDouble(input);  // This will read a full line of text

            switch (option) {
                case 1:
                    processingPayments(Term.Dates.FIRST_TERM);
                    break;
                case 2:
                    processingPayments(Term.Dates.SECOND_TERM);
                    break;
                case 3:
                    processingPayments(Term.Dates.THIRD_TERM);
                    break;
                case 4:
                    break;
                default:
                    System.out.println("Invalid option. Please choose again.");
            }

        } while (option != 4);
        scanner.close();
    }

    public static void processingPayments(Term.Dates term) {

        try {
            ZipSecureFile.setMinInflateRatio(0);

            mapSociosQuotas = getMapSociosQuotas();

            FileInputStream file = new FileInputStream("/Users/jd185241/dev/alba/Alba" + term.getName() + ".xlsx");
            XSSFWorkbook workbook = new XSSFWorkbook(file);

            XSSFSheet sheet = workbook.getSheetAt(0);

            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Cell transaction_description = row.getCell(4);
                Cell credit_amount = row.getCell(6);

                if (credit_amount == null) {
                    continue;
                }

                try {
                    credit_amount.getNumericCellValue();
                } catch (IllegalStateException e) {
                    continue;
                }

                if (credit_amount.getNumericCellValue() == 0.0) {
                    continue;
                }

                String numSocio = findNumSocio(transaction_description);

                if (numSocio == null) {
                    numSocio = findNumSocioWithHyphen(transaction_description);
                }

                if (numSocio == null) {
                    numSocio = findWithAnyFourDigitsInsideString(transaction_description);
                }

                if (numSocio != null) {
                    if (mapSociosQuotas.containsKey(numSocio)) {
                        double total = credit_amount.getNumericCellValue() + mapSociosQuotas.get(numSocio).getPaid();
                        mapSociosQuotas.get(numSocio).setPaid(total);
                    } else {
                        notFoundSocios.put(transaction_description.getStringCellValue(), credit_amount.getNumericCellValue());
                    }
                } else {
                    notFoundSocios.put(transaction_description.getStringCellValue(), credit_amount.getNumericCellValue());
                }
            }
            file.close();

            copyNotFoundSociosToExcel(workbook.getSheetAt(1));
            copyFoundSociosToExcel(workbook.getSheetAt(2));
            try (FileOutputStream outputStream = new FileOutputStream("Alba" + term.getName() + ".xlsx")) {
                workbook.write(outputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void copyNotFoundSociosToExcel(XSSFSheet sheetAt) {
        int i = 0;
        for (String s : notFoundSocios.keySet()) {
            ExcelHelper.copyDataInASheet(sheetAt, i++, s, notFoundSocios.get(s).doubleValue());
        }
    }
    private static void copyFoundSociosToExcel(XSSFSheet sheetAt) {
        int i = 0;
        for (String s : mapSociosQuotas.keySet()) {
            ExcelHelper.copyDataInASheet(sheetAt, i++, s, mapSociosQuotas.get(s).getParentName(), mapSociosQuotas.get(s).getAmount(), mapSociosQuotas.get(s).getPaid());
        }
    }

    public static Map<String, Socio> getMapSociosQuotas() throws SQLException {
        DataSource dataSource = connectToDB();
        Connection conn = dataSource.getConnection();
        return getAllNumberSocioWithQuota(conn);
    }

    private static String findWithAnyFourDigitsInsideString(Cell transaction_description) {
        Pattern patternSocio = Pattern.compile("\\b\\d{4}\\b");
        Matcher m = patternSocio.matcher(transaction_description.getStringCellValue());
        String numSocio = null;
        if (m.find()) {
            numSocio = m.group().trim();
        }
        return numSocio;
    }
    private static String findNumSocioWithHyphen(Cell transaction_description) {
        Pattern patternSocio = Pattern.compile("\\d{4}-\\S+");
        Matcher m = patternSocio.matcher(transaction_description.getStringCellValue());
        String numSocio = null;
        if (m.find()) {
            numSocio = m.group().substring(0, m.group().indexOf("-")).trim();
        }
        return numSocio;
    }

    private static String findNumSocio(Cell transaction_description) {
        Pattern patternSocio = Pattern.compile("\\s\\d{4}\\s");
        Matcher m = patternSocio.matcher(transaction_description.getStringCellValue());
        String numSocio = null;
        if (m.find()) {
            numSocio = m.group().trim();
        }
        return numSocio;
    }

    private static DataSource connectToDB() {
        final String url =
                "jdbc:postgresql://localhost/alba?user=admin&password=admin";
        final PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(url);
        return dataSource;
    }

    private static Map<String, String> getParentNames(Connection conn) throws SQLException {
        Map<String, String> dvalues = new HashMap<>();
        PreparedStatement stmt = conn.prepareStatement("select * from socio");
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            dvalues.put(rs.getString("num_socio"), rs.getString("parents_name"));
        }
        return dvalues;
    }

    private static Map<String, Socio> getAllNumberSocioWithQuota(Connection conn) throws SQLException {
        Map<String, Socio> dvalues = new HashMap<>();
        PreparedStatement stmt = conn.prepareStatement("select * from socio");
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            Socio socio = new Socio();
            socio.setParentName(rs.getString("parents_name"));
            socio.setAmount(rs.getInt("num_kids") * quota);
            socio.setPaid(0.0);
            dvalues.put(rs.getString("num_socio"), socio);
        }
        return dvalues;
    }

    private static Stream<String> tokenize(String s) {
        return Arrays.stream(s.split(" "))
                .map(String::toLowerCase);
    }

    private static Double findASocioByParentName(Connection conn, String num_socio) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM socio WHERE num_socio = ?");
        stmt.setString(1, num_socio);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            String parentName = rs.getString("parents_name");
            for (String s : map.keySet()) {

                Set<String> words = tokenize(parentName).collect(Collectors.toSet());
                long count = tokenize(s).filter(words::contains).count();
                boolean found = count > 1;

                if (found) {
                    Double value = map.get(s);
                    map.remove(s);
                    return value;
                }
            }
        }

        return 0.0;
    }

    private static void insertSocio(Connection conn, String num_socio) throws SQLException {
        PreparedStatement insertStmt =
                conn.prepareStatement("INSERT INTO socio(num_socio) VALUES (?)");
        insertStmt.setString(1, num_socio);
        int insertedRows = insertStmt.executeUpdate();
        System.out.printf("inserted %s socio(s)%n", insertedRows);
    }

    private static void updateSocio(Connection conn, Long id, String num_socio) throws SQLException {
        PreparedStatement updateStmt =
                conn.prepareStatement("UPDATE socio SET num_socio = ? WHERE id = ?");
        updateStmt.setString(1, num_socio);
        updateStmt.setLong(2, id);
        int updatedRows = updateStmt.executeUpdate();
        System.out.printf("updated %s socio(s)%n", updatedRows);
    }

    private static void deleteSocio(Connection conn, String num_socio) throws SQLException {
        PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM socio WHERE num_socio = ?");
        deleteStmt.setString(1, num_socio);
        int deletedRows = deleteStmt.executeUpdate();
        System.out.printf("deleted %s socio(s)%n", deletedRows);
    }
}
