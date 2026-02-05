package kdt.project.fds.scheduler;

import kdt.project.fds.service.RetrainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ModelRetrainScheduler {

    private final RetrainService retrainService;

    // 🕒 Cron 표현식: "초 분 시 일 월 요일"
    // "0 0 0 * * *" -> 매일 밤 자정(00:00:00) 실행
    // "0 0/5 * * * *" -> 테스트용: 5분마다 실행 (테스트할 때 쓰세요)
    @Scheduled(cron = "0 0 0 * * *" )
    public void autoRetrainJob() {
        log.info("🌙 [Batch] 야간 정기 AI 모델 재학습 작업을 시작합니다...");

        try {
            String result = retrainService.triggerRetraining();
            log.info("🌞 [Batch] 재학습 작업 종료. 결과: {}", result);
        } catch (Exception e) {
            log.error("💥 [Batch] 재학습 중 오류 발생", e);
        }
    }
}