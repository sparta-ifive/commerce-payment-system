# 🛒 Commerce Payment System

## 📖 목차
1. [프로젝트 소개](#프로젝트-소개)
2. [팀소개](#팀소개)
3. [주요기능](#주요기능)
4. [개발기간](#개발기간)
5. [기술스택](#기술스택)
6. [서비스 구조](#서비스-구조)
7. [API 명세서](#API-명세서)
8. [ERD](#ERD)
9. [프로젝트 파일 구조](#프로젝트-파일-구조)
10. [Trouble Shooting](#trouble-shooting)

---

## 👨‍🏫 프로젝트 소개

**Commerce Payment System**은 실제 커머스 환경을 구현한 결제 통합 플랫폼입니다.

사용자는 상품을 조회하고 주문을 생성한 뒤, PortOne(아임포트) V2 API를 통해 결제를 진행할 수 있습니다.
결제 완료 시 멤버십 등급에 따라 포인트가 자동 적립되며, 적립된 포인트는 이후 결제 시 사용할 수 있습니다.
환불 요청 시 PortOne API를 통해 실제 결제 취소가 이루어지며, 웹훅(Webhook)을 통해 결제 상태를 실시간으로 동기화합니다.

---

## 팀소개

| 이름 | 역할 | Github                                |
|------|------|---------------------------------------|
| 나은총 | 팀장 | https://github.com/popo2381                    |
| 정은식 | 팀원 | https://github.com/S1K1DA        |
| 강동혁 | 팀원 | https://github.com/youzting                            |
| 조성진 | 팀원 | https://github.com/imprity                         |
| 조현희 | 팀원 | https://github.com/hhjo96 |

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
![IntelliJ IDEA](https://img.shields.io/badge/IntelliJ_IDEA-000000?style=for-the-badge&logo=intellij-idea&logoColor=white)

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
<svg viewBox="0 0 920 640" xmlns="http://www.w3.org/2000/svg" font-family="'Segoe UI', sans-serif">
  <defs>
    <linearGradient id="bgGrad" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" style="stop-color:#0f172a"/>
      <stop offset="100%" style="stop-color:#1e293b"/>
    </linearGradient>
    <linearGradient id="serverGrad" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" style="stop-color:#1d4ed8"/>
      <stop offset="100%" style="stop-color:#2563eb"/>
    </linearGradient>
    <filter id="shadow">
      <feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/>
    </filter>
    <marker id="arrow" markerWidth="8" markerHeight="6" refX="7" refY="3" orient="auto">
      <polygon points="0 0, 8 3, 0 6" fill="#94a3b8"/>
    </marker>
    <marker id="arrowBlue" markerWidth="8" markerHeight="6" refX="7" refY="3" orient="auto">
      <polygon points="0 0, 8 3, 0 6" fill="#60a5fa"/>
    </marker>
    <marker id="arrowPurple" markerWidth="8" markerHeight="6" refX="7" refY="3" orient="auto">
      <polygon points="0 0, 8 3, 0 6" fill="#a78bfa"/>
    </marker>
    <marker id="arrowGreen" markerWidth="8" markerHeight="6" refX="7" refY="3" orient="auto">
      <polygon points="0 0, 8 3, 0 6" fill="#34d399"/>
    </marker>
    <marker id="arrowOrange" markerWidth="8" markerHeight="6" refX="7" refY="3" orient="auto">
      <polygon points="0 0, 8 3, 0 6" fill="#fbbf24"/>
    </marker>
  </defs>

  <!-- Background -->
  <rect width="920" height="640" fill="url(#bgGrad)" rx="16"/>

  <!-- Title -->
<text x="460" y="36" text-anchor="middle" font-size="16" font-weight="700" fill="#f1f5f9" letter-spacing="1">Commerce Payment System — Architecture Diagram</text>

  <!-- ===================== CLIENT (x=20~150) ===================== -->
  <rect x="20" y="56" width="130" height="78" rx="10" fill="#1e293b" stroke="#475569" stroke-width="1.5" filter="url(#shadow)"/>
  <text x="85" y="83" text-anchor="middle" font-size="20">🖥️</text>
  <text x="85" y="104" text-anchor="middle" font-size="12" font-weight="600" fill="#e2e8f0">Client</text>
  <text x="85" y="120" text-anchor="middle" font-size="9" fill="#94a3b8">Browser / App</text>

  <!-- Arrows Client <-> Spring Boot -->
  <line x1="150" y1="88" x2="220" y2="88" stroke="#60a5fa" stroke-width="1.8" marker-end="url(#arrowBlue)" stroke-dasharray="5,3"/>
  <text x="185" y="82" text-anchor="middle" font-size="8" fill="#60a5fa">REST API</text>
  <line x1="220" y1="104" x2="150" y2="104" stroke="#94a3b8" stroke-width="1.4" marker-end="url(#arrow)" stroke-dasharray="4,3"/>
  <text x="185" y="117" text-anchor="middle" font-size="8" fill="#94a3b8">Response</text>

  <!-- ===================== SPRING BOOT SERVER (x=222~670, w=448) ===================== -->
  <rect x="222" y="53" width="448" height="515" rx="12" fill="#0f172a" stroke="#2563eb" stroke-width="1.8" filter="url(#shadow)"/>
  <!-- Header bar -->
  <rect x="222" y="53" width="448" height="32" rx="12" fill="url(#serverGrad)"/>
  <rect x="222" y="71" width="448" height="14" fill="url(#serverGrad)"/>
  <text x="446" y="74" text-anchor="middle" font-size="12" font-weight="700" fill="#fff">🚀  Spring Boot Application</text>

  <!-- Security Layer (x=236~656, w=420) -->
  <rect x="236" y="98" width="420" height="38" rx="7" fill="#1e3a5f" stroke="#3b82f6" stroke-width="1.2"/>
  <text x="446" y="114" text-anchor="middle" font-size="10.5" font-weight="600" fill="#93c5fd">🔐  Security Layer</text>
  <text x="446" y="129" text-anchor="middle" font-size="8" fill="#64a4dc">JWT Filter · AuthUtil · UserDetailsImpl · UserDetailsServiceImpl</text>

  <!-- Controller Layer -->
  <rect x="236" y="148" width="420" height="62" rx="7" fill="#172554" stroke="#3b82f6" stroke-width="1.2"/>
  <text x="446" y="165" text-anchor="middle" font-size="10.5" font-weight="600" fill="#93c5fd">📡  Controller Layer</text>
  <text x="295" y="181" text-anchor="middle" font-size="8" fill="#7dd3fc">UserController</text>
  <text x="381" y="181" text-anchor="middle" font-size="8" fill="#7dd3fc">ProductController</text>
  <text x="471" y="181" text-anchor="middle" font-size="8" fill="#7dd3fc">PaymentController</text>
  <text x="565" y="181" text-anchor="middle" font-size="8" fill="#7dd3fc">ConfigController</text>
  <text x="340" y="198" text-anchor="middle" font-size="8" fill="#7dd3fc">WebhookController</text>
  <text x="480" y="198" text-anchor="middle" font-size="8" fill="#7dd3fc">HomeController · PageController</text>

  <!-- Service Layer -->
  <rect x="236" y="222" width="420" height="86" rx="7" fill="#0f2044" stroke="#2563eb" stroke-width="1.2"/>
  <text x="446" y="239" text-anchor="middle" font-size="10.5" font-weight="600" fill="#93c5fd">⚙️  Service Layer</text>
  <text x="280" y="255" text-anchor="middle" font-size="8" fill="#7dd3fc">UserService</text>
  <text x="363" y="255" text-anchor="middle" font-size="8" fill="#7dd3fc">PaymentService</text>
  <text x="446" y="255" text-anchor="middle" font-size="8" fill="#7dd3fc">PointService</text>
  <text x="533" y="255" text-anchor="middle" font-size="8" fill="#7dd3fc">ProductService</text>
  <text x="610" y="255" text-anchor="middle" font-size="8" fill="#7dd3fc">WebhookService</text>
  <text x="308" y="271" text-anchor="middle" font-size="8" fill="#7dd3fc">PointSupportService</text>
  <text x="420" y="271" text-anchor="middle" font-size="8" fill="#7dd3fc">AuditTxService</text>
  <text x="535" y="271" text-anchor="middle" font-size="8" fill="#7dd3fc">JwtTokenProvider</text>
  <text x="350" y="287" text-anchor="middle" font-size="8" fill="#7dd3fc">TimeService</text>
  <text x="470" y="287" text-anchor="middle" font-size="8" fill="#7dd3fc">PortOneClient</text>
  <text x="570" y="287" text-anchor="middle" font-size="8" fill="#7dd3fc">PortOneWebhookVerifier</text>

  <!-- Repository Layer -->
  <rect x="236" y="320" width="420" height="58" rx="7" fill="#0a1628" stroke="#1d4ed8" stroke-width="1.2"/>
  <text x="446" y="337" text-anchor="middle" font-size="10.5" font-weight="600" fill="#93c5fd">🗄️  Repository Layer (JPA)</text>
  <text x="272" y="353" text-anchor="middle" font-size="8" fill="#7dd3fc">UserRepo</text>
  <text x="340" y="353" text-anchor="middle" font-size="8" fill="#7dd3fc">PaymentRepo</text>
  <text x="408" y="353" text-anchor="middle" font-size="8" fill="#7dd3fc">PointRepo</text>
  <text x="472" y="353" text-anchor="middle" font-size="8" fill="#7dd3fc">OrderRepo</text>
  <text x="540" y="353" text-anchor="middle" font-size="8" fill="#7dd3fc">RefundRepo</text>
  <text x="610" y="353" text-anchor="middle" font-size="8" fill="#7dd3fc">WebhookRepo</text>
  <text x="330" y="369" text-anchor="middle" font-size="8" fill="#7dd3fc">PointAuditRepo</text>
  <text x="450" y="369" text-anchor="middle" font-size="8" fill="#7dd3fc">MembershipGradeRepo</text>
  <text x="570" y="369" text-anchor="middle" font-size="8" fill="#7dd3fc">UserRefreshTokenRepo</text>

  <!-- Exception Handler Box (left half) -->
  <rect x="236" y="390" width="204" height="40" rx="7" fill="#1c1917" stroke="#78716c" stroke-width="1.2"/>
  <text x="338" y="407" text-anchor="middle" font-size="10" font-weight="600" fill="#d6d3d1">🛡️  Exception Handler</text>
  <text x="338" y="422" text-anchor="middle" font-size="8" fill="#a8a29e">GlobalExceptionHandler · ErrorCode</text>

  <!-- Webhook Verifier Box (right half) -->
  <rect x="452" y="390" width="204" height="40" rx="7" fill="#1a1a2e" stroke="#6366f1" stroke-width="1.2"/>
  <text x="554" y="407" text-anchor="middle" font-size="10" font-weight="600" fill="#a5b4fc">🔏  Webhook Verifier</text>
  <text x="554" y="422" text-anchor="middle" font-size="8" fill="#818cf8">PortOneSdkWebhookVerifier</text>

  <!-- Data Initializer Box (left half) -->
  <rect x="236" y="442" width="204" height="40" rx="7" fill="#0f172a" stroke="#334155" stroke-width="1.2"/>
  <text x="338" y="459" text-anchor="middle" font-size="10" font-weight="600" fill="#94a3b8">🌱  Data Initializer</text>
  <text x="338" y="474" text-anchor="middle" font-size="8" fill="#64748b">Product · MemberShip Initializer</text>

  <!-- Constants / Properties Box (right half) -->
  <rect x="452" y="442" width="204" height="40" rx="7" fill="#0f172a" stroke="#334155" stroke-width="1.2"/>
  <text x="554" y="459" text-anchor="middle" font-size="10" font-weight="600" fill="#94a3b8">📌  Constants</text>
  <text x="554" y="474" text-anchor="middle" font-size="8" fill="#64748b">AppProperties · PortOneProperties</text>

  <!-- Config Box -->
  <rect x="236" y="494" width="420" height="40" rx="7" fill="#0f172a" stroke="#334155" stroke-width="1"/>
  <text x="446" y="511" text-anchor="middle" font-size="10" font-weight="600" fill="#64748b">⚙️  Configuration</text>
  <text x="446" y="526" text-anchor="middle" font-size="8" fill="#475569">SecurityConfig · PortOneConfig · ClockConfig · SchedulingConfig · JpaAuditingConfig</text>

  <!-- ===================== DATABASE (x=712~892) ===================== -->
  <rect x="712" y="90" width="188" height="106" rx="12" fill="#134e4a" stroke="#0d9488" stroke-width="1.5" filter="url(#shadow)"/>
  <text x="806" y="122" text-anchor="middle" font-size="20">🗃️</text>
  <text x="806" y="146" text-anchor="middle" font-size="12" font-weight="600" fill="#ccfbf1">Database</text>
  <text x="806" y="163" text-anchor="middle" font-size="8.5" fill="#5eead4">users · payments · orders</text>
  <text x="806" y="179" text-anchor="middle" font-size="8.5" fill="#5eead4">points · refunds · webhooks</text>

  <!-- Arrow: Repository → DB -->
  <line x1="656" y1="340" x2="710" y2="180" stroke="#34d399" stroke-width="1.8" marker-end="url(#arrowGreen)"/>
  <text x="700" y="278" font-size="8" fill="#34d399" transform="rotate(-60,700,278)">JPA / Hibernate</text>

  <!-- ===================== PORTONE API (x=712~900) ===================== -->
  <rect x="712" y="258" width="188" height="100" rx="12" fill="#3b0764" stroke="#9333ea" stroke-width="1.5" filter="url(#shadow)"/>
  <text x="806" y="290" text-anchor="middle" font-size="20">💳</text>
  <text x="806" y="313" text-anchor="middle" font-size="12" font-weight="600" fill="#f3e8ff">PortOne API V2</text>
  <text x="806" y="330" text-anchor="middle" font-size="8.5" fill="#c4b5fd">결제 / 취소 / 조회</text>
  <text x="806" y="346" text-anchor="middle" font-size="8.5" fill="#c4b5fd">REST API 호출</text>

  <!-- Arrow: PortOneClient → PortOne API -->
  <line x1="452" y1="410" x2="710" y2="330" stroke="#a78bfa" stroke-width="1.8" marker-end="url(#arrowPurple)"/>

  <!-- ===================== WEBHOOK (x=712~900) ===================== -->
  <rect x="712" y="420" width="188" height="90" rx="12" fill="#3b0764" stroke="#9333ea" stroke-width="1.5" filter="url(#shadow)"/>
  <text x="806" y="450" text-anchor="middle" font-size="18">📨</text>
  <text x="806" y="471" text-anchor="middle" font-size="12" font-weight="600" fill="#f3e8ff">PortOne Webhook</text>
  <text x="806" y="488" text-anchor="middle" font-size="8.5" fill="#c4b5fd">결제 상태 실시간 동기화</text>
  <text x="806" y="504" text-anchor="middle" font-size="8" fill="#9f7aea">POST /portone-webhook</text>

  <!-- Arrow: Webhook → Spring Boot -->
  <line x1="712" y1="465" x2="672" y2="465" stroke="#a78bfa" stroke-width="1.8" marker-end="url(#arrowPurple)"/>

  <!-- ===================== SCHEDULER (x=20~180) ===================== -->
  <rect x="20" y="380" width="160" height="110" rx="12" fill="#451a03" stroke="#d97706" stroke-width="1.5" filter="url(#shadow)"/>
  <text x="100" y="410" text-anchor="middle" font-size="18">⏰</text>
  <text x="100" y="430" text-anchor="middle" font-size="12" font-weight="600" fill="#fef3c7">Scheduler</text>
  <text x="100" y="448" text-anchor="middle" font-size="8.5" fill="#fcd34d">포인트 확정 배치</text>
  <text x="100" y="464" text-anchor="middle" font-size="8.5" fill="#fcd34d">멤버십 등급 갱신</text>
  <text x="100" y="480" text-anchor="middle" font-size="8" fill="#b45309">PointTasks (@Scheduled)</text>

  <!-- Arrow: Scheduler → Spring Boot -->
  <line x1="180" y1="435" x2="220" y2="435" stroke="#fbbf24" stroke-width="1.8" marker-end="url(#arrowOrange)"/>

  <!-- ===================== LEGEND ===================== -->
  <rect x="20" y="516" width="178" height="112" rx="8" fill="#1e293b" stroke="#334155" stroke-width="1"/>
  <text x="109" y="534" text-anchor="middle" font-size="10" font-weight="700" fill="#94a3b8">Legend</text>
  <line x1="34" y1="550" x2="62" y2="550" stroke="#60a5fa" stroke-width="1.8" stroke-dasharray="5,3" marker-end="url(#arrowBlue)"/>
  <text x="68" y="554" font-size="8.5" fill="#94a3b8">REST 요청</text>
  <line x1="34" y1="568" x2="62" y2="568" stroke="#94a3b8" stroke-width="1.4" stroke-dasharray="4,3" marker-end="url(#arrow)"/>
  <text x="68" y="572" font-size="8.5" fill="#94a3b8">REST 응답</text>
  <line x1="34" y1="586" x2="62" y2="586" stroke="#34d399" stroke-width="1.8" marker-end="url(#arrowGreen)"/>
  <text x="68" y="590" font-size="8.5" fill="#94a3b8">DB 통신 (JPA)</text>
  <line x1="34" y1="604" x2="62" y2="604" stroke="#a78bfa" stroke-width="1.8" marker-end="url(#arrowPurple)"/>
  <text x="68" y="608" font-size="8.5" fill="#94a3b8">외부 API 연동</text>
  <line x1="34" y1="622" x2="62" y2="622" stroke="#fbbf24" stroke-width="1.8" marker-end="url(#arrowOrange)"/>
  <text x="68" y="626" font-size="8.5" fill="#94a3b8">스케줄 실행</text>
</svg>

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

![img_2.png](img_2.png)


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
