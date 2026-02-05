package kdt.project.fds.repository;

import kdt.project.fds.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자 정보 관리를 위한 리포지토리
 * ID 타입이 String(문자열 아이디)임에 유의하세요.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    // 이메일로 사용자 찾기 (필요 시)
    Optional<User> findByUserEmail(String userEmail);

    // 이름으로 사용자 존재 여부 확인 (필요 시)
    boolean existsByUserName(String userName);
}