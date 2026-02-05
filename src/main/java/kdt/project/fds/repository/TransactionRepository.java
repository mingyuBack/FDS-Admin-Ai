package kdt.project.fds.repository;

import kdt.project.fds.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * 1. 관리자용: 모든 거래 내역 조회 (유저 이름 JOIN FETCH)
     * '미등록' 문제를 해결하기 위해 User 테이블과 조인하여 가져옵니다.
     */
    @Query("SELECT t FROM Transaction t JOIN FETCH t.user ORDER BY t.txTimestamp DESC")
    List<Transaction> findAllWithUserOrderByTxTimestampDesc();

    /**
     * 2. 고액 거래 필터링 (쿼리 직접 정의)
     * [수정] @Query를 추가하여 'findHighAmountTransactions' 에러를 해결합니다.
     */
    @Query("SELECT t FROM Transaction t JOIN FETCH t.user WHERE t.txAmount >= :minAmount ORDER BY t.txTimestamp DESC")
    List<Transaction> findHighAmountTransactions(@Param("minAmount") Double minAmount);

    /**
     * 3. 특정 계좌의 최근 거래 10건 (사용자 화면용)
     */
    List<Transaction> findTop10BySourceValueOrderByTxTimestampDesc(String sourceValue);

    /**
     * 4. 특정 수취인 거래 조회
     */
    List<Transaction> findByTargetValue(String targetValue);

    /**
     * 5. 단순 전체 조회 (최신순)
     */
    List<Transaction> findAllByOrderByTxTimestampDesc();
}