package com.qsmart.dto;

public class SmsInboundRequest {
    private String from;      // sender phone number e.g. "+919876543210" or "9876543210"
    private String text;      // SMS message body e.g. "BOOK HOSPITAL01 OPD"
    private String timestamp; // optional provider timestamp

    public SmsInboundRequest() {}

    public SmsInboundRequest(String from, String text) {
        this.from = from;
        this.text = text;
    }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
