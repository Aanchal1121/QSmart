package com.qsmart.repository;

import com.qsmart.model.PriorityRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriorityRequestRepository extends JpaRepository<PriorityRequest, Long> {
    List<PriorityRequest> findByStatusOrderByCreatedAtDesc(String status);
    List<PriorityRequest> findTop50ByOrderByCreatedAtDesc();
}
