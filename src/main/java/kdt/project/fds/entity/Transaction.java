package kdt.project.fds.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "TRANSACTIONS", schema = "FDS_ADMIN")
@Getter
@Setter

public class Transaction {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID", insertable = false, updatable = false)
    private User user; // USERS 테이블과 조인
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tx_seq")
    @SequenceGenerator(name = "tx_seq", sequenceName = "FDS_ADMIN.SEQ_TX_ID", allocationSize = 1)
    private Long txId;

    @Column(name = "USER_ID")
    private String userId; // 조회 편의를 위해 ID로 바로 저장

    private String txType;    // TRANSFER, CARD_PAY
    private Long sourceId;    // ACCOUNT_ID 또는 CARD_ID
    private String sourceValue; // 화면에 보여줄 번호
    private String targetValue;
    private Double txAmount;
    private String merchantCat;
    private String location;
    private LocalDateTime txTimestamp;


}