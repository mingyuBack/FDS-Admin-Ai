package kdt.project.fds.service;

//DB 저장, 트랜잭션 처리, 데이터 포맷팅(JSON)만 담당.

import kdt.project.fds.entity.FraudDetectionResult;
import kdt.project.fds.entity.Transaction;
import kdt.project.fds.entity.TransactionFeature;
import kdt.project.fds.repository.AccountRepository;
import kdt.project.fds.repository.FraudRepository;
import kdt.project.fds.repository.TransactionFeatureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FdsResultService {

    private final FraudRepository fraudRepository;
    private final TransactionFeatureRepository featureRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public void saveAiResult(Transaction tx, Double prob, Double threshold, Integer isFraud, String engineMsg) {

        // 1. 엔진 태그 결정 (카드 vs 계좌이체)
        String engineTag = "CARD".equals(tx.getTxType()) ? "[Engine_A]" : "[Engine_B]";
        String finalEngineName = engineTag + " " + engineMsg;

        // 2. 결과 저장 (사용자님 Entity 변수명에 맞춤)
        FraudDetectionResult result = FraudDetectionResult.builder()
                .txId(tx.getTxId())
                .probability(prob)        // fraudProbability -> probability
                .thresholdValue(threshold)
                .isFraud(isFraud)
                .engine(finalEngineName)  // detectedEngine -> engine
                .build();
        fraudRepository.save(result);

        // 3. 피처 저장 (JSON)
        try {
            double currentBal = accountRepository.findByAccountNum(tx.getSourceValue())
                    .map(acc -> acc.getBalance()).orElse(0.0);

            String jsonFeatures = String.format(
                    "{\"amount\": %.0f, \"old_bal\": %.0f, \"type\": \"%s\", \"loc\": \"%s\"}",
                    tx.getTxAmount(), currentBal, tx.getTxType(), tx.getLocation()
            );

            TransactionFeature feature = new TransactionFeature();
            feature.setTxId(tx.getTxId());
            feature.setOldBalanceOrg(currentBal);
            feature.setNewBalanceOrg(currentBal - tx.getTxAmount());
            feature.setVFeatures(jsonFeatures);

            featureRepository.save(feature);
            log.info("✅ FDS 저장 완료: {}", finalEngineName);

        } catch (Exception e) {
            log.error("❌ Feature 저장 실패: {}", e.getMessage());
        }
    }
}