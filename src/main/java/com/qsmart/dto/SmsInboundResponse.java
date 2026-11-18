package com.qsmart.dto;

public class SmsInboundResponse {
    private boolean success;
    private String reply;
    private String tokenNumber;
    private String command;

    public SmsInboundResponse() {}

    public SmsInboundResponse(boolean success, String reply, String tokenNumber, String command) {
        this.success = success;
        this.reply = reply;
        this.tokenNumber = tokenNumber;
        this.command = command;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }

    public String getTokenNumber() { return tokenNumber; }
    public void setTokenNumber(String tokenNumber) { this.tokenNumber = tokenNumber; }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }
}
