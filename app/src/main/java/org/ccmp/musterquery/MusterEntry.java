package org.ccmp.musterquery;

public class MusterEntry {
    int id;
    String name;
    int daysWorked;
    int totalWage;
    String creditStatus;
    String creditedDate;

    public MusterEntry(int id, String name, int daysWorked, int totalWage, String creditStatus, String creditedDate) {
        this.id = id;
        this.name = name;
        this.daysWorked = daysWorked;
        this.totalWage = totalWage;
        this.creditStatus = creditStatus;
        this.creditedDate = creditedDate;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getDaysWorked() {
        return daysWorked;
    }

    public int getTotalWage() {
        return totalWage;
    }

    public String getCreditStatus() {
        return creditStatus;
    }

    public String getCreditedDate() {
        return creditedDate;
    }



}
