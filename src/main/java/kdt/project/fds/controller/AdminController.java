package kdt.project.fds.controller;

import kdt.project.fds.entity.BlacklistAccount;
import kdt.project.fds.service.AdminService;
import kdt.project.fds.service.BlacklistService; // 추가됨
import kdt.project.fds.service.RetrainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final BlacklistService blacklistService; // [추가]
    private final RetrainService retrainService;
    // 1. 블랙리스트 목록 조회 -> BlacklistService 담당
    @GetMapping("/blacklist")
    public ResponseEntity<List<BlacklistAccount>> getBlacklist() {
        return ResponseEntity.ok(blacklistService.getAllBlacklist());
    }

    // 2. 수동 차단 -> BlacklistService 담당
    @PostMapping("/blacklist")
    public ResponseEntity<?> addToBlacklist(@RequestBody Map<String, String> payload) {
        try {
            String accountNum = payload.get("accountNum");
            String reason = payload.get("reason");
            String msg = blacklistService.addToBlacklist(accountNum, reason);
            return ResponseEntity.ok(msg);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("오류: " + e.getMessage());
        }
    }

    // 3. 차단 해제 -> BlacklistService 담당
    @DeleteMapping("/blacklist/{accountNum}")
    public ResponseEntity<String> removeBlacklist(@PathVariable("accountNum") String accountNum) {
        try {
            String msg = blacklistService.removeBlacklist(accountNum);
            return ResponseEntity.ok(msg);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 4. 승인/거절 -> AdminService 담당 (기존 유지)
    @PostMapping("/approve/{id}")
    public ResponseEntity<String> approveTransaction(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(adminService.approveTransaction(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reject/{id}")
    public ResponseEntity<String> rejectTransaction(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(adminService.rejectTransaction(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/retrain")
    public ResponseEntity<String> retrainModel() {
        log.info("🖱️ 관리자 페이지에서 '재학습' 요청이 들어왔습니다.");

        // 서비스 호출
        String resultMessage = retrainService.triggerRetraining();

        return ResponseEntity.ok(resultMessage);
    }


    // (로그인 메서드는 생략 - 기존 유지)
    //관리자 로그인 검증 API

    @PostMapping("/login")

    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {

        String username = loginData.get("username");

        String password = loginData.get("password");



        // 실제로는 DB의 Admin 테이블과 비교해야 하지만, 데모용으로 하드코딩합니다.
        if ("admin".equals(username) && "1234".equals(password)) {
            // 로그인 성공
            return ResponseEntity.ok(Map.of("message", "Login Success", "token", "admin-token-12345"));

        } else {
            // 로그인 실패
            return ResponseEntity.status(401).body("아이디 또는 비밀번호가 잘못되었습니다.");

        }
    }
}