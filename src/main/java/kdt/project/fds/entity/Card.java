package kdt.project.fds.entity;

import jakarta.persistence.*;
import kdt.project.fds.entity.User;
import lombok.Getter;
import lombok.Setter;

// Card.java
@Entity
@Table(name = "CARDS", schema = "FDS_ADMIN")
@Getter
@Setter
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "card_seq")
    @SequenceGenerator(name = "card_seq", sequenceName = "FDS_ADMIN.SEQ_CARD_ID", allocationSize = 1)
    private Long cardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user; // 주인 정보

    private String cardNum;
    private String cardType;
    private String issuer;
}