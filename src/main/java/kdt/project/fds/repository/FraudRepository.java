package kdt.project.fds.repository;

import kdt.project.fds.dto.FraudDetailDTO;
import kdt.project.fds.entity.FraudDetectionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface FraudRepository extends JpaRepository<FraudDetectionResult, Long> {
    Optional<FraudDetectionResult> findByTxId(Long txId);
    @Query("""
    SELECT new kdt.project.fds.dto.FraudDetailDTO(
        f.id,
        t.txId,
        u.userName,
        u.userId,
        t.sourceValue,
        t.targetValue,
        CAST(t.txAmount AS double),
        f.probability,
        f.isFraud,
        f.engine,
        tf.vFeatures,
        t.txTimestamp,
        tf.oldBalanceOrg,
        tf.newBalanceOrg,
        CAST(a.balance AS double)
    )
    FROM FraudDetectionResult f
    JOIN Transaction t ON f.txId = t.txId
    LEFT JOIN User u ON t.userId = u.userId
    LEFT JOIN TransactionFeature tf ON t.txId = tf.txId
    LEFT JOIN Account a ON t.sourceValue = a.accountNum
    ORDER BY f.id DESC
""")
    List<FraudDetailDTO> findAllWithDetails();
    // [추가] 특정 거래 ID에 해당하는 모든 탐지 결과 삭제
    void deleteByTxId(Long txId);

}