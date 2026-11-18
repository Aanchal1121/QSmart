package com.qsmart.repository;

import com.qsmart.model.DoctorAvailability;
import com.qsmart.model.DoctorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {
    Optional<DoctorAvailability> findByDoctorId(Long doctorId);
    List<DoctorAvailability> findByDepartmentId(Long departmentId);
    List<DoctorAvailability> findByDepartmentIdAndStatus(Long departmentId, DoctorStatus status);
}
