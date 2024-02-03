package com.alba;

import java.util.Date;

public class Term {

    public enum Dates {
        FIRST_TERM("1Term","08/01/2023", "12/31/2023"),
        SECOND_TERM("2Term", "01/01/2024", "03/31/2024"),
        THIRD_TERM("3Term", "04/01/2024", "07/31/2024");

        private String startDate;
        private String endDate;
        private String name;

        Dates(String name, String startDate, String endDate) {
            this.name = name;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public String getName(){
            return name;
        }
        public String getStartDate() {
            return startDate;
        }

        public String getEndDate() {
            return endDate;
        }
    }
}