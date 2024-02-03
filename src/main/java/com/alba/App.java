package com.alba;

import com.alba.utils.DateHelper;
import com.alba.utils.ExcelHelper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class App {

    private final static Map<String, Double> map = new HashMap<>();
    private static Map<String[], Double[]> mapSociosQuotas = new HashMap<>();
    private final static Set<String> notFoundSocios = new HashSet<>();
    private final static Set<String> foundWithName = new HashSet<>();

    private final static double quota = 63.0;

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
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//            uploadXslxFromBankWithNumSocio();

//            crossReferencesWithSocios();
//            excelTable();
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


    public static void crossReferencesWithSocios() throws SQLException {

        DataSource dataSource = connectToDB();
        Connection conn = dataSource.getConnection();
        mapSociosQuotas = getAllNumberSocioWithQuota(conn);

        for (String[] st : mapSociosQuotas.keySet()) {
            String s = st[0];
            if (map.containsKey(s)) {
                Double[] values = mapSociosQuotas.get(s);
                values[1] = map.get(s);
                mapSociosQuotas.put(st, values);
                map.remove(s);
            } else {
                Double aSocioByParentName = findASocioByParentName(conn, s);
                if (aSocioByParentName != 0.0) {
                    Double[] values = mapSociosQuotas.get(s);
                    values[1] = values[1] + aSocioByParentName;
                    mapSociosQuotas.put(st, values);
                    map.remove(s);
                } else {
                    notFoundSocios.add(s);
                }
            }
        }

    }

    public static void uploadXslxFromBankWithNumSocio() {

        try {
            FileInputStream file = new FileInputStream("/Users/jd185241/dev/alba/alba/src/main/resources/Statement_Aug23_Feb24.xlsx");
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

                String numSocio = findNumSocio(transaction_description, credit_amount);
                if (numSocio == null) {
                    numSocio = findNumSocioWithHyphen(transaction_description, credit_amount);
                }

                if (numSocio == null) {
                    numSocio = findWithParentName(transaction_description, credit_amount);
                }

                if (numSocio != null) {
                    if (map.containsKey(numSocio)) {
                        double total = credit_amount.getNumericCellValue() + map.get(numSocio);
                        map.put(numSocio, total);
                    } else {
                        map.put(numSocio, credit_amount.getNumericCellValue());
                    }
                } else {
                    notFoundSocios.add(transaction_description.getStringCellValue());
                }
            }
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String findNumSocioWithHyphen(Cell transaction_description, Cell credit_amount) {
        Pattern patternSocio = Pattern.compile("\\d{4}-\\S+");
        Matcher m = patternSocio.matcher(transaction_description.getStringCellValue());
        String numSocio = null;
        if (m.find()) {
            numSocio = m.group().substring(0, m.group().indexOf("-"));
        }
        return numSocio;
    }

    private static String findNumSocio(Cell transaction_description, Cell credit_amount) {
        Pattern patternSocio = Pattern.compile("\\s\\d{4}\\s");
        Matcher m = patternSocio.matcher(transaction_description.getStringCellValue());
        String numSocio = null;
        if (m.find()) {
            numSocio = m.group();
        }
        return numSocio;
    }

    private static String findWithParentName(Cell transaction_description, Cell credit_amount) throws SQLException {
        Pattern patternWithNames = Pattern.compile("([^\\d\\s]+) ([^\\d\\s]+) ([^\\d\\s]+) ([^\\d\\s]+)");
        Matcher m = patternWithNames.matcher(transaction_description.getStringCellValue());

        DataSource dataSource = connectToDB();
        Connection conn = dataSource.getConnection();

        Map<String, String> parentNames = getParentNames(conn);
        String numSocio = null;
        while (m.find()) {
            numSocio = m.group();
            foundWithName.add(numSocio);
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

    private static Map<String[], Double[]> getAllNumberSocioWithQuota(Connection conn) throws SQLException {
        Map<String[], Double[]> dvalues = new HashMap<>();
        PreparedStatement stmt = conn.prepareStatement("select * from socio");
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            dvalues.put(new String[]{rs.getString("num_socio"), rs.getString("parents_name")}, new Double[]{rs.getInt("num_kids") * quota, 0.0});

            System.out.printf("id:%d num_socio:%s num_kids:%s quota:%s%n", rs.getLong("id"),
                    rs.getString("num_socio"), rs.getInt("num_kids"), rs.getInt("num_kids") * quota);
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
