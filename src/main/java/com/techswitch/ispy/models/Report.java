package com.techswitch.ispy.models;

import com.techswitch.ispy.models.validator.ValidDate;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.techswitch.ispy.models.validator.DateValidator.DATE_FORMAT_INPUT;

public class Report {
    @NotNull(message = "Suspect ID cannot be null")
    private Long suspectId;
    @ValidDate(message = "Please use a DD-MM-YYYY format for date")
    private String date;
    private String location;
    @NotNull(message = "Description cannot be empty")
    @NotEmpty(message = "Description cannot be empty")
    private String description;

    public Report() {
    }

    public Report(Long suspectId, String date, String location, String description) {
        this.suspectId = suspectId;
        this.date = date;
        this.location = location;
        this.description = description;
    }

    public Long getSuspectId() {
        return suspectId;
    }

    public void setSuspectId(Long suspectId) {
        this.suspectId = suspectId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date createSqlDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT_INPUT);
        return Date.valueOf(LocalDate.parse(date, formatter));
    }
}
