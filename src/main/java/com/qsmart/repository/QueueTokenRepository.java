package com.qsmart.repository;

import com.qsmart.model.AccessChannel;
import com.qsmart.model.QueueToken;
import com.qsmart.model.TokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface QueueTokenRepository extends JpaRepository<QueueToken, Long> {

    Optional<QueueToken> findByTokenNumberAndTokenDate(String tokenNumber, LocalDate tokenDate);

    Optional<QueueToken> findByTokenNumberIgnoreCase(String tokenNumber);

    Optional<QueueToken> findByVerificationQrHash(String verificationQrHash);

    List<QueueToken> findByDepartmentIdAndTokenDateAndStatusOrderBySequenceNumberAsc(Long departmentId, LocalDate tokenDate, TokenStatus status);

    List<QueueToken> findByDepartmentIdAndTokenDateOrderBySequenceNumberAsc(Long departmentId, LocalDate tokenDate);

    @Query("SELECT COALESCE(MAX(t.sequenceNumber), 0) FROM QueueToken t WHERE t.department.id = :departmentId AND t.tokenDate = :tokenDate")
    int findMaxSequenceNumber(@Param("departmentId") Long departmentId, @Param("tokenDate") LocalDate tokenDate);

    @Query("SELECT t FROM QueueToken t WHERE t.patientPhone = :phone AND t.department.id = :departmentId AND t.tokenDate = :tokenDate AND t.status IN :statuses")
    List<QueueToken> findActiveTokensByPhoneAndDepartment(@Param("phone") String phone,
                                                         @Param("departmentId") Long departmentId,
                                                         @Param("tokenDate") LocalDate tokenDate,
                                                         @Param("statuses") Collection<TokenStatus> statuses);

    @Query("SELECT t FROM QueueToken t WHERE t.user.id = :userId AND t.tokenDate = :tokenDate AND t.status IN :statuses")
    List<QueueToken> findActiveTokensByUser(@Param("userId") Long userId,
                                            @Param("tokenDate") LocalDate tokenDate,
                                            @Param("statuses") Collection<TokenStatus> statuses);

    @Query("SELECT t FROM QueueToken t WHERE (t.user.id = :userId OR t.patientPhone = :phone) AND t.tokenDate = :tokenDate AND t.status IN :statuses")
    List<QueueToken> findActiveTokensByUserOrPhone(@Param("userId") Long userId,
                                                   @Param("phone") String phone,
                                                   @Param("tokenDate") LocalDate tokenDate,
                                                   @Param("statuses") Collection<TokenStatus> statuses);

    @Query("SELECT COUNT(t) FROM QueueToken t WHERE t.department.id = :departmentId AND t.tokenDate = :tokenDate AND t.status = 'WAITING' AND t.sequenceNumber < :sequenceNumber")
    long countWaitingAhead(@Param("departmentId") Long departmentId,
                           @Param("tokenDate") LocalDate tokenDate,
                           @Param("sequenceNumber") int sequenceNumber);

    List<QueueToken> findByTokenDate(LocalDate tokenDate);

    long countByDepartmentIdAndTokenDateAndStatus(Long departmentId, LocalDate tokenDate, TokenStatus status);

    long countByTokenDate(LocalDate tokenDate);

    long countByTokenDateAndStatus(LocalDate tokenDate, TokenStatus status);

    long countByTokenDateAndAccessChannel(LocalDate tokenDate, AccessChannel accessChannel);

    List<QueueToken> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<QueueToken> findByPatientPhoneOrderByCreatedAtDesc(String phone);

    List<QueueToken> findTop100ByOrderByCreatedAtDesc();
}
