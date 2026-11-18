package com.qsmart.service;

import com.qsmart.model.SmsMessage;
import com.qsmart.repository.SmsMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MockSmsProvider implements SmsProvider {

    private static final Logger log = LoggerFactory.getLogger(MockSmsProvider.class);

    private final SmsMessageRepository smsMessageRepository;

    @Autowired
    public MockSmsProvider(SmsMessageRepository smsMessageRepository) {
        this.smsMessageRepository = smsMessageRepository;
    }

    @Override
    public boolean sendSms(String to, String message) {
        log.info("[MOCK SMS GATEWAY] To: {} | Content: {}", to, message);

        try {
            SmsMessage record = new SmsMessage(to, "OUTBOUND", message, "NOTIFICATION", "SENT", null);
            smsMessageRepository.save(record);
            return true;
        } catch (Exception e) {
            log.error("Failed to record mock SMS", e);
            return false;
        }
    }

    @Override
    public String getProviderName() {
        return "QSmart Mock SMS Gateway (Development Mode)";
    }

    @Override
    public boolean isMock() {
        return true;
    }
}
