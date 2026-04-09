package com.example.campaignservice.exception;

public class InvalidCampaignStateException extends RuntimeException {

    public InvalidCampaignStateException(String message) {
        super(message);
    }
}