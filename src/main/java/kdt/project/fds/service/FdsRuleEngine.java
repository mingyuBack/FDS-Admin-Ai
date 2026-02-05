package kdt.project.fds.service;

import kdt.project.fds.entity.Transaction;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class FdsRuleEngine {

    /**
     * 규칙 기반 탐지 (AI 호출 전 실행)
     * @return 사기 의심 사유 (정상이면 null)
     */
    public String evaluateRules(Transaction tx) {

        // 1. 고액 거래 규칙 (예: 5,000만원 이상)
        if (tx.getTxAmount() >= 50000000) {
            return "RULE: HIGH_AMOUNT_LIMIT";
        }

        // 2. 심야 고액 거래 규칙 (00시 ~ 05시 사이 300만원 이상)
        int hour = LocalDateTime.now().getHour();
        if ((hour >= 0 && hour <= 5) && tx.getTxAmount() >= 1000000) {
            return "RULE: NIGHT_SUSPICIOUS_TRANSFER";
        }

        // 3. (추가 가능) 특정 위험 지역 거래 등...

        return null; // 통과
    }
}