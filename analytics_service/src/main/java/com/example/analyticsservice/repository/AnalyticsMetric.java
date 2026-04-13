package com.example.analyticsservice.repository;

public enum AnalyticsMetric {
    IMPRESSIONS("impressions"),
    CLICKS("clicks"),
    CONVERSIONS("conversions");

    private final String fieldName;

    AnalyticsMetric(String fieldName) {
        this.fieldName = fieldName;
    }

    public String fieldName() {
        return fieldName;
    }
}