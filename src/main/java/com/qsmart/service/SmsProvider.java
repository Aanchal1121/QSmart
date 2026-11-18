package com.qsmart.service;

public interface SmsProvider {
    /**
     * Send an outbound SMS message to a mobile number
     * @param to recipient phone number
     * @param message SMS text content
     * @return boolean true if sent/queued successfully
     */
    boolean sendSms(String to, String message);

    /**
     * Name of the SMS provider implementation
     */
    String getProviderName();

    /**
     * Check if this is a development mock provider
     */
    boolean isMock();
}
