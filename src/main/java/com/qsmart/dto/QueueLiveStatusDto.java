package com.qsmart.dto;

import java.util.List;

public class QueueLiveStatusDto {
    private Long departmentId;
    private String departmentName;
    private String departmentCode;
    private String hospitalName;
    private String hospitalCode;
    private String currentServingToken;
    private String currentServingPatient;
    private String currentCounterNumber;
    private List<TokenStatusResponse> waitingTokens;
    private List<TokenStatusResponse> recentlyCalledTokens;
    private int totalWaiting;
    private int totalServedToday;
    private int totalCancelledToday;
    private int totalSkippedToday;
    private int activeCountersCount;
    private int activeDoctorsCount;
    private int avgWaitMinutes;
    private int delayMinutes;
    private String delayReason;
    private boolean queuePaused;

    public QueueLiveStatusDto() {}

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    public String getHospitalCode() { return hospitalCode; }
    public void setHospitalCode(String hospitalCode) { this.hospitalCode = hospitalCode; }

    public String getCurrentServingToken() { return currentServingToken; }
    public void setCurrentServingToken(String currentServingToken) { this.currentServingToken = currentServingToken; }

    public String getCurrentServingPatient() { return currentServingPatient; }
    public void setCurrentServingPatient(String currentServingPatient) { this.currentServingPatient = currentServingPatient; }

    public String getCurrentCounterNumber() { return currentCounterNumber; }
    public void setCurrentCounterNumber(String currentCounterNumber) { this.currentCounterNumber = currentCounterNumber; }

    public List<TokenStatusResponse> getWaitingTokens() { return waitingTokens; }
    public void setWaitingTokens(List<TokenStatusResponse> waitingTokens) { this.waitingTokens = waitingTokens; }

    public List<TokenStatusResponse> getRecentlyCalledTokens() { return recentlyCalledTokens; }
    public void setRecentlyCalledTokens(List<TokenStatusResponse> recentlyCalledTokens) { this.recentlyCalledTokens = recentlyCalledTokens; }

    public int getTotalWaiting() { return totalWaiting; }
    public void setTotalWaiting(int totalWaiting) { this.totalWaiting = totalWaiting; }

    public int getTotalServedToday() { return totalServedToday; }
    public void setTotalServedToday(int totalServedToday) { this.totalServedToday = totalServedToday; }

    public int getTotalCancelledToday() { return totalCancelledToday; }
    public void setTotalCancelledToday(int totalCancelledToday) { this.totalCancelledToday = totalCancelledToday; }

    public int getTotalSkippedToday() { return totalSkippedToday; }
    public void setTotalSkippedToday(int totalSkippedToday) { this.totalSkippedToday = totalSkippedToday; }

    public int getActiveCountersCount() { return activeCountersCount; }
    public void setActiveCountersCount(int activeCountersCount) { this.activeCountersCount = activeCountersCount; }

    public int getActiveDoctorsCount() { return activeDoctorsCount; }
    public void setActiveDoctorsCount(int activeDoctorsCount) { this.activeDoctorsCount = activeDoctorsCount; }

    public int getAvgWaitMinutes() { return avgWaitMinutes; }
    public void setAvgWaitMinutes(int avgWaitMinutes) { this.avgWaitMinutes = avgWaitMinutes; }

    public int getDelayMinutes() { return delayMinutes; }
    public void setDelayMinutes(int delayMinutes) { this.delayMinutes = delayMinutes; }

    public String getDelayReason() { return delayReason; }
    public void setDelayReason(String delayReason) { this.delayReason = delayReason; }

    public boolean isQueuePaused() { return queuePaused; }
    public void setQueuePaused(boolean queuePaused) { this.queuePaused = queuePaused; }
}
