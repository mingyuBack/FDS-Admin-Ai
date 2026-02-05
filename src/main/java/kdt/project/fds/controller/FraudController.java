package kdt.project.fds.controller;

import kdt.project.fds.dto.FraudDetailDTO;
import kdt.project.fds.service.FraudService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/fraud")
@RequiredArgsConstructor
public class FraudController {

    // [변경] 오직 서비스 하나만 의존합니다. (Repository 주입 제거)
    private final FraudService fraudService;

    @GetMapping("/all")
    public ResponseEntity<List<FraudDetailDTO>> getAllHistory() {
        return ResponseEntity.ok(fraudService.getAllFraudHistory());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<String> updateStatus(
            @PathVariable("id") Long id,
            @RequestParam("isFraud") Integer isFraud) {

        // 로직은 서비스에게 전적으로 위임
        fraudService.updateFraudStatus(id, isFraud);
        return ResponseEntity.ok("SUCCESS");
    }
}