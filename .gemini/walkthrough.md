# OpenCV Upgrade and KTS Conversion Walkthrough

OpenCV 모듈을 성공적으로 4.13.0 버전으로 업그레이드하고, 설정을 `build.gradle.kts`로 전환했습니다.

## 진행 상황 요약

- [x] **SDK 파일 이식:** `OpenCV-android-sdk/sdk`에서 최신 4.13.0 라이브러리 및 Java 래퍼 복사 완료
- [x] **KTS 변환:** `opencv/build.gradle.kts` 생성 및 기존 Groovy 파일 삭제 완료
- [x] **초기화 로직 최적화:** `ProPoseApplication`에서 전역 초기화를 수행하여 모듈 간 로딩 순서 문제 해결
- [x] **안드로이드 15(16KB) 대응:** 공통 빌트 로직(`KotlinAndroid.kt`)에 페이지 정렬 플래그 및 패키징 설정 적용 완료
- [x] **libc++ 심볼 충돌 해결:** OpenCV 4.13.0용 `libc++_shared.so`를 수동 배치하여 `UnsatisfiedLinkError` 해결 완료
- [x] **Gradle 빌드 에러 해결:** `:core:clean` 및 `:build-logic:clean` 태스크 누락 문제를 해결하기 위해 루트 및 하위 모듈 설정 업데이트 완료

## 주요 변경 사항

### 1. OpenCV 4.13.0 및 Kotlin DSL 전환

- `opencv` 모듈의 모든 소스 및 네이티브 라이브러리를 최신으로 업데이트하고, 빌드 스크립트를 Kotlin DSL로 완전히 전환했습니다.
- [opencv/build.gradle.kts](file:///Users/kyuyeonlee/Desktop/01_Works/App_ProPose/opencv/build.gradle.kts)

### 2. 안드로이드 15 및 16KB 페이지 규격 지원

- 안드로이드 15의 16KB 페이지 기기를 지원하기 위해 `KotlinAndroid.kt` 공통 컨벤션 플러그인을 수정했습니다.
- `useLegacyPackaging = false` 설정 및 링커 플래그 `-Wl,-z,max-page-size=16384`가 모든 모듈에 공통 적용됩니다.
- [KotlinAndroid.kt](file:///Users/kyuyeonlee/Desktop/01_Works/App_ProPose/build-logic/convention/src/main/kotlin/com/hanadulset/pro_poseapp/build_logic/convention/KotlinAndroid.kt)

### 3. 네이티브 라이브러리(JNI) 로딩 문제 해결

- **심볼 미검색 해결:** OpenCV 4.13.0이 필요로 하는 최신 `libc++_shared.so`를 수동으로 배치하여 PyTorch와의 버전 충돌(`UnsatisfiedLinkError`)을 해결했습니다.
- **빌드 충돌 해결:** `opencv` 모듈에서 발생하는 라이브러리 중복 오류를 `pickFirsts` 규칙으로 해결했습니다.
- [상세 분석 보고서](file:///Users/kyuyeonlee/.gemini/antigravity/brain/b8ebe01f-efb7-4b99-b915-7935c4c1b019/pytorch_issue_analysis_report.md)

## 검증 결과

- **Gradle Sync:** 성공적으로 완료
- **로그캣 확인:** `OpenCVLoader.initLocal()` 호출 시 성공 로그 확인 가능
- **파일 배치:** 4개 모든 ABI(`arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`)에 최신 라이브러리 배치 완료

> [!IMPORTANT]
> 로컬 환경에서 다시 한 번 **Rebuild Project**를 진행하여 모든 변경 사항이 최종 바이너리에 반영되었는지 확인해 주시기 바랍니다.
