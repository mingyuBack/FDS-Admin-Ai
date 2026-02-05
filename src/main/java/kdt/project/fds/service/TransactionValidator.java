package kdt.project.fds.service;

import kdt.project.fds.entity.Transaction;
import kdt.project.fds.repository.BlacklistRepository;
import kdt.project.fds.repository.FdsConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionValidator {

    private final BlacklistRepository blacklistRepository;
    private final FdsConfigRepository configRepository;
    private final DetectionService detectionService;

    /**
     * 거래 실행 전 사전 차단 여부를 검사합니다.
     * @return true(차단됨/중단), false(통과/계속진행)
     */
    public boolean shouldBlock(Transaction tx) {

        // 1. 블랙리스트 검사
        if (blacklistRepository.existsByAccountNum(tx.getTargetValue())) {
            log.warn("🚫 [검문소 차단] 블랙리스트 탐지: {}", tx.getTargetValue());
            detectionService.saveFilterResult(tx, "블랙리스트 계좌 탐지");
            return true; // 차단!
        }

        // 2. 자동 승인 한도 검사
        double autoApproveLimit = configRepository.findById("AUTO_LIMIT")
                .map(c -> Double.parseDouble(c.getConfigValue())).orElse(100000.0);

        if (tx.getTxAmount() > autoApproveLimit) {
            log.warn("⚠️ [검문소 격리] 자동 승인 한도 초과: {}원 (한도: {}원)", tx.getTxAmount(), autoApproveLimit);
            detectionService.saveFilterResult(tx, "자동 승인 한도 초과");
            return true; // 차단(격리)!
        }

        return false; // 통과!
    }
}