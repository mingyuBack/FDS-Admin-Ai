package kdt.project.fds.service;

import kdt.project.fds.entity.Transaction;
import kdt.project.fds.repository.AccountRepository;
import kdt.project.fds.repository.FdsConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DetectionService {

    private final FdsResultService resultService;
    private final FdsRuleEngine ruleEngine;
    private final FdsConfigRepository configRepository;
    private final AccountRepository accountRepository;

    // RestTemplate은 필드에서 생성하거나 Bean 주입 권장
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String FLASK_URL = "http://localhost:5000/api/predict";

    // [중요] DB에 저장된 설정 키와 정확히 일치해야 합니다.
    private static final String KEY_THRESHOLD = "THRESHOLD";
    private static final String KEY_AUTO_LIMIT = "AUTO_LIMIT";

    public int detectAndSave(Transaction tx) {
        log.info("🛡️ 탐지 프로세스 시작 - TX_ID: {}, 금액: {}", tx.getTxId(), tx.getTxAmount());

        // =================================================================
        // [핵심 1] 설정값부터 먼저 조회 (어떤 경우에도 기록하기 위해)
        // =================================================================
        double threshold = Double.parseDouble(configRepository.findById(KEY_THRESHOLD)
                .map(c -> c.getConfigValue()).orElse("0.7")); // 기본값

        long autoLimit = Long.parseLong(configRepository.findById(KEY_AUTO_LIMIT)
                .map(c -> c.getConfigValue()).orElse("100000")); // 기본값

        // =================================================================
        //  Rule 엔진 체크
        String ruleViolation = ruleEngine.evaluateRules(tx);
        if (ruleViolation != null) {
            String reason = mapReasonToKorean(ruleViolation);

            // [기록] 0.0이 아니라, 위에서 가져온 'threshold'를 저장합니다.
            resultService.saveAiResult(tx, 1.0, threshold, 1, "[Rule] " + reason);
            log.warn("⛔ 정책 기반 즉시 차단: {}", reason);
            return 1; // 승인 대기(차단)
        }
        // =================================================================
        //  AI 판정 및 금액 한도 체크
        try {
            double currentBalance = accountRepository.findByAccountNum(tx.getSourceValue())
                    .map(acc -> acc.getBalance()).orElse(0.0);

            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("user_id", tx.getUserId());
            requestMap.put("amount", tx.getTxAmount());
            requestMap.put("location", tx.getLocation() != null ? tx.getLocation() : "Unknown"); // null 방지
            requestMap.put("old_bal", currentBalance);
            requestMap.put("tx_type", tx.getTxType());

            // AI 서버 호출
            Map response = restTemplate.postForObject(FLASK_URL, requestMap, Map.class);

            if (response != null && "success".equals(response.get("status"))) {
                Double probability = Double.valueOf(response.get("probability").toString());
                String flaskEngine = response.get("engine").toString();

                // --- [판단 로직] ---
                boolean isAiSafe = probability < threshold;      // 관문 2: AI 확률 통과?
                boolean isAmountSafe = tx.getTxAmount() <= autoLimit; // 관문 3: 금액 한도 통과?

                int finalDecision;
                String decisionReason;

                if (isAiSafe && isAmountSafe) {
                    // [모두 통과] -> 자동 승인
                    finalDecision = 0;
                    decisionReason = flaskEngine + " (정상 승인)";
                } else {
                    // [하나라도 실패] -> 승인 대기
                    finalDecision = 1;
                    if (!isAiSafe) {
                        decisionReason = flaskEngine + " (위험도 높음)";
                    } else {
                        decisionReason = flaskEngine + " (AI 안전하나 금액 한도 초과)";
                    }
                }

                // [기록] 여기서도 threshold 값이 정확히 들어갑니다.
                resultService.saveAiResult(tx, probability, threshold, finalDecision, decisionReason);

                return finalDecision; // 0이면 자동승인, 1이면 대기
            }

        } catch (Exception e) {
            log.error("AI 서버 에러: {}", e.getMessage());
            // 에러 발생 시에도 설정값(threshold)은 기록하고 '승인 대기(1)' 처리
            resultService.saveAiResult(tx, 0.0, threshold, 1, "[System] AI 서버 오류");
            return 1;
        }

        // 예외적인 경우 기본 차단
        return 1;
    }

    /**
     * 필터/블랙리스트 차단 시 호출 (TransactionService에서 사용)
     * 여기서는 임계치를 조회하지 않고 0.0이나 1.0으로 처리해도 되지만,
     * 일관성을 위해 0.0으로 두거나 필요시 조회 로직을 넣을 수 있습니다.
     */
    public void saveFilterResult(Transaction tx, String reason) {
        resultService.saveAiResult(tx, 1.0, 0.0, 1, "[Blacklist] " + reason);
    }

    private String mapReasonToKorean(String violation) {
        if (violation.contains("HIGH_AMOUNT")) return "고액 거래 (규칙 위반)";
        if (violation.contains("NIGHT")) return "심야 의심 거래";
        return "보안 정책 위반: " + violation;
    }
}