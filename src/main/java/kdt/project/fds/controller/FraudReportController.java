package kdt.project.fds.controller;

import kdt.project.fds.entity.FraudReport;
import kdt.project.fds.entity.Transaction;
import kdt.project.fds.repository.FraudReportRepository;
import kdt.project.fds.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class FraudReportController {

    private final FraudReportRepository reportRepository;
    private final TransactionRepository transactionRepository; // [추가] 거래 조회용 리포지토리

    // 사유 코드 매핑
    private static final Map<Integer, String> REASON_MAP = Map.of(
            1, "보이스피싱 의심",
            2, "대포통장 의심",
            3, "중고거래 사기",
            4, "검찰/기관 사칭",
            5, "가족/지인 사칭 메신저 피싱"
    );

    // 신고 랭킹 조회 API
    @GetMapping("/ranking")
    public ResponseEntity<List<FraudReport>> getReportRanking() {
        List<FraudReport> ranking = reportRepository.findAllByOrderByReportCountDesc();
        return ResponseEntity.ok(ranking);
    }

    // 신고 접수 API
    @PostMapping
    public ResponseEntity<?> createReport(@RequestBody Map<String, Object> payload) {
        try {
            // 1. 프론트엔드에서 보낸 'transactionId' 추출
            Object txIdObj = payload.get("transactionId");
            if (txIdObj == null) {
                return ResponseEntity.badRequest().body("거래 ID(transactionId)가 필요합니다.");
            }
            Long txId = Long.parseLong(String.valueOf(txIdObj));
            // 2. 거래 ID로 원본 거래 내역 조회 (계좌번호를 알기 위함)
            Transaction tx = transactionRepository.findById(txId)
                    .orElseThrow(() -> new RuntimeException("해당 거래 내역을 찾을 수 없습니다."));
            // 3. 거래 내역에서 수취인 계좌번호(targetValue) 추출
            String account = tx.getTargetValue();
            // ---------------------------------------------------------
            String inputReason = (String) payload.get("reason");
            // [사유 코드 매핑 로직]
            int code = 0;
            String mappedReason = inputReason;

            if (inputReason != null && inputReason.matches("\\d+")) {
                int inputCode = Integer.parseInt(inputReason);
                if (REASON_MAP.containsKey(inputCode)) {
                    code = inputCode;
                    mappedReason = REASON_MAP.get(inputCode);
                }
            } else if (inputReason != null) {
                if (inputReason.contains("보이스")) code = 1;
                else if (inputReason.contains("대포")) code = 2;
            }
            // [신고 저장/누적 로직]
            Optional<FraudReport> existingReport = reportRepository.findByReportedAccount(account);
            if (existingReport.isPresent()) {
                // 이미 신고된 계좌 -> 횟수 증가
                FraudReport report = existingReport.get();
                int newCount = report.getReportCount() + 1;
                report.setReportCount(newCount);
                report.setReason(report.getReason() + " / " + mappedReason);
                report.setReasonCode(code);
                report.setCreatedAt(LocalDateTime.now());
                reportRepository.save(report);

                log.info("🚨 신고 누적 - 계좌: {}, 누적횟수: {}", account, newCount);
                return ResponseEntity.ok("신고가 누적되었습니다. (현재 " + newCount + "회 신고됨)");
            } else {
                // 첫 신고 -> 신규 생성
                FraudReport report = FraudReport.builder()
                        .reportedAccount(account) // DB에서 찾은 계좌번호 저장
                        .reason(mappedReason)
                        .reasonCode(code)
                        .reporterId(1L)
                        .reportCount(1)
                        .status("PENDING")
                        .build();
                reportRepository.save(report);
                log.info("🚨 신규 신고 접수 - 계좌: {}", account);
                return ResponseEntity.ok("신고가 접수되었습니다. (1회)");
            }
        } catch (Exception e) {
            log.error("신고 에러", e);
            return ResponseEntity.internalServerError().body("오류 발생: " + e.getMessage());
        }
    }
}