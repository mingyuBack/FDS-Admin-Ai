package kdt.project.fds.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "USERS", schema = "FDS_ADMIN")
@Getter @Setter
public class User {
    @Id
    @Column(name = "USER_ID")
    private String userId; // 이제 String(문자열) 입니다!

    @Column(name = "USER_PW", nullable = false)
    private String userPw;

    @Column(name = "USER_NAME", nullable = false)
    private String userName;

    @Column(name = "USER_EMAIL")
    private String userEmail;

    @Column(name = "GENDER")
    private String gender;

    @Column(name = "BIRTH")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birth; // 날짜 타입

    @Column(name = "PHONE")
    private String phone;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}