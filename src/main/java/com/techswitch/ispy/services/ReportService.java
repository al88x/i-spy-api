package com.techswitch.ispy.services;

import com.techswitch.ispy.models.Report;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.Date;

@Service
public class ReportService extends DatabaseService {

    @Autowired
    protected ReportService(@Qualifier("databaseUrl") String url) {
        super(url);
    }

    public Long createReport(Report report){
           return jdbi.withHandle(handle ->
                    handle.createQuery("INSERT INTO reports (suspect_id, date_of_sighting, location, description) " +
                            "VALUES (:suspectId, :date::date, :location, :description) RETURNING id")
                            .bind("suspectId", report.getSuspectId())
                            .bind("date", report.getDate())
                            .bind("location", report.getLocation())
                            .bind("description", report.getDescription())
                            .mapTo(Long.class)
                            .findOnly()
            );
    }
}
