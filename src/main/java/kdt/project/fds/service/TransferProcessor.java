package kdt.project.fds.service;

import kdt.project.fds.entity.Account;
import kdt.project.fds.entity.Transaction;
import kdt.project.fds.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferProcessor implements PaymentProcessor {

    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public Transaction execute(Transaction tx) {
        // 1. 송금인 계좌 조회
        Account sender = accountRepository.findByAccountNum(tx.getSourceValue())
                .orElseThrow(() -> new RuntimeException("송금인 계좌가 존재하지 않습니다."));

        // 2. 송금 실행 (잔액 차감)
        sender.withdraw(tx.getTxAmount());
        accountRepository.save(sender);

        // 3. 수취인 입금 (수취인이 존재하는 경우에만)
        accountRepository.findByAccountNum(tx.getTargetValue()).ifPresent(receiver -> {
            receiver.setBalance(receiver.getBalance() + tx.getTxAmount());
            accountRepository.save(receiver);
        });

        log.info("💸 [계좌이체 프로세서] 이체 완료 TX_ID: {}, Amount: {}", tx.getTxId(), tx.getTxAmount());
        return tx;
    }
}