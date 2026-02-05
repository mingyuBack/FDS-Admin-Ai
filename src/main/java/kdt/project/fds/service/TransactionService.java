package kdt.project.fds.service;

import kdt.project.fds.entity.Account;
import kdt.project.fds.entity.Transaction;
import kdt.project.fds.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final DetectionService detectionService;


    private final TransactionFeatureRepository featureRepository;
    private final FraudRepository fraudRepository;

    // Processor들
    private final TransferProcessor transferProcessor;
    private final CardProcessor cardProcessor;

    // [추가] 새로 만든 검문소
    private final TransactionValidator validator;

    @Transactional
    public Transaction processTransfer(Transaction txRequest) {
        // 1. 기본 정보 설정 & 계좌 매핑
        txRequest.setTxTimestamp(LocalDateTime.now());

        if (!"CARD".equalsIgnoreCase(txRequest.getTxType())) {
            Account senderAccount = accountRepository.findByAccountNum(txRequest.getSourceValue())
                    .orElseThrow(() -> new RuntimeException("송금 계좌를 찾을 수 없습니다."));
            txRequest.setSourceId(senderAccount.getAccountId());
        }

        // 2. 거래 기록 우선 저장 (Log)
        Transaction savedTx = transactionRepository.save(txRequest);
        // [1차 관문] 정적 규칙 검사 (Validator 위임)
        if (validator.shouldBlock(savedTx)) {
            return savedTx; // 차단/격리된 경우 여기서 종료
        }
        // [2차 관문] AI 탐지 (DetectionService 위임)
        // -------------------------------------------------------------------------
        int fraudStatus = detectionService.detectAndSave(savedTx);
        if (fraudStatus == 1) {
            log.warn("⚠️ [격리] AI 판정 이상 거래");
            return savedTx;
        }

        return executeTransfer(savedTx);
    }

    public Transaction executeTransfer(Transaction tx) {
        if ("CARD".equalsIgnoreCase(tx.getTxType())) {
            return cardProcessor.execute(tx);
        } else {
            return transferProcessor.execute(tx);
        }
    }

    @Transactional
    public void deleteTransactionData(Long txId) {
        log.info("삭제 프로세스 시작 - TX_ID: {}", txId);
        if (featureRepository.existsById(txId)) featureRepository.deleteById(txId);
        fraudRepository.deleteByTxId(txId);
        transactionRepository.deleteById(txId);
        log.info("삭제 완료");
    }
}