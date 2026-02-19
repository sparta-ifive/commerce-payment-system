# 🛒 Commerce Payment System

## 📖 목차
1. [프로젝트 소개](#프로젝트-소개)
2. [팀소개](#팀소개)
3. [프로젝트 계기](#프로젝트-계기)
4. [주요기능](#주요기능)
5. [개발기간](#개발기간)
6. [기술스택](#기술스택)
7. [서비스 구조](#서비스-구조)
8. [와이어프레임](#와이어프레임)
9. [API 명세서](#API-명세서)
10. [ERD](#ERD)
11. [프로젝트 파일 구조](#프로젝트-파일-구조)
12. [Trouble Shooting](#trouble-shooting)

---

## 👨‍🏫 프로젝트 소개

**Commerce Payment System**은 실제 커머스 환경을 구현한 결제 통합 플랫폼입니다.

사용자는 상품을 조회하고 주문을 생성한 뒤, PortOne(아임포트) V2 API를 통해 결제를 진행할 수 있습니다.
결제 완료 시 멤버십 등급에 따라 포인트가 자동 적립되며, 적립된 포인트는 이후 결제 시 사용할 수 있습니다.
환불 요청 시 PortOne API를 통해 실제 결제 취소가 이루어지며, 웹훅(Webhook)을 통해 결제 상태를 실시간으로 동기화합니다.

---

## 팀소개

> ✏️ **[팀원 소개를 작성해 주세요]**
>
> 예시:
> | 이름 | 역할 | GitHub |
> |------|------|--------|
> | 홍길동 | 팀장 / 결제 도메인 | @github |
> | ... | ... | ... |

---

## 프로젝트 계기

> ✏️ **[프로젝트를 시작하게 된 계기나 목적을 작성해 주세요]**

---

## 💜 주요기능

- **회원 인증**: 이메일 기반 회원가입 및 로그인, JWT Access/Refresh Token 발급, 로그아웃 및 토큰 갱신
- **상품 관리**: 판매 중인 상품 목록 조회 및 상세 조회 (카테고리: 전자기기, 음식, 장난감, 의류)
- **결제 처리**: PortOne V2 API 연동을 통한 결제 시도·확정·실패 처리, 비관적 락을 통한 동시성 제어
- **포인트 시스템**: 멤버십 등급(NORMAL / VIP / VVIP)별 결제 금액 비례 포인트 적립, 결제 시 포인트 사용, 배치 작업을 통한 포인트 확정 처리
- **환불 처리**: 결제 후 7일 이내 환불 요청 시 PortOne API 통해 실제 결제 취소 및 재고 복구
- **웹훅 이벤트**: PortOne Webhook V2 서명 검증(Standard Webhooks), 멱등성 처리, 결제 상태 실시간 동기화

---

## ⏲️ 개발기간
- 2026.02.04(수) ~ 2026.02.20(금)

---

## 📚️ 기술스택

### ✔️ Language
![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white)

### ✔️ Version Control
![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white)
![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)

### ✔️ IDE
> ✏️ **[사용한 IDE를 작성해 주세요]** (예: IntelliJ IDEA, VS Code)

### ✔️ Framework
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white)

### ✔️ Deploy
> ✏️ **[배포 환경을 작성해 주세요]** (예: AWS EC2, Docker, GitHub Actions 등)

### ✔️ DBMS
> ✏️ **[사용한 데이터베이스를 작성해 주세요]** (예: MySQL, PostgreSQL)

---

## 서비스 구조

```
[Client]
   │
   ▼
[Spring Boot Application]
   ├── Security Layer (JWT 인증 필터)
   ├── Controller Layer (REST API)
   ├── Service Layer (비즈니스 로직)
   ├── Repository Layer (JPA)
   └── External API
        └── PortOne V2 (결제 / 환불 / 웹훅)

[Scheduler]
   └── 포인트 확정 & 멤버십 등급 갱신 배치 (Spring Scheduler)
```

> ✏️ **[아키텍처 다이어그램 이미지가 있으면 추가해 주세요]**

---

## 와이어프레임

> ✏️ **[와이어프레임 이미지 또는 링크를 첨부해 주세요]**

---

## API 명세서

> ✏️ **[API 명세서 링크(Swagger, Notion, Postman 등)를 첨부해 주세요]**

아래는 코드 기반으로 정리한 엔드포인트 목록입니다.

| 분류 | Method | URL | 설명 | 인증 |
|------|--------|-----|------|------|
| 인증 | POST | `/api/auth/signup` | 회원가입 | ❌ |
| 인증 | POST | `/api/auth/login` | 로그인 (JWT 발급) | ❌ |
| 인증 | POST | `/api/auth/refresh` | Access Token 재발급 | ❌ |
| 인증 | POST | `/api/auth/logout` | 로그아웃 | ✅ |
| 인증 | GET | `/api/auth/me` | 내 정보 조회 | ✅ |
| 상품 | GET | `/api/products` | 상품 목록 조회 | ❌ |
| 상품 | GET | `/api/products/{productId}` | 상품 상세 조회 | ❌ |
| 결제 | POST | `/api/payments/attempt` | 결제 시도 | ✅ |
| 결제 | POST | `/api/payments/{paymentId}/confirm` | 결제 확정 | ✅ |
| 결제 | POST | `/api/payments/{paymentId}/refund` | 환불 요청 | ✅ |
| 웹훅 | POST | `/portone-webhook` | PortOne 웹훅 수신 | ❌ |
| 설정 | GET | `/api/public/config` | 프론트 런타임 설정 조회 | ❌ |

---

## ERD

> ✏️ **[ERD 이미지 또는 링크를 첨부해 주세요]**


```
User ──< Payment
User ──< Point
User >── MembershipGrade
User ──< UserRefreshToken

Payment >── Order
Payment ──── Refund (1:1)
Payment ──── Point (1:1)

Point ──< PointAudit
Webhook (독립)
```

---

## 프로젝트 파일 구조

```
src/main/java/com/spartaifive/commercepayment/
├── common/
│   ├── audit/                  # AuditTxService (트랜잭션 분리 감사 서비스)
│   ├── auth/                   # AuthUtil, UserDetailsImpl, UserDetailsServiceImpl
│   ├── config/                 # Security, JWT, PortOne, JPA Auditing, Scheduling 설정
│   ├── constants/              # 환불 기간 등 비즈니스 상수
│   ├── controller/             # ConfigController, HomeController, PageController
│   ├── exception/              # GlobalExceptionHandler, ErrorCode, 커스텀 예외
│   ├── external/
│   │   └── portone/            # PortOneClient, Request/Response DTO
│   ├── initializer/            # 테스트 데이터 초기화 (상품, 멤버십)
│   ├── response/               # 공통 응답 포맷 (DataResponse, MessageResponse 등)
│   ├── security/               # JwtAuthenticationFilter, JwtTokenProvider
│   └── service/                # TimeService
└── domain/
    ├── payment/                # 결제 엔티티, 서비스, 컨트롤러, DTO
    ├── point/                  # 포인트 엔티티, 서비스, 배치 작업
    ├── product/                # 상품 엔티티, 서비스, 컨트롤러
    ├── refund/                 # 환불 엔티티, 레포지토리
    ├── user/                   # 유저, 멤버십, 리프레시 토큰 엔티티 및 서비스
    └── webhookevent/           # 웹훅 엔티티, 서비스, 컨트롤러, 서명 검증기
```

---

## Trouble Shooting



>
> ### 🔴 문제: [문제 제목]
> - **상황**: 어떤 상황에서 발생했는지
> - **원인**: 원인 분석
> - **해결**: 해결 방법
