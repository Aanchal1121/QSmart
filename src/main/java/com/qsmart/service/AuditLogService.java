package com.qsmart.service;

import com.qsmart.model.AuditLog;
import com.qsmart.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Autowired
    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void log(String action, String performedBy, String targetEntity, String targetId, String details) {
        AuditLog entry = new AuditLog(action, performedBy, targetEntity, targetId, details);
        auditLogRepository.save(entry);
    }

    public List<AuditLog> getRecentLogs() {
        return auditLogRepository.findTop100ByOrderByTimestampDesc();
    }
}
