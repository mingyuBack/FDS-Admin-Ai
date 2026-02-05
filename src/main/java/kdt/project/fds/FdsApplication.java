package kdt.project.fds;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling // 이게 있어야 자동화
@SpringBootApplication
public class FdsApplication {

    public static void main(String[] args) {

        SpringApplication.run(FdsApplication.class, args);
    }

}
