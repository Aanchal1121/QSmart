package com.qsmart.service;

import com.qsmart.model.AccessChannel;
import com.qsmart.model.Department;
import com.qsmart.model.QueueToken;
import com.qsmart.model.TokenStatus;
import com.qsmart.repository.DepartmentRepository;
import com.qsmart.repository.QueueTokenRepository;
import com.qsmart.repository.ServiceCounterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

@Service
public class AnalyticsService {

    private final QueueTokenRepository tokenRepository;
    private final DepartmentRepository departmentRepository;
    private final ServiceCounterRepository counterRepository;

    @Autowired
    public AnalyticsService(QueueTokenRepository tokenRepository,
                            DepartmentRepository departmentRepository,
                            ServiceCounterRepository counterRepository) {
        this.tokenRepository = tokenRepository;
        this.departmentRepository = departmentRepository;
        this.counterRepository = counterRepository;
    }

    public Map<String, Object> getOverallAnalytics() {
        LocalDate today = LocalDate.now();
        List<QueueToken> todayTokens = tokenRepository.findByTokenDate(today);

        long totalBookings = todayTokens.size();
        long servedCount = todayTokens.stream().filter(t -> t.getStatus() == TokenStatus.SERVED).count();
        long waitingCount = todayTokens.stream().filter(t -> t.getStatus() == TokenStatus.WAITING).count();
        long calledCount = todayTokens.stream().filter(t -> t.getStatus() == TokenStatus.CALLED).count();
        long cancelledCount = todayTokens.stream().filter(t -> t.getStatus() == TokenStatus.CANCELLED).count();
        long skippedCount = todayTokens.stream().filter(t -> t.getStatus() == TokenStatus.SKIPPED).count();

        // Channel breakdown
        long smartphoneCount = todayTokens.stream().filter(t -> t.getAccessChannel() == AccessChannel.SMARTPHONE).count();
        long smsCount = todayTokens.stream().filter(t -> t.getAccessChannel() == AccessChannel.SMS).count();
        long walkInCount = todayTokens.stream().filter(t -> t.getAccessChannel() == AccessChannel.WALK_IN).count();

        // Average wait time (created to called) for served/called tokens
        double avgWaitMinutes = todayTokens.stream()
                .filter(t -> t.getCalledAt() != null && t.getCreatedAt() != null)
                .mapToLong(t -> Math.max(0, Math.abs(Duration.between(t.getCreatedAt(), t.getCalledAt()).toMinutes())))
                .average()
                .orElse(12.0);

        // Average service duration (called to served)
        double avgServiceMinutes = todayTokens.stream()
                .filter(t -> t.getServedAt() != null && t.getCalledAt() != null)
                .mapToLong(t -> Math.max(0, Math.abs(Duration.between(t.getCalledAt(), t.getServedAt()).toMinutes())))
                .average()
                .orElse(8.5);

        // Department-wise distribution
        Map<String, Long> deptCounts = new HashMap<>();
        List<Department> departments = departmentRepository.findAll();
        for (Department d : departments) {
            long count = todayTokens.stream().filter(t -> t.getDepartment().getId().equals(d.getId())).count();
            deptCounts.put(d.getName(), count);
        }

        // Peak hours distribution (08:00 - 18:00)
        Map<String, Integer> hourlyDistribution = new LinkedHashMap<>();
        for (int h = 8; h <= 18; h++) {
            String hourKey = String.format("%02d:00", h);
            int count = 0;
            for (QueueToken t : todayTokens) {
                if (t.getCreatedAt() != null && t.getCreatedAt().getHour() == h) {
                    count++;
                }
            }
            hourlyDistribution.put(hourKey, count);
        }

        // Counter utilization
        long totalCounters = counterRepository.count();

        Map<String, Object> data = new HashMap<>();
        data.put("totalBookings", totalBookings);
        data.put("servedCount", servedCount);
        data.put("waitingCount", waitingCount);
        data.put("calledCount", calledCount);
        data.put("cancelledCount", cancelledCount);
        data.put("skippedCount", skippedCount);
        data.put("smartphoneCount", smartphoneCount);
        data.put("smsCount", smsCount);
        data.put("walkInCount", walkInCount);
        data.put("avgWaitMinutes", Math.round(avgWaitMinutes));
        data.put("avgServiceMinutes", Math.round(avgServiceMinutes));
        data.put("departmentDistribution", deptCounts);
        data.put("hourlyDistribution", hourlyDistribution);
        data.put("totalCounters", totalCounters);

        return data;
    }
}
