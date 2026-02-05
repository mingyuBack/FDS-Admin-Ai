package kdt.project.fds.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id; // 반드시 이걸 써야 합니다.
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "TRANSACTION_FEATURES", schema = "FDS_ADMIN")
@Getter
@Setter
@NoArgsConstructor
public class TransactionFeature {

    @Id // 이 필드가 PK임을 선언
    @Column(name = "TX_ID")
    private Long txId;

    @Column(name = "OLD_BALANCE_ORG")
    private Double oldBalanceOrg;

    @Column(name = "NEW_BALANCE_ORG")
    private Double newBalanceOrg;

    @Lob
    @Column(name = "V_FEATURES")
    private String vFeatures;
}