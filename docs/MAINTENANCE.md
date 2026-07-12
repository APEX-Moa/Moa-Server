# Moa 유지보수 가이드

이 문서 하나로 **실행 · 수정 · 배포 · 문제해결**을 모두 다룹니다.
Spring/웹을 처음 만지는 후배도 따라올 수 있도록 순서대로 적었습니다.

---

## 0. 한눈에 보는 구조

```
[학생 폰]  index.html ─┐
                       ├──(HTTPS/JSON)──▶  [Spring Boot 백엔드]  ──▶  [DB]
[선생님]  teacher.html ─┘        REST API              JPA         H2(개발)/PostgreSQL(운영)
```

- 프론트엔드 = **정적 파일**(HTML/CSS/JS). 빌드 필요 없음. 브라우저가 그대로 실행.
- 백엔드 = **Spring Boot**. 모든 데이터·검증·인증을 담당.
- 둘은 **REST API(JSON)** 로만 대화. 프론트의 모든 서버호출은 `frontend/api.js` 한 파일에 모여 있음.

### 백엔드 폴더 (표준 Spring 계층 구조)
```
backend/src/main/java/kr/moa/
├── MoaApplication.java        앱 시작점
├── config/                    설정(MoaProperties: 교시·학교명·JWT), 초기데이터(DataSeeder)
├── security/                  JWT 발급/검증, 시큐리티 설정
├── domain/                    DB 테이블 = 엔티티 (Student, Room, Reservation, Staff, RosterEntry)
├── repository/                DB 접근 (Spring Data JPA 인터페이스)
├── service/                   업무 로직 (예약 규칙·정원·출석 검증)
└── web/                       REST 컨트롤러(주소 매핑) + dto(요청/응답 형식)
```
> **수정할 때 어디를 보나?** 규칙(정원, 중복예약, 출석시간)은 `service/`, 주소/입출력은 `web/`, 저장구조는 `domain/`.

### 프론트엔드 폴더
```
frontend/
├── index.html / app.js      학생 화면 + 로직
├── teacher.html / teacher.js 선생님 화면 + 로직
├── api.js                   ★ 서버 호출 모음 (엔드포인트 추가 시 여기부터)
├── config.js                API 주소·교시 캐시
└── styles.css               디자인 시스템(색상 변수 :root)
```

---

## 1. 준비물 설치 (최초 1회)

```bash
# JDK 17 (백엔드 실행에 필요)
brew install openjdk@17
# PATH 등록 (zsh)
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc && source ~/.zshrc
java -version    # 17 확인

# Node (프론트 로컬 서버용, 선택) — 이미 설치돼 있으면 생략
brew install node
```

---

## 2. 개발 환경에서 실행

**터미널 1 — 백엔드**
```bash
cd backend
./mvnw spring-boot:run
```
- 처음 실행 시 의존성 다운로드로 몇 분 걸립니다.
- 개발 모드는 **H2 메모리 DB**라 껐다 켜면 데이터가 초기화됩니다(테스트에 편함).
- H2 콘솔: http://localhost:8080/h2 (JDBC URL `jdbc:h2:mem:moa`, user `sa`, 비번 없음)
- 최초 기동 시 관리자 계정 `admin / moa0000` 이 자동 생성됩니다(로그에 표시).

**터미널 2 — 프론트엔드**
```bash
cd frontend
npx serve .
```
- 학생: http://localhost:3000/index.html · 선생님: http://localhost:3000/teacher.html
- `config.js`가 로컬에선 자동으로 `http://localhost:8080/api` 를 사용합니다.

---

## 3. 자주 하는 유지보수 작업

### (1) 교시 시간 바꾸기 / 추가
`backend/src/main/resources/application.yml`:
```yaml
moa:
  periods:
    - { id: p1, label: "야자 1교시", time: "18:30 – 20:00" }
    - { id: p2, label: "야자 2교시", time: "20:10 – 21:40" }
    # - { id: p3, label: "야자 3교시", time: "21:50 – 22:40" }   # 추가 예시
```
> `time` 형식은 반드시 `HH:MM – HH:MM`(가운데 – 대시). 출석 가능 시간(시작 20분 전~종료)이 이 값으로 계산됩니다.
> 저장 후 백엔드만 재시작하면 프론트에도 자동 반영됩니다(프론트가 `/api/config`로 받아감).

### (2) 학교 이름 바꾸기
같은 `application.yml` → `moa.school-name: "학교이름"`.

### (3) 교실 지정/정원/차단 (배치도 방식)
선생님 콘솔 → **관리 탭** → **야자 교실 지정(배치도)**:
- 학교 배치도가 보이고, 그 아래 건물·층별 교실 목록이 있습니다.
- **스위치를 켜면** 그 교실이 학생 예약 대상이 되고, **정원**은 옆 칸에서 바로 조정합니다.
- 끄면 예약 대상에서 제외됩니다(데이터는 보존).
- 배치도에 없는 공간은 **+ 직접 추가**로 만들 수 있습니다.

배치도 교실 목록(이름·기본 정원)은 코드의 **`frontend/school-rooms.js`** 한 파일에 있습니다.
교실 이름을 바꾸거나 새 교실을 목록에 넣으려면 이 파일만 수정하세요. 배치도 이미지는 `frontend/assets/floorplan.png` 를 교체하면 됩니다.

> **도착 확인 QR**: 관리 탭 → *지정 교실 QR 인쇄* → 켜 둔 교실들의 QR이 인쇄용 페이지로 열립니다(교실 문에 부착).

### (4) 학생 명단 등록
관리 탭 → 명단 붙여넣기(`학번,이름` 한 줄에 하나). 엑셀에서 두 열 복사해 붙여도 됩니다.
명단에 있는 학번만 학생 가입이 가능합니다(외부인 차단).

### (5) 교사 계정 관리 (개별 계정)
로그인은 **아이디 + 비밀번호**이고, 계정은 두 종류입니다.
- **총괄 관리자(ADMIN)** — 기본 `admin` / `moa0000`. 교사 계정을 추가·삭제·비번재설정할 수 있고, 교사 기능도 전부 사용.
- **교사(STAFF)** — 관리자가 발급. 승인·교실지정만. 누가 승인했는지 **예약에 이름이 기록**됩니다.

**교사 추가/삭제/비번재설정 (관리자):** 콘솔 → **관리 탭 → 교사 계정 관리** → `+ 교사 추가`(아이디·이름·비밀번호) / 교사 카드 탭 → 비번재설정·삭제. (코드 수정 불필요)

**총괄 관리자(admin) 비밀번호 변경** — `moa0000`은 **반드시 바꾸세요.**
- 운영 전이면: `config/DataSeeder.java`의 기본 비번을 바꾸고 DB의 admin 행을 지운 뒤 재시작.
- 운영 중이면: 다른 관리자 계정으로 로그인해 admin의 비번을 재설정하거나, DB에서 `staff.password_hash`를 BCrypt 해시로 직접 업데이트.
> 관리자 계정은 앱에서 삭제되지 않습니다(실수 방지).

### (6) 색상·디자인 바꾸기
`frontend/styles.css` 맨 위 `:root`의 변수만 바꾸면 전체 톤이 바뀝니다.
```css
--brand: #4f46e5;   /* 메인 색 */
--accent:#06b6d4;   /* 강조 색 */
```

---

## 4. 데이터베이스

| | 개발(dev) | 운영(prod) |
|---|---|---|
| 종류 | H2 (메모리) | PostgreSQL |
| 데이터 유지 | ❌ 재시작 시 초기화 | ✅ 영구 |
| 설정 | 기본값 | 환경변수 `DB_URL/DB_USER/DB_PASSWORD` |

**주요 테이블**: `student`(가입 학생), `roster_entry`(가입 허용 명단), `room`(교실),
`reservation`(예약·출석), `staff`(관리자).

**백업(운영, 매일 권장)**:
```bash
pg_dump "$DB_URL" > backup_$(date +%F).sql     # 백업
psql "$DB_URL" < backup_2026-03-01.sql          # 복원
```

---

## 5. 운영 배포

### 5-1. 프론트엔드를 백엔드에 합쳐 한 번에 서빙(권장, 가장 간단)
```bash
cp frontend/* backend/src/main/resources/static/     # 정적파일 포함
# frontend/config.js 의 MOA_API 는 배포 도메인에서 자동으로 "/api" 를 사용하므로 수정 불필요
cd backend && ./mvnw clean package                    # → target/moa-*.jar 생성
```
그러면 `https://도메인/` 이 학생 화면, `/teacher.html` 이 선생님 화면, `/api/**` 가 API가 됩니다.

### 5-2. 서버에서 실행 (PostgreSQL + prod 프로파일)
```bash
export DB_URL="jdbc:postgresql://localhost:5432/moa"
export DB_USER="moa"
export DB_PASSWORD="******"
export MOA_JWT_SECRET="아주-길고-랜덤한-64자-이상-비밀키"     # 반드시 교체
java -jar target/moa-*.jar --spring.profiles.active=prod
```

### 5-3. 항상 켜두기 (systemd, Linux 서버)
`/etc/systemd/system/moa.service`:
```ini
[Unit]
Description=Moa
After=network.target postgresql.service
[Service]
User=moa
Environment=DB_URL=jdbc:postgresql://localhost:5432/moa
Environment=DB_USER=moa
Environment=DB_PASSWORD=******
Environment=MOA_JWT_SECRET=아주-길고-랜덤한-비밀키
ExecStart=/usr/bin/java -jar /opt/moa/moa.jar --spring.profiles.active=prod
Restart=always
[Install]
WantedBy=multi-user.target
```
```bash
sudo systemctl enable --now moa      # 등록+시작
sudo journalctl -u moa -f            # 로그 보기
```

### 5-4. HTTPS
- **QR 카메라 스캔은 HTTPS에서만 동작**합니다. 학교 도메인에 인증서를 붙이세요.
- 가장 쉬운 방법: 앞단에 **Nginx + Let's Encrypt(certbot)** 리버스 프록시로 8080을 감싸기.

---

## 6. 300명 동시접속 대응 (이미 반영됨 · 확인용)

| 항목 | 설정 | 위치 |
|---|---|---|
| 인증 | **무상태 JWT** (서버 세션 없음 → 수평 확장 쉬움) | `security/` |
| DB 커넥션 풀 | HikariCP **max 30** | `application-prod.yml` |
| 웹 스레드 | Tomcat 기본 **200** (300 동접에 충분) | 기본값 |
| 중복예약 방지 | `(학번, 날짜, 교시)` **DB 유니크 제약** | `domain/Reservation` |

- 300명이 "동시에 매 순간 요청"하는 게 아니라 대부분 화면을 보는 상태라, 위 설정으로 넉넉합니다.
- 더 키워야 하면: `application-prod.yml`의 `maximum-pool-size`와 `server.tomcat.threads.max`를 함께 올리고 DB의 `max_connections`도 확인하세요.
- 예약 폭주 시간대 동시 신청은 DB 유니크 제약으로 중복이 원천 차단됩니다. 정원 초과는 서비스 트랜잭션에서 재확인합니다.

---

## 7. API 요약 (프론트 ↔ 백엔드 계약)

| 메서드 & 경로 | 권한 | 설명 |
|---|---|---|
| `GET /api/config` | 공개 | 학교명·교시 |
| `POST /api/auth/student/register` | 공개 | 학생 가입(명단 확인) |
| `POST /api/auth/student/login` | 공개 | 학생 로그인 |
| `POST /api/auth/staff/login` | 공개 | 선생님 로그인 |
| `GET /api/rooms` | 로그인 | 사용 가능 교실 |
| `GET /api/availability?date=` | 로그인 | 교실·교시별 예약 수(잔여 계산) |
| `POST /api/reservations` | 학생 | 예약 신청 |
| `GET /api/reservations/me` | 학생 | 내 예약 |
| `DELETE /api/reservations/{id}` | 학생 | 예약 취소 |
| `POST /api/reservations/checkin` | 학생 | QR 출석 |
| `GET /api/reservations?date=` | 선생님 | 그 날 전체 예약 |
| `POST /api/reservations/{id}/approve` `/reject` | 선생님 | 승인/반려 |
| `POST /api/reservations/bulk-approve?date=` | 선생님 | 일괄 승인 |
| `GET/POST/PUT/DELETE /api/rooms...` | 선생님 | 교실 관리 |
| `POST /api/roster/bulk` | 선생님 | 명단 등록 |

프론트에서 새 기능이 필요하면 `frontend/api.js`에 함수 한 줄 추가 → 백엔드 `web/`에 엔드포인트 추가.

---

## 8. 문제 해결

| 증상 | 원인 / 해결 |
|---|---|
| 프론트에서 "서버에 연결할 수 없어요" | 백엔드 안 켜짐, 또는 `config.js`의 `MOA_API` 주소 틀림 |
| 로그인은 되는데 곧 튕김(401) | JWT 만료(기본 12시간). 다시 로그인. 만료시간은 `moa.jwt.expiry-hours` |
| CORS 에러(콘솔 빨간 글씨) | 개발 중엔 허용됨. 운영에서 도메인이 다르면 `security/`의 CORS 허용 도메인에 추가 |
| QR 스캔 시 카메라 안 열림 | **HTTPS 또는 localhost**에서만 동작. 배포 도메인에 인증서 필요 |
| 출석이 "시간이 아니에요"로 막힘 | 서버 시간대 확인. 서버 TZ를 `Asia/Seoul`로(`TZ=Asia/Seoul` 환경변수) |
| 재배포 후 데이터가 사라짐 | 개발 H2는 원래 초기화됨. 운영은 반드시 PostgreSQL(prod) 사용 |

---

## 9. 보안 체크리스트 (운영 전)

- [ ] 관리자 비밀번호 `moa0000` → 강한 값으로 변경 (3-(5))
- [ ] `MOA_JWT_SECRET` 을 길고 랜덤한 값으로 설정 (env)
- [ ] HTTPS 적용 (QR·비밀번호 보호)
- [ ] `security/`의 CORS 허용을 운영 도메인으로 좁히기
- [ ] DB 자동 백업(cron + pg_dump) 설정
- [ ] 학생 PIN·비밀번호는 **BCrypt 해시로 저장**됨(평문 저장 안 함) — 확인만

---

문의가 쌓이면 이 문서에 계속 항목을 추가하세요. 유지보수의 8할은 "다음 사람을 위한 기록"입니다.
