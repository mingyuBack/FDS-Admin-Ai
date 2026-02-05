package kdt.project.fds.service;

import kdt.project.fds.entity.Transaction;

public interface PaymentProcessor {
    /**
     * 결제 실행 (이체, 카드 결제 등)
     * @param tx 거래 정보
     * @return 처리 완료된 거래 정보
     * 미래 결제 수단 확장성을 고려해 생성
     */
    Transaction execute(Transaction tx);
}
