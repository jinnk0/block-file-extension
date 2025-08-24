# 파일 확장자 관리 기능 구현
## 과제 요구사항
### 목표
- 첨부 시 보안에 문제가 될 수 있는 파일 확장자 차단
### 요건
- [x] 고정 확장자는 차단을 자주 하는 확장자 리스트, 기본은 uncheck 상태
- [x] 고정 확장자 상태를 check, uncheck 상태로 변경할 경우 DB에 저장 (새로고침 시 유지)
- [x] 고정 확장자는 커스텀 확장자 칸에 추가되지 않음
- [x] 확장자 최대 입력 길이는 20자리
- [x] 확장자 추가 버튼 클릭 시 DB에 저장되고 아래쪽 영역에 표현됨
- [x] 커스텀 확장자는 최대 200개까지 추가 가능
- [x] 확장자 옆 x 버튼을 클릭 시 DB에서 삭제
## 추가적으로 고려한 내용
- 고정 확장자는 자주 차단하는 확장자 리스트이므로, 확장자 추가 빈도를 저장하여 1시간 주기로 업데이트
- 현재 추가되어 있는 커스텀 확장자 개수 표시
- 이미 추가한 커스텀 확장자 중복 추가 시 예외 처리
- 이중 확장자 처리 (ex. tar.gz -> tar, gz, tar.gz 모두 검사)
## DB 테이블
### 차단된 파일 확장자 관리 테이블 (blocked_file_extension)
| 속성(Attribute)  | 값(Value) | 설명                               |
|----------------|----------|----------------------------------|
| id(PK)         | long     |                                  |
| extension      | String   | 확장자 명                            |
| extension_type | String   | FIX(고정 확장자) / CUSTOM(커스텀 확장자)    |
| is_blocked     | boolean  | true(차단)/false(허용), 고정 확장자 체크 여부 |
### 파일 확장자 차단 빈도 관리 테이블 (extension_frequency)
| 속성(Attribute) | 값(Value) | 설명          |
|---------------|----------|-------------|
| id(PK)        | long     |             |
| extension     | String   | 확장자 명       |
| added_count   | long     | 확장자를 차단한 횟수 |
## 기술 스택
- API 개발 : Java, Spring Boot, JPA
- DB : H2 (local), PostgreSQL (production)
- 화면 개발 : Thymeleaf
- 배포 : Render
## 화면 설계
