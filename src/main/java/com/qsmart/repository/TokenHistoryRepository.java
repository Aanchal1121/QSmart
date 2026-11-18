package com.qsmart.repository;

import com.qsmart.model.TokenHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TokenHistoryRepository extends JpaRepository<TokenHistory, Long> {
    List<TokenHistory> findByQueueTokenIdOrderByTimestampAsc(Long queueTokenId);
}
