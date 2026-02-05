package kdt.project.fds.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 실무형 FraudDetectionResult 엔티티
 * 탐지 결과 기록의 불변성을 보장하기 위해 빌더 패턴을 적용합니다.
 */
@Entity
@Table(name = "FRAUD_DETECTION_RESULTS", schema = "FDS_ADMIN")
@Getter
@Setter // 데이터 수정이 필요한 경우를 위해 유지 (예: 수동 판정 업데이트)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FraudDetectionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "det_seq")
    @SequenceGenerator(
            name = "det_seq",
            sequenceName = "FDS_ADMIN.SEQ_DETECTION_ID",
            allocationSize = 1
    )
    @Column(name = "DETECTION_ID")
    private Long id;

    @Column(name = "TX_ID", nullable = false)
    private Long txId;

    @Column(name = "FRAUD_PROBABILITY", nullable = false)
    private Double probability;

    @Column(name = "IS_FRAUD", nullable = false)
    private Integer isFraud; // 0: 정상, 1: 사기

    @Column(name = "DETECTED_ENGINE", length = 50)
    private String engine;

    // 실무 팁: 상태 변경을 명확하게 하는 비즈니스 메서드
    public void updateManualDecision(boolean fraud) {
        this.isFraud = fraud ? 1 : 0;
    }
    @Column(name = "THRESHOLD_VALUE", nullable = false)
    private Double thresholdValue; // 판정 당시의 기준치 저장
    //DDL에 있는 날짜 컬럼 매핑 (이게 없으면 언제 탐지했는지 모릅니다)
    @Column(name = "DETECTED_AT")
    private LocalDateTime detectedAt;
    // [추가] DB에서 직접 조인해서 가져오기 위한 설정
    // JoinColumn을 사용하거나, 아래와 같이 쿼리에서 처리할 수 있도록 필드를 유지합니다.

    @Transient // DB 컬럼은 아니지만 화면 전달용으로 사용
    private String userId;

    @Transient
    private String userName;
}