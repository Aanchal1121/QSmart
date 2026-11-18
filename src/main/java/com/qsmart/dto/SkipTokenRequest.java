package com.qsmart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SkipTokenRequest {
    @NotNull(message = "Token ID is required")
    private Long tokenId;

    @NotBlank(message = "Skip reason is required")
    private String reason;

    public SkipTokenRequest() {}

    public Long getTokenId() { return tokenId; }
    public void setTokenId(Long tokenId) { this.tokenId = tokenId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
