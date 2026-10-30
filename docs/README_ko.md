<div align="center">

[简体中文](../README.md) · [English](README_EN.md) · [繁體中文](README_zh-TW.md) · [日本語](README_ja.md) · **한국어** · [Türkçe](README_tr.md) · [Русский](README_ru.md) · [Tiếng Việt](README_vi.md) · [ไทย](README_th.md) · [Deutsch](README_de.md) · [Español](README_es.md) · [Français](README_fr.md) · [Українська](README_uk.md) · [Polski](README_pl.md) · [Português (Brasil)](README_pt-BR.md) · [فارسی](README_fa.md) · [العربية](README_ar.md) · [Bahasa Indonesia](README_id.md) · [Italiano](README_it.md) · [Ελληνικά](README_el.md) · [ქართული](README_ka.md)

</div>

<div align="center">

# 시간에 초 표시(Show Seconds in Time)

상태 표시줄 시간에 **초**를 표시하고 탭으로 전환합니다. **자리마다 색**을 지정하거나 문자열 전체에 **그라데이션**을 넣을 수 있습니다.<br>**길게 누르면** **네트워크 시간**을 보여 주는 반투명 창(드래그·크기 조절·그라데이션 지원)이 열리고, **사용자 지정 작업**으로 정시에 초를 자동으로 켤 수 있습니다.<br>핵심 경로는 AOSP 공개 API만 사용합니다(OPPO / OnePlus / realme 비공개 클래스는 선택 사항이며 없으면 건너뜀). ColorOS 15 / 16 실기기 검증 완료.

[![version](https://img.shields.io/badge/version-1.2.0-blue.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/releases/latest)
![android](https://img.shields.io/badge/Android-10%2B-brightgreen.svg?style=flat-square)
[![lsposed](https://img.shields.io/badge/LSPosed-module-orange.svg?style=flat-square)](https://github.com/Xposed-Modules-Repo/io.github.kelebao.colorosclockseconds/releases)
![coloros](https://img.shields.io/badge/ColorOS%2015%2F16-verified-success.svg?style=flat-square)
[![stars](https://img.shields.io/github/stars/kelebao/ClockSeconds.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/stargazers)
[![license](https://img.shields.io/badge/license-MIT-lightgrey.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/blob/main/LICENSE)

</div>

---

## 기능

| 기능 | 설명 |
|------|------|
| 상태 표시줄 초 표시 | `HH:mm:ss` / `h:mm:ss a`, 시스템 12/24시간제와 시간대를 따릅니다. 테마 모듈이 색 span을 지우지 못하도록 끝에 **제로 폭 공백**을 붙이고, 색이 사라지면 자동으로 다시 그립니다(5분 쿨다운). |
| 탭으로 전환 | 시간을 **탭**하면 초 켜기/끄기(**더블 탭** 선택 가능). **길게 누르기**는 플로팅 미리보기 전용이며, 작업 시간대에는 "작업이 처리 중" 안내를 표시합니다. |
| 자리별 색 / 그라데이션 | 6자리를 **각각 색 지정**(10가지 프리셋 + HSV 슬라이더 + 시스템 따르기), 콜론은 자동으로 광학 중앙 정렬. 또는 문자열 전체 **그라데이션**(시작 색 → 끝 색, 6가지 프리셋, 양 끝 사용자 지정, 밝은 배경은 자동으로 어둡게). 미리보기는 **직접 그려** 상태 표시줄과 일치합니다. |
| 길게 누르기 미리보기 | 반투명 창에 **네트워크 시간**을 표시합니다. 드래그로 이동, ◣ 으로 크기 조절, 위치와 크기는 **자동 저장**. 그라데이션 지원, 숫자는 고정폭. |
| 사용자 지정 작업 | **매시** 또는 **매일** 시간대: N분 전에 초를 켜고 M분 뒤 원래대로. 시스템 시간/시간대가 바뀌면 즉시 재계산하며 60초 백업 계산도 있습니다. |
| 네트워크 시간 비교 | **네트워크 시간**과 기기 시간의 차이를 표시합니다. **동기화**를 누르면 시스템 "자동 설정"이 켜집니다. |
| adb 권한 불필요 | 쓰기는 `Settings.Secure`를 우선하고, `WRITE_SECURE_SETTINGS`가 없으면 브로드캐스트로 SystemUI가 대신 기록합니다 — **어떤 기능도 adb 명령이 필요 없습니다**. |
| 빠른 설정 타일 | 알림 창에 "초 표시" 타일을 추가해 앱을 열지 않고 전환할 수 있습니다. |
| 시계 리소스 이름 | 자동 인식이 안 되면 **자동 감지**를 누르거나 리소스 이름(예: `clock`)을 직접 입력하세요. |
| UI 21개 언어 | 시스템 언어를 따릅니다: 简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, ไทย, Deutsch, Español, Français, Українська, Polski, Português (Brasil), فارسی, العربية, Bahasa Indonesia, Italiano, Ελληνικά, ქართული. 그 외는 영어로 표시됩니다. |
| 저장 / 지원 범위 | 상태는 `Settings.Secure`에 저장되어 프로세스 간 공유되고 재부팅 후에도 유지됩니다. **ColorOS 15 / 16(Android 15 / 16) 실기기 검증 완료**; 핵심은 공개 API만 사용하므로 다른 제조사에서도 동작할 것으로 보지만 OPPO / OnePlus 기기만 테스트했습니다. 최소 Android 10. |

## 색상 모드

색을 입히는 방법은 두 가지이며 **숫자 색상** 카드에서 전환합니다: **자리별**(자리마다 한 색) 또는 문자열 전체 **그라데이션**.

<table><tr>
<td align="center" width="50%">
<img src="screenshots/colors-digit.png" alt="자리별: 6자리 각각 색 지정, 콜론은 광학 중앙 정렬" width="100%">
<br>
자리별: 6자리 각각 색 지정, 콜론은 광학 중앙 정렬
</td>
<td align="center" width="50%">
<img src="screenshots/colors-gradient.png" alt="그라데이션: 시작 색 → 끝 색, 6가지 프리셋 또는 양 끝 지정" width="100%">
<br>
그라데이션: 시작 색 → 끝 색, 6가지 프리셋 또는 양 끝 지정
</td>
</tr></table>

## 설치

1. [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest)에서 최신 APK 다운로드
2. **LSPosed**에서 모듈을 활성화하고 범위를 **시스템 UI**(`com.android.systemui`)로 지정
3. SystemUI 프로세스를 재시작하거나 기기를 재부팅
4. 설정 화면은 **LSPosed 매니저**에서 엽니다(런처 아이콘은 생성되지 않음)

> root(LSPosed)가 필요합니다. **보통 adb 권한은 필요 없습니다**: 쓰기는 `Settings.Secure`를 우선하고, `WRITE_SECURE_SETTINGS`가 없으면 SystemUI가 브로드캐스트로 대신 기록합니다. 설정 화면에서 **직접 쓰기**를 원하면 다음을 부여하세요:
> ```bash
> adb shell pm grant io.github.kelebao.colorosclockseconds android.permission.WRITE_SECURE_SETTINGS
> ```
> 부여하지 않아도 사용에 문제는 없습니다. 탭, 작업 예약, 플로팅 미리보기는 모두 SystemUI 프로세스 안에서 처리됩니다.

## 사용법

- **상태 표시줄 시간을 탭**: 초 켜기/끄기(기본 동작). 설정에서 **더블 탭**으로 바꿀 수 있습니다.
- **시간을 길게 누르기**: **네트워크 시간** 반투명 창. 드래그로 이동, 시간을 탭하면 **닫기**(오른쪽 위)와 **크기 조절**(왼쪽 아래) 버튼이 나타납니다.
- **숫자 색상** 카드는 두 페이지이며 탭 자체가 스위치입니다: **자리별**은 6자리를 개별 색칠, **그라데이션**은 문자열 전체에 색 띠를 입힙니다.
- **사용자 지정 작업** 카드에서 "매시 2분 전에 초 켜기" 같은 예약을 만들 수 있습니다.
- 전환은 전역 터치 판정으로 처리되어 **알림창 내리기와 충돌하지 않습니다**.
- 설정 화면에서 현재 상태, 제스처, 색상, 네트워크 시간 차이와 원탭 **동기화**를 확인할 수 있습니다.

## 미리보기

<table><tr>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-off.jpg" alt="초 끔: `15:06`, 시(십의 자리)는 선택한 색 유지" width="100%">
<br>
초 끔: `15:06`, 시(십의 자리)는 선택한 색 유지
</td>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-on.jpg" alt="초 켬: `15:06:03`, 분과 초는 시스템 색 유지" width="100%">
<br>
초 켬: `15:06:03`, 분과 초는 시스템 색 유지
</td>
</tr></table>

## 동작 원리

모듈은 **SystemUI 하나만** hook하며 안정적인 legacy Xposed API를 씁니다.
**핵심 경로는 AOSP 공개 API만 사용하고 제조사 비공개 클래스는 선택 사항(없으면 건너뜀)**이므로 시스템 상태를 깨뜨리지 않고 LSPosed 안전 모드도 유발하지 않습니다:

- **초**: `TextView.setText`(after)로 시스템 원문을 캐시하고, `TextView.onDraw`(before)에서 `원문:ss`로 덮어쓴 뒤 1초 주기 `Handler`로 갱신합니다.
- **자리별 색**: 자리마다 `ForegroundColorSpan`(외부 span은 먼저 제거). 콜론에는 `CharacterStyle`로 0.11em 올립니다(여러 run으로 그리면 호스트 글리프가 내려가기 때문).
- **그라데이션**: `CharacterStyle`의 `updateDrawState`에서 `setShader`. 설정 화면 미리보기는 **직접 그리며**(`GradientTimeView`), ColorOS 15는 shader를 run 단위로 양자화합니다.
- **전환**: 알림 패널이 상태 표시줄 터치를 가로채므로 전역 `View.dispatchTouchEvent`를 hook하고, 시간 영역 안에서 거의 움직이지 않은 누름일 때만 스위치를 뒤집습니다. 이벤트를 **소비하지 않습니다**.
- **작업**: "로컬 시간이 창 안에 있는가"로 판정하고 경계 정확 타이머 + 60초 백업을 사용하며, 시간/시간대 변경 시 즉시 재계산합니다.

## 전력

- **초 켬**: 1초 타이머가 갱신을 구동합니다(초 표시의 본질적 비용). 실측 SystemUI 단일 코어: 끔 7.0%, 켬 7.25%(60초 구간)로 구간별 변동보다 작습니다.
- **초 끔**: 타이머를 **즉시 중지**(`stopTicker`)하며 매초 깨우기와 프로세스 간 읽기가 없습니다.
- **색 유지**는 2s → 4s → … → 30s로 간격을 늘리고 5분 동안 손실이 없으면 종료합니다(`color-guard stopped`).
- 상태는 **프로세스 내 캐시**라 매초 `Settings.Secure`를 읽지 않습니다.
- 작업 백업은 `Handler.postDelayed`: **WakeLock도 AlarmManager도 사용하지 않아** 깊은 잠을 깨우지 않습니다.
- 타이머는 뷰의 **약한 참조**를 들고 있다가 분리되면 종료합니다.

## 빌드

**JDK 17+**(17 또는 21 권장)와 **Android SDK Platform 35**가 필요합니다(Build-Tools 35는 AGP가 내려받습니다).

```bash
export JAVA_HOME=/path/to/jdk-17-or-21
./gradlew assembleDebug
```

## 백업 및 개발 이어가기(중요)

`ClockSeconds/` 폴더에서 **`build/`와 `.gradle/`를 제외한 모든 파일**을 보관하세요(소스는 약 300 KB):

| 분류 | 파일 | 설명 |
|------|------|------|
| 빌드 스크립트 | `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties`, `keystore.properties` | 버전, 의존성, 컴파일 옵션, 서명 |
| Gradle Wrapper | `gradlew`, `gradlew.bat`, `gradle/wrapper/*` | Gradle 8.14.5 고정 |
| 소스 | `app/src/main/java/**`(`.java` 20개) | 모듈 로직, 설정 화면, 그리기 클래스 |
| 리소스 | `app/src/main/res/**` | 문자열(22개 로케일), 테마 |
| Xposed 메타데이터 | `app/src/main/resources/META-INF/xposed/*` | 엔트리 클래스, 스코프, API 버전 |
| 매니페스트 | `app/src/main/AndroidManifest.xml` | 권한, 액티비티 |
| 도구 스크립트 | `tools/**` | README 생성기, i18n 검사, 릴리스 스크립트 |
| 문서 | `README*.md`, `docs/**`, `CHANGELOG.md` | 설명, 스크린샷, 변경 이력 |
| CI | `.github/workflows/*.yml` | 태그 시 서명 빌드 |

## 문제 해결

<details>
<summary>해결 단계 펼치기</summary>

logcat 확인:

- **릴리스 빌드**는 `BuildConfig.DEBUG` 뒤에 모든 진단 로그를 숨겨 logcat에 아무것도 출력하지 않습니다.
- 디버깅에는 **debug 빌드**(`assembleDebug`, 같은 키로 서명되어 덮어쓰기 설치 가능)를 설치하고 다음으로 필터링하세요:

  ```bash
  adb logcat -s ColorOSClockSeconds
  ```

- 초가 전혀 안 나오면: LSPosed 범위에 시스템 UI가 있는지 확인하고 재시작한 뒤, 스위치 값을 확인하거나 강제로 기록하세요:

  ```bash
  adb shell settings get secure coloros_clock_seconds_enabled   # 1=on 0=off
  adb shell settings put  secure coloros_clock_seconds_enabled 1
  ```

- 색이나 초가 저절로 되돌아가면 시스템/테마가 텍스트를 다시 만들고 있는 것이며 모듈이 다시 그립니다. 오래 지속되면 debug 로그에서 `color-guard stopped` / `span-theft`를 찾아보세요.
- ColorOS가 아닌 기기에서 안 되면 debug 빌드로 `candidate:` / `CONFIG CLOCK:`을 확인해 시계 클래스/리소스 이름이 감지 규칙에 맞는지 보세요.

</details>

## 피드백

질문이나 제안은 [GitHub Issues](https://github.com/kelebao/ClockSeconds/issues)에 남겨 주세요.

## License

[MIT](LICENSE) © kelebao
