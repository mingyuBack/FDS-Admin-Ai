package kdt.project.fds.service;

import kdt.project.fds.entity.Account;
import kdt.project.fds.entity.FraudDetectionResult;
import kdt.project.fds.entity.Transaction;
import kdt.project.fds.repository.AccountRepository;
import kdt.project.fds.repository.FraudRepository; // [수정] 올바른 리포지토리 import
import kdt.project.fds.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetrainService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    // [수정] 신고/블랙리스트 리포지토리 대신 결과 테이블 리포지토리 주입
    private final FraudRepository fraudRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String AI_RETRAIN_URL = "http://localhost:5000/api/retrain";

    public String triggerRetraining() {
        log.info("🔄 [고도화] AI 모델 재학습 데이터 수집 시작 (FRAUD_DETECTION_RESULTS 테이블 기반)...");

        List<Transaction> allTxs = transactionRepository.findAll();
        List<Map<String, Object>> trainingData = new ArrayList<>();

        int fraudCount = 0;

        for (Transaction tx : allTxs) {
            // --- [수정됨] 정답 라벨링 로직 (FraudRepository 이용) ---
            int isFraud = 0; // 기본값: 데이터가 없거나 0이면 정상

            // FraudRepository에 정의된 findByTxId 사용
            // Transaction 객체의 txId를 꺼내서 조회
            Optional<FraudDetectionResult> resultOpt = fraudRepository.findByTxId(tx.getTxId());

            if (resultOpt.isPresent()) {
                FraudDetectionResult result = resultOpt.get();
                // 저장된 결과값(isfraud)을 그대로 가져옴 (1: 사기, 0: 정상)
                isFraud = result.getIsFraud();
            }
            // --------------------------------------------------------

            // 학습 데이터 포장 (기존 로직 유지)
            Map<String, Object> row = new HashMap<>();
            row.put("amount", tx.getTxAmount());
            row.put("tx_type", tx.getTxType());

            double oldBal = accountRepository.findByAccountNum(tx.getSourceValue())
                    .map(Account::getBalance).orElse(0.0);
            row.put("old_bal", oldBal);

            // 정답지 (Label)
            row.put("is_fraud", isFraud);

            trainingData.add(row);

            if (isFraud == 1) fraudCount++;
        }

        log.info("📊 수집된 데이터: 총 {}건 (사기/위험 판정: {}건)", trainingData.size(), fraudCount);

        if (trainingData.size() < 10) {
            return "데이터 부족: 학습을 위해 최소 10건 이상의 데이터가 필요합니다.";
        }

        // Python 서버로 전송
        try {
            // 응답을 Map으로 받습니다.
            Map<String, Object> response = restTemplate.postForObject(AI_RETRAIN_URL, trainingData, Map.class);

            if (response != null) {
                String status = (String) response.get("status");
                String message = (String) response.get("message");

                // [수정] Python이 보내준 정확도와 버전을 꺼냅니다.
                // 숫자형은 바로 cast하면 에러 날 수 있으니 안전하게 String.valueOf 사용
                String accuracy = String.valueOf(response.get("accuracy"));
                String version = (String) response.get("version");

                if ("success".equals(status)) {
                    log.info("✅ 재학습 완료!");
                    log.info("📈 모델 정확도: {}", accuracy);
                    log.info("🕒 모델 버전: {}", version);
                    log.info("💬 상세 메시지: {}", message);

                    // 화면에 보여줄 메시지 구성
                    return String.format("재학습 성공! (정확도: %s, 버전: %s) - %s", accuracy, version, message);
                } else {
                    log.warn("⚠️ 재학습 수행했으나 모델 미교체: {}", message);
                    return "재학습 미반영: " + message;
                }
            }
        } catch (Exception e) {
            log.error("❌ 재학습 요청 중 통신 에러", e);
            return "재학습 요청 실패: " + e.getMessage();
        }

        return "재학습 실패 (응답 없음)";
    }
}