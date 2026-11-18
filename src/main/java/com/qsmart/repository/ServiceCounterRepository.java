package com.qsmart.repository;

import com.qsmart.model.CounterStatus;
import com.qsmart.model.ServiceCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceCounterRepository extends JpaRepository<ServiceCounter, Long> {
    List<ServiceCounter> findByDepartmentId(Long departmentId);
    List<ServiceCounter> findByDepartmentIdAndStatus(Long departmentId, CounterStatus status);
    Optional<ServiceCounter> findByStaffId(Long staffId);
    Optional<ServiceCounter> findByDoctorId(Long doctorId);
    long countByDepartmentIdAndStatus(Long departmentId, CounterStatus status);
}
