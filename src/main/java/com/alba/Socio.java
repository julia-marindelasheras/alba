package com.alba;

import java.util.Objects;

public class Socio {

    private String parentName;

    private double amount;

    private double paid;

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getPaid() {
        return paid;
    }

    public void setPaid(double paid) {
        this.paid = paid;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Socio socio = (Socio) o;
        return Double.compare(amount, socio.amount) == 0 && Double.compare(paid, socio.paid) == 0 && Objects.equals(parentName, socio.parentName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentName, amount, paid);
    }
}
