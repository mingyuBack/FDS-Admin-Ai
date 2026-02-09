# FDS-Admin-Ai
🏗️ System Architecture & MLOps Strategy
본 프로젝트는 AI 기술이 실제 서비스 환경에서 안전하게 운영될 수 있도록 엔지니어링적 고가용성을 확보하는 데 집중했습니다
서비스 효율성을 위해 마이크로서비스 아키텍처(MSA) 구조로 설계되었습니다.

* **AI Inference Server (현재 레포):** Flask 기반의 실시간 사기 탐지 API 서버
* **Admin & Analysis Dashboard ([FDS-Admin-Ai 바로가기](https://github.com/mingyuBack/FDS-AI-Server)):** 모델 학습 및 재학습/ 모델 운용

1. 마이크로서비스 아키텍처 (MSA) 지향 


관심사의 분리: 비즈니스 로직을 처리하는 **Spring Boot(Web/Admin)**와 고중량 AI 연산을 수행하는 **Flask(AI Engine)**를 물리적으로 분리하여 시스템 부하를 격리하고 장애 전파를 방지했습니다. 


데이터 정합성: @Transactional 기반의 로직 설계를 통해 블랙리스트 등록 및 설정 변경 시 금융 도메인에 필수적인 데이터 무결성을 보장합니다. 

2. 하이브리드 모델 운영 및 고가용성 전략 


이원화 모델 구조: 공공 데이터 기반의 Base Model과 운영 데이터 기반의 Latest Model을 병행 운용하여 모델 업데이트 중에도 탐지 공백이 발생하지 않도록 설계했습니다. 


Failover & Fallback: 최신 모델 엔진에 장애가 발생하거나 로딩에 실패할 경우, 즉시 원본(Base) 모델로 전환하여 멈추지 않는 서비스를 제공합니다. 


Champion Protection (MLOps): 재학습된 모델이 기존 모델보다 우수하거나 특정 임계치(정확도 85%)를 통과할 때만 Hot Swap 방식으로 교체 배포합니다. 

🛠 Tech Stack
Language: Java 17

Framework: Spring Boot 3.x (Spring Data JPA, Spring Web)


AI/MLOps: Python Flask, MLflow, AWS S3 

Database: Oracle


Architecture: Layered Architecture / MSA 

External Communication: REST Template (Connecting to Flask AI Server)

🚀 Key Features

실시간 탐지 및 정책 반영: 거래 요청 시 즉시 FDS 엔진이 개입하며, 관리자가 UI에서 **AI 탐지 임계치(Threshold)**를 조정하면 백엔드 설정 테이블에 즉시 반영되어 실시간 탐지 로직의 기준값이 동적으로 변경됩니다. 


동적 차단 (Dynamic Blocking): 신고가 누적된 계좌를 대시보드에서 관리자가 즉시 차단(AdminController)하여 추가 피해를 막습니다. 


Human-in-the-Loop 승인: AI가 판단하기 모호한 회색지대(Gray Zone) 거래를 '승인 대기' 상태로 분류하여, 관리자가 대시보드에서 최종 판단을 내릴 수 있는 프로세스를 구축했습니다. 


대규모 데이터 학습 및 최적화: 약 68만 건의 대규모 트랜잭션 데이터를 기반으로 학습을 수행했으며, 복잡한 Join 쿼리와 Fetch Join을 활용해 데이터 조회 성능을 최적화했습니다. 

🧱 Domain Layer (Entity)
1. Transaction (거래 원장)

Table: TRANSACTIONS 


주요 필드: txId (PK), txAmount, sourceValue/targetValue, txType 


Relationships: User (ManyToOne) - Lazy Loading 

2. Account (계좌)

Table: ACCOUNTS 


주요 필드: accountNum (Unique), balance, status (ACTIVE/BLOCKED) 

3. FraudDetectionResult (탐지 결과)

Table: FRAUD_DETECTION_RESULTS 


주요 필드: isFraud (0:정상, 1:사기), probability, engine 

4. BlacklistAccount (차단 목록)

Table: BLACKLIST_ACCOUNTS 


주요 필드: accountNum, reason (차단 사유) 

💾 Data Access Layer (Repository & DTO)
1. TransactionRepository
findAllWithUserOrderByTxTimestampDesc(): FETCH JOIN을 사용하여 N+1 문제를 방지하고 성능을 최적화했습니다.

2. FraudRepository
findAllWithDetails(): Transaction, User, TransactionFeature를 Left Join하여 대시보드용 상세 데이터를 조회합니다.

🧠 Service Layer Architecture 
1. TransactionService (Core)
거래 요청 시 1차 로그 기록 및 3단계 필터링 파이프라인 실행.

블랙리스트 확인 -> 자동 승인 한도 체크 -> AI/Rule 기반 사기 탐지 위임. 

2. DetectionService (Engine)

Rule Engine: 고정 규칙(고액, 심야 등) 검사. 


AI Model: Flask API를 통해 실시간 사기 확률 조회 및 Champion Protection 기반 배포 검증. 

3. AdminService (Management) -

승인/거절: 보류된 거래 처리 및 수취인 계좌 블랙리스트 등록. 
시스템 장애 대비: 장애 상황을 고려한 Fail-Safe 설계 및 예외 처리

<img width="667" height="168" alt="image" src="https://github.com/user-attachments/assets/f8fb0d9b-9b7b-418b-aefe-98f3f0c5586c" />
