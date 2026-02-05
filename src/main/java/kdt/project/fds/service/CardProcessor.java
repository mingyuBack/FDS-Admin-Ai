package kdt.project.fds.service;

import kdt.project.fds.entity.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class CardProcessor implements PaymentProcessor {

    // [설정] 카드사에서 자체적으로 관리하는 '위험 가맹점' 키워드 (AI와 별개)
    private static final List<String> RISKY_STORES = Arrays.asList("CASINO", "GAMBLING", "ADULT_NO_1", "ILLEGAL");

    @Override
    public Transaction execute(Transaction tx) {
        log.info("💳 [CardProcessor] 카드 결제 비즈니스 로직 진입 - TX_ID: {}", tx.getTxId());

        // =========================================================
        // [시뮬레이션 구간] 나중에 껍데기만 남길 때는 이 아래를 지우거나 주석 처리하세요.
        // =========================================================

        // 1. 최소 결제 금액 체크 (카드사 정책 시뮬레이션)
        if (tx.getTxAmount() < 100) {
            log.error("❌ [결제 거절] 최소 결제 금액 미달: {}원", tx.getTxAmount());
            throw new RuntimeException("카드 결제는 최소 100원 이상이어야 합니다.");
        }

        // 2. 위험 가맹점 문자열 포함 여부 체크
        // (AI는 확률로 잡지만, 여기서는 규칙으로 '무조건' 잡는 로직)
        String storeName = tx.getTargetValue() != null ? tx.getTargetValue().toUpperCase() : "";
        boolean isRiskyStore = RISKY_STORES.stream().anyMatch(storeName::contains);

        if (isRiskyStore) {
            log.warn("🚨 [카드사 직권 차단] 위험 가맹점 결제 시도: {}", storeName);
            // 실제라면 거절 상태로 DB 업데이트 하거나 예외 발생
            throw new RuntimeException("해당 가맹점(" + storeName + ")은 카드사 정책상 결제가 차단되었습니다.");
        }

        // 3. 해외 결제 알림 (Location 정보 활용)
        if (tx.getLocation() != null && !"Korea".equalsIgnoreCase(tx.getLocation())) {
            log.info("✈️ [해외 결제 승인] 국가: {}, 금액: {}원", tx.getLocation(), tx.getTxAmount());
        }

        // 4. 최종 승인 처리
        // (카드는 계좌 잔액을 즉시 차감하지 않고, 신용 공여(Credit)를 하므로 잔액 변경 로직은 없음)
        log.info("✅ [카드 결제 성공] 가맹점: {}, 승인금액: {}원", tx.getTargetValue(), tx.getTxAmount());

        // =========================================================
        // [시뮬레이션 구간 끝]
        // =========================================================

        return tx;
    }
}