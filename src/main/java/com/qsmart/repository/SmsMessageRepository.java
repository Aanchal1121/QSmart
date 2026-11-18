package com.qsmart.repository;

import com.qsmart.model.SmsMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SmsMessageRepository extends JpaRepository<SmsMessage, Long> {
    List<SmsMessage> findByPhoneOrderByTimestampDesc(String phone);
    List<SmsMessage> findTop50ByOrderByTimestampDesc();
}
