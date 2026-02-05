package kdt.project.fds.repository;

import kdt.project.fds.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    // 1. 매개변수 이름은 상관없지만 필드명인 AccountNum과 일치시킴
    Optional<Account> findByAccountNum(String accountNum);

    /**
     * 2. 사용자 이름으로 존재 여부 확인 (수정됨)
     * Account 엔티티 안의 User 객체 내부의 userName을 찾아야 하므로
     * 'User_UserName' 또는 'UserUserName' 형식을 사용해야 합니다.
     */
    boolean existsByUser_UserName(String userName);
}