package kdt.project.fds.service;

import kdt.project.fds.entity.Transaction;
import kdt.project.fds.repository.FraudRepository;
import kdt.project.fds.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final FraudRepository fraudRepository;
    private final TransactionRepository transactionRepository;

    // [변경] Repository 대신 Service를 사용합니다.
    private final BlacklistService blacklistService;
    private final TransferProcessor transferProcessor;

    /**
     * 관리자 승인
     */
    @Transactional
    public String approveTransaction(Long txId) {
        Transaction tx = transactionRepository.findById(txId)
                .orElseThrow(() -> new RuntimeException("거래 정보를 찾을 수 없습니다. ID: " + txId));

        transferProcessor.execute(tx);

        fraudRepository.findByTxId(txId).ifPresent(fdsResult -> {
            fdsResult.setIsFraud(0);
            fdsResult.setEngine("관리자 수동 승인: 송금 완료");
            fraudRepository.save(fdsResult);
        });

        log.info("거래 승인 완료: TX_ID {}", txId);
        return "거래 ID [" + txId + "]가 승인되어 송금이 완료되었습니다.";
    }

    /**
     * 관리자 거절
     */
    @Transactional
    public String rejectTransaction(Long txId) {
        Transaction tx = transactionRepository.findById(txId)
                .orElseThrow(() -> new RuntimeException("기록 없음: ID " + txId));

        // [변경] 직접 Repository 저장 안 하고, 전문가(BlacklistService)에게 위임
        String scammerAccount = tx.getTargetValue();
        blacklistService.addToBlacklist(scammerAccount, "관리자 수동 거절 (TX_ID: " + txId + ")");

        fraudRepository.findByTxId(txId).ifPresent(fdsResult -> {
            fdsResult.setIsFraud(1);
            fdsResult.setEngine("관리자 거절 확정 및 블랙리스트 등록");
            fraudRepository.save(fdsResult);
        });

        log.info("거래 거절 완료: {}", scammerAccount);
        return "거절 완료: 수취인 계좌 [" + scammerAccount + "]가 차단되었습니다.";
    }

}