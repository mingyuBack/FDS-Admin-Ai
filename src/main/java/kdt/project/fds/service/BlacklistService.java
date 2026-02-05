package kdt.project.fds.service;

import kdt.project.fds.entity.BlacklistAccount;
import kdt.project.fds.repository.BlacklistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlacklistService {

    private final BlacklistRepository blacklistRepository;

    // 1. 목록 조회
    @Transactional(readOnly = true)
    public List<BlacklistAccount> getAllBlacklist() {
        return blacklistRepository.findAll();
    }

    // 2. 추가 (이미 있으면 에러 or 무시)
    @Transactional
    public String addToBlacklist(String accountNum, String reason) {
        if (blacklistRepository.existsByAccountNum(accountNum)) {
            // 이미 차단된 경우, 메시지만 리턴하거나 예외를 던질 수 있습니다.
            // 여기서는 중복 차단 시 에러 대신 로그만 남기고 성공 처리하는 유연한 방식을 택하겠습니다.
            log.info("이미 차단된 계좌입니다: {}", accountNum);
            return "이미 차단된 계좌입니다.";
        }
        BlacklistAccount blacklist = new BlacklistAccount();
        blacklist.setAccountNum(accountNum);
        blacklist.setReason(reason);
        blacklistRepository.save(blacklist);
        log.info("블랙리스트 등록 완료: {}", accountNum);
        return "계좌 [" + accountNum + "]가 블랙리스트에 추가되었습니다.";
    }
    // 3. 삭제
    @Transactional
    public String removeBlacklist(String accountNum) {
        return blacklistRepository.findByAccountNum(accountNum)
                .map(account -> {
                    blacklistRepository.delete(account);
                    log.info("블랙리스트 해제 완료: {}", accountNum);
                    return "계좌 [" + accountNum + "]의 차단이 해제되었습니다.";
                })
                .orElseThrow(() -> new RuntimeException("해당 계좌를 블랙리스트에서 찾을 수 없습니다."));
    }
    // 4. 단순 조회 (존재 여부)
    public boolean isBlocked(String accountNum) {
        return blacklistRepository.existsByAccountNum(accountNum);
    }
}