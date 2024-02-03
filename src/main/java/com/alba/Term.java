package com.alba;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Term {

    public enum Dates {
        FIRST_TERM("1Term", "01/08/2023", "31/12/2023"),
        SECOND_TERM("2Term", "01/01/2024", "31/03/2024"),
        THIRD_TERM("3Term", "01/04/2024", "31/07/2024");

        private String startDate;
        private String endDate;
        private String name;

        Dates(String name, String startDate, String endDate) {
            this.name = name;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public String getName() {
            return name;
        }

        public String getStartDate() {
            return startDate;
        }

        public String getEndDate() {
            return endDate;
        }
    }

    public static boolean isDateInRange(Dates dates, LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate startDate = LocalDate.parse(dates.getStartDate(), formatter);
        LocalDate endDate = LocalDate.parse(dates.getEndDate(), formatter);
        return (date.isAfter(startDate) || date.isEqual(startDate)) && (date.isBefore(endDate) || date.isEqual(endDate));
    }
}