# Moneython Backend

**Moneython**은 금융/경제 지식을 쉽고 재미있게 학습할 수 있도록 설계된 퀴즈 기반 교육 플랫폼입니다.  
이 레포지토리는 Moneython의 **백엔드 서비스**들을 MSA 아키텍처 기반으로 구현한 코드 저장소입니다.

### 🏗️ 아키텍처 개요
- **구성 방식**: Microservice Architecture (MSA)
- **서비스 등록/탐색**: Spring Cloud Eureka
- **설정 관리**: Spring Cloud Config
- **API 게이트웨이**: Spring Cloud Gateway
- **서비스 간 통신**: REST + Kafka (이벤트 기반)
- **배포 환경**: AWS EC2 + Docker + GitHub Actions (CI/CD)

### 🗂️ 서비스 구조
```
backend/
├── gateway-service/         # API Gateway
├── user-service/            # 회원가입, 로그인, 인증
├── quiz-service/            # 퀴즈 출제, 정답 저장
├── rank-service/            # 랭킹 및 통계
├── discovery-service/       # Eureka 서버
├── config-server/           # Config 서버
├── config-repo/             # 설정 파일 저장소
└── docker-compose.yml       # 로컬 통합 실행 설정
```

### ⚙️ 실행 방법 (로컬 개발)
```bash
# 전체 빌드
./gradlew clean build -x test

# Docker 기반 실행
docker-compose up --build
```

### 🚀 CI/CD 파이프라인
- CI: GitHub Actions를 통한 자동 빌드 및 Docker 이미지 생성
-  EC2 서버에 SSH 배포 및 docker-compose 재실행
각 서비스는 별도의 Docker 이미지로 빌드되며, 공통 .env 및 docker-compose.yml로 통합 배포됩니다.

### 📝 브랜치 전략
- main: 운영 배포용
- develop: 통합 개발 브랜치
- 서비스명/기능명: 단위 기능 구현 브랜치 (예: user/login)

### 📁 관련 정보
- Frontend Repository: 준비 중
- Swagger: 준비 중
- 기능명세서: 준비 중