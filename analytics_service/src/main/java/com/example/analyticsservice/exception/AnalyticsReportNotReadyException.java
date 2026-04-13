package com.example.analyticsservice.exception;

public class AnalyticsReportNotReadyException extends RuntimeException {

    public AnalyticsReportNotReadyException(String message) {
        super(message);
    }
}