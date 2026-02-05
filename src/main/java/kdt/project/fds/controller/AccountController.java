package kdt.project.fds.controller;

import kdt.project.fds.dto.AccountRequestDTO;
import kdt.project.fds.entity.Account;
import kdt.project.fds.entity.User;
import kdt.project.fds.repository.AccountRepository;
import kdt.project.fds.repository.UserRepository; // 유저 확인용 추가
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository; // 계좌 생성 시 유저 존재 여부 확인용

    /**
     * 전체 계좌 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<Account>> getAllAccounts() {
        log.info("모든 계좌 목록 조회 요청 수신");
        List<Account> accounts = accountRepository.findAll();
        return ResponseEntity.ok(accounts);
    }

    /**
     * 계좌 생성 (Postman 테스트용)
     * 이제 userName 대신 userId(String)를 받아와야 합니다.
     */
    @PostMapping
    public ResponseEntity<?> createAccount(@RequestBody AccountRequestDTO dto) {
        log.info("계좌 생성 요청 - 사용자 ID: {}, 계좌번호: {}", dto.getUserId(), dto.getAccountNum());

        // 1. 실제 존재하는 유저인지 확인 (영문 아이디)
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        // 2. 계좌 엔티티 설정
        Account account = new Account();
        account.setUser(user); // 연관관계 매핑
        account.setAccountNum(dto.getAccountNum());
        account.setBalance(dto.getBalance());

        Account savedAccount = accountRepository.save(account);
        return ResponseEntity.status(201).body(savedAccount);
    }
}