package kdt.project.fds.service;

import kdt.project.fds.dto.FraudDetailDTO;
import kdt.project.fds.entity.Account;
import kdt.project.fds.entity.FraudDetectionResult;
import kdt.project.fds.entity.Transaction;
import kdt.project.fds.repository.AccountRepository;
import kdt.project.fds.repository.FraudRepository;
import kdt.project.fds.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FraudService {

    private final FraudRepository fraudRepository;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    // 순환 참조 방지를 위해 Service 대신 Processor를 직접 쓰거나,
    // 여기서는 간단히 TransactionService를 주입받아 처리 (위임)
    private final TransactionService transactionService;

    /**
     * 모든 탐지 이력 조회
     */
    @Transactional(readOnly = true)
    public List<FraudDetailDTO> getAllFraudHistory() {
        return fraudRepository.findAllWithDetails();
    }

    /**
     * 탐지 상태 변경 (사기 <-> 정상)
     * 정상이 되면 송금 재시도(승인) 로직까지 수행
     */
    @Transactional
    public void updateFraudStatus(Long id, Integer isFraud) {
        // id가 txId라고 가정하고 조회
        FraudDetectionResult result = fraudRepository.findByTxId(id)
                .orElseGet(() -> fraudRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("기록을 찾을 수 없습니다.")));

        result.setIsFraud(isFraud);
        fraudRepository.save(result);

        // '정상(0)'으로 변경 시 -> 송금 실행 (관리자 승인과 동일 효과)
        if (isFraud == 0) {
            Transaction tx = transactionRepository.findById(result.getTxId())
                    .orElseThrow(() -> new RuntimeException("원본 거래 기록이 없습니다."));

            // TransactionService의 다리 메서드를 통해 Processor 호출
            transactionService.executeTransfer(tx);
            log.info("✅ 탐지 상태 해제 및 송금 완료 - TX_ID: {}", tx.getTxId());
        }
    }
}