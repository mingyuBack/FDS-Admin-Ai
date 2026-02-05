package kdt.project.fds.controller;

import kdt.project.fds.entity.Transaction;
import kdt.project.fds.repository.FraudRepository;
import kdt.project.fds.repository.TransactionRepository;
import kdt.project.fds.service.TransactionService; // 새로 만든 서비스 주입
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    //  TransactionService를 부릅니다.
    private final TransactionService transactionService;
    private final FraudRepository fraudRepository;
    private final TransactionRepository transactionRepository;
    @PostMapping
    public ResponseEntity<?> createTransaction(@RequestBody Transaction tx) {
        log.info("새로운 거래 요청 수신 - 사용자: {}, 출처: {}, 금액: {}",
                tx.getUserId(), tx.getSourceValue(), tx.getTxAmount());

        try {
            // [중요] 3단계 필터 로직이 담긴 서비스 호출
            Transaction result = transactionService.processTransfer(tx);

            // 결과 응답 구성
            // 서비스 내부에서 격리된 경우(20만원 초과 등)와 즉시 성공한 경우를 포괄하는 메시지
            return ResponseEntity.ok(Map.of(
                    "status", "PROCESSED",
                    "txId", result.getTxId(),
                    "amount", result.getTxAmount(),
                    "message", "거래 요청이 접수되었습니다. 고액 또는 의심 거래는 승인 대기 목록에서 확인 가능합니다."
            ));

        } catch (RuntimeException e) {
            log.warn("거래 거절 - 사유: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "FAIL",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("시스템 오류 발생: ", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "ERROR",
                    "message", "서버 내부 오류가 발생했습니다."
            ));
        }
    }



       // 모든 거래 내역 조회
//        @GetMapping("/history")
//        public ResponseEntity<List<Map<String, Object>>> getAllHistory() {
//            List<Transaction> history = transactionRepository.findAllWithUserOrderByTxTimestampDesc();
//
//            List<Map<String, Object>> result = history.stream().map(t -> {
//                Map<String, Object> map = new HashMap<>();
//                map.put("txId", t.getTxId());
//                map.put("userId", t.getUserId());
//                // 조인된 User 엔티티에서 실제 이름을 가져옴
//                map.put("userName", t.getUser() != null ? t.getUser().getUserName() : "미등록");
//                map.put("sourceValue", t.getSourceValue());
//                map.put("targetValue", t.getTargetValue());
//                map.put("txAmount", t.getTxAmount());
//                map.put("txTimestamp", t.getTxTimestamp());
////                map.put("isFraud", 0); // 필요 시 서비스 로직에서 실제 값 세팅
//                int dbIsFraud = fraudRepository.findByTxId(t.getTxId())
//                        .map(res -> res.getIsFraud())
//                        .orElse(0); // 결과가 없으면 정상(0), 있으면 DB 값(1)
//
//                map.put("isFraud", dbIsFraud);
//                return map;
//            }).collect(Collectors.toList());
//
//        return ResponseEntity.ok(result);
//    }

    // [추가] 특정 거래 기록 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTransaction(@PathVariable("id") Long id) {
        // 1. 요청이 오는지 확인하는 로그 (매우 중요!)
        System.out.println(">>> 삭제 요청 수신! ID: " + id);

        try {
            if (!transactionRepository.existsById(id)) {
                System.out.println(">>> 삭제 실패: 존재하지 않는 ID");
                return ResponseEntity.badRequest().body("이미 삭제되었거나 존재하지 않는 기록입니다.");
            }
            transactionService.deleteTransactionData(id);
            System.out.println(">>> 삭제 완료! ID: " + id);
            return ResponseEntity.ok("성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("삭제 중 서버 오류: " + e.getMessage());
        }
    }
}