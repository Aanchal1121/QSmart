package com.qsmart.dto;

public class CallNextRequest {
    private Long counterId;
    private Long specificTokenId; // optional

    public CallNextRequest() {}

    public Long getCounterId() { return counterId; }
    public void setCounterId(Long counterId) { this.counterId = counterId; }

    public Long getSpecificTokenId() { return specificTokenId; }
    public void setSpecificTokenId(Long specificTokenId) { this.specificTokenId = specificTokenId; }
}
