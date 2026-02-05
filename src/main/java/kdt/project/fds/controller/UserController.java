package kdt.project.fds.controller;

import kdt.project.fds.entity.User;
import kdt.project.fds.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users") // 이 경로가 있어야 /api/users 호출이 가능합니다.
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        log.info("유저 생성 요청 - ID: {}, 이름: {}", user.getUserId(), user.getUserName());

        // 유저 저장
        User savedUser = userRepository.save(user);

        return ResponseEntity.ok(savedUser);
    }
}