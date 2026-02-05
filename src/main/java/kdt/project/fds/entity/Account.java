package kdt.project.fds.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ACCOUNTS", schema = "FDS_ADMIN")
@Getter
@Setter
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "acc_seq")
    @SequenceGenerator(name = "acc_seq", sequenceName = "FDS_ADMIN.SEQ_ACCOUNT_ID", allocationSize = 1)
    private Long accountId;

    @ManyToOne(fetch = FetchType.EAGER) // JS 화면 표시를 위해 EAGER로 변경 권장
    @JoinColumn(name = "USER_ID")
    private User user;

    @Column(name = "ACCOUNT_NUM")
    private String accountNum;

    private Double balance;

    /**
     * [비즈니스 로직] 출금 처리
     * 잔액이 부족하면 예외를 던져 트랜잭션을 롤백시킵니다.
     */
    public void withdraw(Double amount) {
        if (amount <= 0) {
            throw new RuntimeException("출금 금액은 0보다 커야 합니다.");
        }
        if (this.balance < amount) {
            throw new RuntimeException("잔액이 부족합니다. (현재 잔액: " + this.balance + ")");
        }
        this.balance -= amount;
    }
}