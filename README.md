# 카드 SMS 결제 내역 관리 앱

안드로이드 기반의 카드 결제 SMS를 자동으로 파싱하고 관리하는 애플리케이션입니다.

## 📱 주요 기능

### 🔍 SMS 자동 파싱
- 카드사에서 발송하는 결제 알림 SMS를 자동으로 수신
- 정규표현식 패턴을 통한 결제 정보 추출 (카드명, 금액, 가맹점)
- 신뢰도 점수 기반 자동 검증

### 💳 결제 내역 관리
- 개별 결제 내역 저장 및 관리
- 카테고리별 자동 분류 (식비, 교통비, 쇼핑 등)
- 사용자 메모 및 수동 검증 기능

### 📊 월별 결제 요약
- 월별 총 결제 금액 자동 계산
- 카드별 결제 현황 통계
- 결제 패턴 분석

### 🗓️ 카드 결제일 관리
- 주요 카드사별 기본 결제일 설정
- 개인 맞춤형 결제일 및 마감일 관리
- 결제일 알림 기능

### ⚙️ 패턴 관리
- SMS 파싱 패턴 사용자 정의
- 새로운 카드사 패턴 추가
- 패턴 활성화/비활성화 관리

## 🏗️ 기술 스택

- **언어**: Kotlin
- **아키텍처**: MVVM (Model-View-ViewModel)
- **데이터베이스**: Room (SQLite)
- **UI**: Material Design Components
- **네비게이션**: Navigation Component
- **비동기 처리**: Kotlin Coroutines
- **의존성 주입**: Manual DI

## 📦 주요 의존성

```kotlin
// UI
implementation("androidx.core:core-ktx:1.10.1")
implementation("com.google.android.material:material:1.10.0")
implementation("androidx.navigation:navigation-fragment-ktx:2.6.0")

// 데이터베이스
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")
```

## 🗄️ 데이터베이스 구조

### 주요 엔티티
- **SmsMessage**: 수신된 SMS 메시지
- **CardPayment**: 파싱된 결제 내역
- **PaymentCategory**: 결제 카테고리
- **CardBillingCycle**: 카드별 결제일 정보
- **SmsPattern**: SMS 파싱 패턴
- **MonthlySummary**: 월별 결제 요약

### 관계
- CardPayment → PaymentCategory (외래키)
- 각 엔티티는 적절한 인덱스로 성능 최적화

## 🚀 설치 및 실행

### 요구사항
- Android API 24 (Android 7.0) 이상
- SMS 수신 권한
- 외부 저장소 쓰기 권한

### 빌드 방법
```bash
# 프로젝트 클론
git clone [repository-url]
cd card_sms_checker

# 빌드
./gradlew assembleDebug

# 설치
./gradlew installDebug
```

## 📱 사용법

### 1. 초기 설정
1. 앱 설치 후 SMS 수신 권한 허용
2. 설정에서 사용하는 카드 정보 등록
3. SMS 파싱 패턴 확인 및 수정

### 2. 결제 내역 확인
- 메인 화면에서 최근 결제 내역 확인
- 카테고리별 필터링 가능
- 개별 결제 내역 수정 및 메모 추가

### 3. 월별 요약
- 월별 총 결제 금액 확인
- 카드별 결제 현황 분석
- 결제 패턴 파악

### 4. 결제일 관리
- 카드별 결제일 및 마감일 설정
- 결제일 알림 설정
- 결제일 기준 통계 확인

## 🔧 주요 클래스

### SmsReceiver
- SMS 수신 및 데이터베이스 저장
- 실시간 SMS 파싱 처리

### SmsParsingService
- SMS 메시지 파싱 로직
- 정규표현식 패턴 매칭
- 신뢰도 점수 계산

### PaymentRepository
- 결제 데이터 CRUD 작업
- 데이터베이스 트랜잭션 관리

### BillingCycleCalculator
- 결제일 계산 로직
- 월별 통계 생성

## 📋 지원 카드사

기본적으로 다음 카드사의 SMS 패턴을 지원합니다:
- 삼성카드
- 현대카드
- KB국민카드
- 신한카드
- 롯데카드
- 우리카드
- 하나카드
- NH농협카드
- BC카드
- IBK기업은행카드

## 🔒 개인정보 보호

- 모든 데이터는 로컬 디바이스에만 저장
- 외부 서버로 데이터 전송 없음
- SMS 내용은 파싱 후 원본 메시지와 함께 안전하게 보관

## 🐛 알려진 이슈

- 일부 카드사의 SMS 형식 변경 시 파싱 오류 가능
- 신규 카드사 추가 시 패턴 수동 설정 필요

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 `LICENSE` 파일을 참조하세요.

## 📞 문의

프로젝트에 대한 문의사항이나 버그 리포트는 Issues를 통해 제출해 주세요.

---

**개발자**: [개발자명]  
**버전**: 1.0  
**최종 업데이트**: 2024년
