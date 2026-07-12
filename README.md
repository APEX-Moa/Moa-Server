# Moa 백엔드 (야자 교실 예약)

광주소프트웨어마이스터고 "야자(야간자율학습) 교실 예약" 앱의 백엔드입니다.
Spring Boot 3 · Java 17 · Maven · JWT 인증(stateless).

## 빠른 시작 (개발)

```bash
# 프로젝트 루트(backend/)에서
./mvnw spring-boot:run
```

- 기본 프로파일은 **dev** 이며 **H2 인메모리 DB** 를 사용합니다. 별도 설치 불필요.
- 서버: http://localhost:8080

### 기본 관리자 계정
앱을 처음 실행하면 관리자 계정이 자동 생성됩니다 (콘솔 로그에도 1회 안내).

```
아이디: admin
비밀번호: moa0000
```
> 운영 배포 후에는 반드시 비밀번호를 변경하세요.

### H2 콘솔
- 주소: http://localhost:8080/h2
- JDBC URL: `jdbc:h2:mem:moa`
- 사용자: `sa` (비밀번호 없음)

## 인증 방식
- 로그인/회원가입 시 **JWT 토큰**을 발급받습니다.
- 이후 요청은 헤더에 `Authorization: Bearer <token>` 을 실어 보냅니다.
- 서버는 세션을 저장하지 않으므로(stateless) 수평 확장에 유리합니다.

## 운영(prod) 전환 — PostgreSQL

환경변수로 DB 접속 정보와 JWT 시크릿을 주입하고 `prod` 프로파일로 실행합니다.

```bash
export DB_URL="jdbc:postgresql://<host>:5432/moa"
export DB_USER="moa"
export DB_PASSWORD="********"
export MOA_JWT_SECRET="<64바이트 이상의 랜덤 문자열>"

./mvnw spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=prod
# 또는 빌드 후 실행
./mvnw clean package
java -jar target/moa-backend-1.0.0.jar --spring.profiles.active=prod
```

운영 튜닝 메모(약 300명 동시 접속 기준):
- JWT stateless 인증 → 세션 부담 없음
- HikariCP 커넥션 풀: max 30 / min-idle 5 (application-prod.yml)
- Tomcat max-threads=200 기본값으로 충분

## 설정 위치 (학교명 / 교시)

`src/main/resources/application.yml` 의 `moa.*` 아래에서 변경합니다.

```yaml
moa:
  school-name: 광주소프트웨어마이스터고
  jwt:
    secret: <개발용 기본값 — 운영은 MOA_JWT_SECRET 로 덮어쓰기>
    expiry-hours: 12
  periods:
    - id: p1
      label: 야자 1교시
      time: "18:30 – 20:00"   # 출석 가능 시간 판정에 사용 (HH:MM – HH:MM)
    - id: p2
      label: 야자 2교시
      time: "20:10 – 21:40"
```

## API 요약 (base path `/api`)

| 메서드 | 경로 | 권한 | 설명 |
|--------|------|------|------|
| POST | /auth/student/register | 공개 | 학생 가입 (명단에 있어야 함) |
| POST | /auth/student/login | 공개 | 학생 로그인 |
| POST | /auth/staff/login | 공개 | 교직원 로그인 |
| GET  | /config | 공개 | 학교명/교시 |
| GET  | /rooms | 인증 | 활성 교실 목록 |
| GET  | /rooms/all | STAFF | 전체 교실 |
| POST | /rooms | STAFF | 교실 생성 |
| PUT  | /rooms/{id} | STAFF | 교실 수정 |
| DELETE | /rooms/{id} | STAFF | 교실 삭제 |
| GET  | /availability?date= | 인증 | 교실·교시별 예약 수 |
| POST | /reservations | STUDENT | 예약 생성 |
| GET  | /reservations/me | STUDENT | 내 예약 |
| DELETE | /reservations/{id} | STUDENT | 예약 취소(본인) |
| POST | /reservations/checkin | STUDENT | 출석 체크 |
| GET  | /reservations?date= | STAFF | 날짜별 전체 예약 |
| POST | /reservations/{id}/approve | STAFF | 승인 |
| POST | /reservations/{id}/reject | STAFF | 거절 |
| POST | /reservations/bulk-approve?date= | STAFF | 일괄 승인 |
| POST | /roster/bulk | STAFF | 명단 업로드("학번,이름") |
| GET  | /roster/count | STAFF | 명단 인원 수 |

## 프로젝트 구조

```
kr.moa
├─ config      # MoaProperties, DataSeeder(초기 데이터)
├─ domain      # JPA 엔티티
├─ repository  # Spring Data JPA 저장소
├─ security    # JWT 발급/필터/시큐리티 설정
├─ service     # 비즈니스 로직
└─ web
   ├─ dto      # 요청/응답 record
   └─ *Controller, GlobalExceptionHandler, ApiException
```
