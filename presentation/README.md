# Presentation Module

이 모듈은 앱의 UI 레이어와 화면 흐름(Navigation)을 담당합니다.

## Navigation Flow

앱의 전체적인 화면 전환 흐름은 아래와 같습니다.

### 1. 초기 진입 및 권한 체크 (Graph Selection)
- **권한 미허용 상태**: `Graph.NotPermissionAllowed`로 진입
- **권한 허용 상태**: `Graph.PermissionAllowed`로 진입

### 2. 시각화된 흐름도 (ASCII Diagram)

```text
 [Start]
    |
 <Perm Allowed?> -- No --> [Graph.NotPermissionAllowed]
    |                          |
   Yes                         v
    |                 +-------------------+
    |                 |      Splash       |
    |                 +-------------------+
    |                          |
    |                 +-------------------+
    |                 |  AppUseAgreement  |
    |                 +-------------------+
    |                          |
    |                 +-------------------+
    |                 |       Perm        |
    |                 +-------------------+
    |                          |
    |                          v
    +-----------------> [Graph.PermissionAllowed]
                               |
                      +-------------------+
                      |      Splash       |
                      +-------------------+
                               |
                      +-------------------+
                      |    AppLoading     |
                      +-------------------+
                               |
                               v
                      [Graph.UsingCamera]
                               |
               +---------------+---------------+
               |               |               |
        +-------------+ +-------------+ +-------------+
        |   Setting   | |     Cam     | |   Images    |
        +-------------+ +-------------+ +-------------+
               ^               ^               ^
               |               |               |
               +---------------+---------------+
                        (Navigation)
```

### 3. 상세 흐름 설명
- **Graph.NotPermissionAllowed**: 최초 실행 시 약관 동의(`AppUseAgreement`)와 필수 권한(`Perm`) 획득을 유도합니다.
- **Graph.PermissionAllowed**: 권한이 이미 있는 경우, 앱 로딩(`AppLoading`)을 통해 AI 모델 및 카메라를 준비합니다.
- **Graph.UsingCamera**: 실제 카메라 기능이 동작하는 단계로, 설정(`Setting`)과 갤러리(`Images`) 화면으로 이동이 가능합니다.

## 주요 컴포넌트
- **MainNavHost**: 앱의 최상위 내비게이션 호스트. 권한 상태에 따라 시작 그래프를 결정합니다.
- **ProPoseTransition**: 앱 전역에서 사용하는 공통 화면 전환 애니메이션(Slide, Fade 등) 정의.
- **CustomDialog**: 다국어 지원이 적용된 공용 다이얼로그 컴포넌트.
