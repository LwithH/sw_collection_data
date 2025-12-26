package com.model;

import java.util.Date;

public class Dsp {
    private Date dayStart;
    private Date dayEnd;
    private String currency;
    private double rate;
    
    // Getters and Setters
    public Date getDayStart() {
        return dayStart;
    }
    public void setDayStart(Date dayStart) {
        this.dayStart = dayStart;
    }
    
    public Date getDayEnd() {
        return dayEnd;
    }
    public void setDayEnd(Date dayEnd) {
        this.dayEnd = dayEnd;
    }
    
    public String getCurrency() {
        return currency;
    }
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public double getRate() {
        return rate;
    }
    public void setRate(double rate) {
        this.rate = rate;
    }
}
