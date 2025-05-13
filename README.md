# 📌 백엔드 개발 과제 (Java)

## 📖 개요

- Spring Boot 기반의 JWT 인증/인가 시스템을 구현합니다.
- JUnit 테스트 코드를 작성합니다.
- Swagger를 통해 API 명세를 문서화합니다.
- AWS EC2에 배포하여 외부에서 접속 가능하도록 구성합니다.

---

## 🚀 배포 정보

- **Base URL**: `http://52.78.222.135:8080`
- **Swagger UI**: [http://52.78.222.135:8080/swagger-ui/index.html](http://52.78.222.135:8080/swagger-ui/index.html)

---

## 📘 API 문서

### 📌 주요 엔드포인트

| 기능              | 메서드 | URL                                      | 설명                                           |
|-------------------|--------|-------------------------------------------|------------------------------------------------|
| 회원가입          | POST   | `/signup`                                 | 사용자 계정을 생성합니다.                      |
| 로그인            | POST   | `/login`                                  | JWT 토큰을 발급받습니다.                       |
| 관리자 권한 부여 | PATCH  | `/admin/users/{userId}/roles`             | 관리자 권한을 부여합니다. (Admin 권한 필요)    |

---

### 🧪 예시 요청/응답

#### ✅ 회원가입 요청

```http
POST /signup
Content-Type: application/json
```

```json
{
  "username": "JIN HO",
  "password": "12341234",
  "nickname": "Mentos"
}
```

#### 🔁 성공 응답

```json
{
  "username": "JIN HO",
  "nickname": "Mentos",
  "roles": [
    {
      "role": "USER"
    }
  ]
}
```

#### ❌ 실패 응답 (중복 사용자)

```json
{
  "error": {
    "code": "USER_ALREADY_EXISTS",
    "message": "이미 가입된 사용자입니다."
  }
}
```

---

#### ✅ 로그인 요청

```http
POST /login
Content-Type: application/json
```

```json
{
  "username": "JIN HO",
  "password": "12341234"
}
```

#### 🔁 성공 응답

```json
{
  "token": "eKDIkdfjoakIdkfjpekdkcjdkoIOdjOKJDFOlLDKFJKL"
}
```

#### ❌ 실패 응답 (잘못된 정보)

```json
{
  "error": {
    "code": "INVALID_CREDENTIALS",
    "message": "아이디 또는 비밀번호가 올바르지 않습니다."
  }
}
```

---

#### ✅ 관리자 권한 부여 요청

```http
PATCH /admin/users/15/roles
Authorization: Bearer {토큰}
```

#### 🔁 성공 응답

```json
{
  "username": "JIN HO",
  "nickname": "Mentos",
  "roles": [
    {
      "role": "ADMIN"
    }
  ]
}
```

#### ❌ 실패 응답 (권한 없음)

```json
{
  "error": {
    "code": "ACCESS_DENIED",
    "message": "관리자 권한이 필요한 요청입니다. 접근 권한이 없습니다."
  }
}
```

---

## 🔐 JWT 인증 방식

### ✅ 발급

- 로그인 성공 시 JWT 토큰이 발급됩니다.
- 토큰 구조는 Header, Payload, Signature 로 구성됩니다.
- 예: `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`

### ✅ 요청 시 사용 방법

- 모든 보호된 API 요청에는 헤더에 JWT 토큰을 포함해야 합니다.

```http
Authorization: Bearer {발급받은 토큰}
```

### ✅ 서버 검증 로직

- 토큰 존재 여부 확인
- 서명 검증
- 만료 여부 확인
- 권한(role) 확인
- SecurityContext 에 인증 정보 등록

---

## ✅ 테스트

### 🔍 테스트 항목

| 항목               | 설명                                                                 |
|--------------------|----------------------------------------------------------------------|
| 회원가입           | 정상 / 중복 가입 시도 테스트                                        |
| 로그인             | 올바른 자격 정보 / 틀린 비밀번호 테스트                            |
| 관리자 권한 부여   | 관리자 유저 / 일반 유저 / 존재하지 않는 유저 테스트                |
| 토큰 관련          | 토큰 없음 / 만료 / 잘못된 토큰 형식 테스트                         |

### 💻 실행 방법

```bash
./gradlew test
```

또는

```bash
mvn test
```

---

## 📑 기타 참고 사항

- 서버 주소 및 포트는 과제 요구에 따라 **0.0.0.0:8080**으로 고정되어 있습니다.
- 모든 응답은 `application/json` 형식으로 반환됩니다.
- 실제 DB를 사용하지 않고, **메모리 내 데이터**를 사용하여 처리합니다.

---

## 📬 제출 항목 정리

- [x] GitHub Public Repository 링크
- [x] Swagger UI 주소: `http://52.78.222.135:8080/swagger-ui/index.html`
- [x] API 엔드포인트 URL: `http://52.78.222.135:8080`

---

> ⚠️ Swagger 연동에 실패한 경우, 위 API 명세를 기반으로 Postman 등으로 직접 요청을 테스트하세요.
