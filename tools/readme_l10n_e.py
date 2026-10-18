# -*- coding: utf-8 -*-
"""Slot translations for README generation: 한국어 (ko)."""

TEXTS = {
    'ko': {
        'title': '시계 초 단위 표시（Clock Seconds）',
        'tagline': (
            '상태 표시줄 시계에 **초**를 표시합니다. 한 번 탑으로 전환하고,'
            '시간 십의 자리는 **항상 빨강**.<br>'
            '제조사私有 클래스에 전혀 의존하지 않으며 ColorOS 15 실기기에서 검증되었습니다.'
            '샤오미·원플러스·아너·vivo 등 주요 제조사도 지원합니다.'
        ),
        'h_features': '기능',
        'th_feature': '기능',
        'th_desc': '설명',
        'rows': [
            ('초 표시',
             '상태 표시줄 시계가 초까지 표시（`HH:mm:ss` / `h:mm:ss a`, 시스템 12/24시간제 따름）'),
            ('탭하여 전환',
             '상태 표시줄 시계를 **탭**하기만 하면 초 표시 켜기/끄기 전환. 설정 페이지에서 **길게 누르기**로 변경 가능'),
            ('빨간 시간 자릿수',
             '시간의 「십의 자릿수」를 빨간색으로 표시（`13`의 `1`, `05`의 `0`）.'
             '분과 초는 시스템 원래 색상을 유지. 한 자릿수 시간（`9:45` 등）은 십의 자리가 없어 미적용. 모든 제조사에서 유효'),
            ('UI 언어',
             '앱 UI가 **21개 언어**를 지원（시스템 언어 자동 전환）: '
             '简体中文、English、繁體中文、日本語、한국어、Türkçe、Русский、'
             'Tiếng Việt、ไทย、Deutsch、Español、Français、Українська、Polski、'
             'Português (Brasil)、فارسی、العربية、Bahasa Indonesia、Italiano、'
             'Ελληνικά、ქართული'),
            ('영속성',
             '상태는 `Settings.Secure`에 저장. 프로세스 간 공유되며 SystemUI 재시작 후에도 유지'),
            ('지원 범위',
             'ColorOS 15 / 16 / 17（OPPO / 원플러스 / 리얼미） 실기기 검증 완료. '
             '샤오미 / 아너 / 레드미 등 루팅된 다른 제조사에서도 그대로 동작. '
             '제조사私有 클래스에 의존하지 않아 LSPosed 안전 모드가 발동하지 않음. 최소 Android 10'),
        ],
        'h_install': '설치',
        'install': [
            '[**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest)에서 최신 APK 다운로드',
            '**LSPosed**에서 모듈 활성화. 스코프: **시스템 UI（`com.android.systemui`）**',
            'SystemUI 프로세스 재시작 또는 기기 재부팅',
            '설정 페이지는 **LSPosed 매니저**에서 열기（바탕화면 아이콘 생성 안 됨）',
        ],
        'note1': (
            '루트（LSPosed）가 필요합니다. `Settings.Secure` 쓰기에는 '
            '`WRITE_SECURE_SETTINGS` 권한 부여가 필요합니다:'
        ),
        'note2': (
            '권한이 없어도 상태 표시줄 시계를 탭하면 전환할 수 있습니다'
            '（해당 동작은 SystemUI 프로세스 내에서 실행되어 본래 권한을 보유）.'
        ),
        'h_usage': '사용법',
        'usage': [
            '**상태 표시줄 시계 탭**: 초 표시 켜기/끄기 전환（기본 제스처）.',
            '전환은 전역 터치 히트 테스트로 구현되어 **「알림창 내리기」와 충돌하지 않습니다**——'
            '순수 탭은 전환, 스와이프는 기존처럼 알림창을 내립니다. '
            '설정 페이지에서 **길게 누르기**로 변경할 수도 있습니다.',
            '설정 페이지에서 현재 상태·제스처 설정을 확인하고 새로고침할 수 있습니다.',
        ],
        'h_demo': '미리보기',
        'cap_off': '초 표시 끔: `15:06`, 시의 십의 자리 `1`은 빨간색 유지',
        'cap_on': '초 표시 켜짐: `15:06:03`, 분과 초는 시스템 기본 색상',
        'h_how': '작동 원리',
        'how_intro': [
            '모듈은 **SystemUI** 프로세스만 hook하며, 검증된 legacy Xposed 방식을 사용하고,',
            '**제조사私有 클래스는 절대 건드리지 않으므로** 시스템 내부 상태를 망가뜨리거나 '
            'LSPosed 안전 모드를 발동시키지 않습니다:',
        ],
        'how': [
            '**초 표시**: 안정적인 `TextView.setText`（after）를 hook하여 시스템이 상태 표시줄 '
            '시계에 설정한 원시 텍스트를 캐시하고,\n'
            '다음으로 `TextView.onDraw`（before）를 hook하여 시스템이 텍스트를 설정한 후 '
            '표시를 `원시 텍스트:ss`로 덮어씁니다.\n'
            '1초 주기의 `Handler`로 구동. 추가되는 초는 시계 자체의 글자색을 계승（실측 `#ccffffff`）.\n'
            '함께 시간 십의 자릿수를 빨간색으로 표시（해당 한 문자에 빨간 `ForegroundColorSpan` 추가）. '
            '초 표시 켜기/끄기와 관계없이 항상 빨강;\n'
            '분과 초는 빨갛게 하지 않으며, 한 자릿수 시간은 십의 자리가 없어 미적용.',
            '**전환**: ColorOS의 알림 패널이 상태 표시줄 영역의 터치를 가로채므로 '
            '시계 자체의 `OnTouchListener`에는 이벤트가 도달하지 않습니다.\n'
            '따라서 모듈은 시스템 전역 `View.dispatchTouchEvent`를 hook하고, 인식된 시계의 '
            '화면 경계 안에 누른 지점이 있으며\n'
            '거의 이동이 없（탭）거나 500ms간 길게 눌린（길게 누르기） 경우에만 '
            '`Settings.Secure` 스위치를 반전시킵니다. 모듈은 터치 이벤트를\n'
            '**소비하지 않으므로** 알림창 내리기는 기존대로 동작합니다.',
        ],
        'h_power': '절전 설명',
        'power': [
            '**초 표시 켜기**: 모듈이 1초 주기 타이머로 새로고침을 구동합니다'
            '（초 표시의 고유 비용으로, 시계 재그리기는 분당 약 60회）.',
            '**초 표시 끄기**: 모듈이 해당 타이머를 **즉시 중단**（`stopTicker`）합니다. '
            '매초 깨우기도 프로세스 간 읽기도 전혀 없습니다.\n'
            '시스템 본래의 「1분에 1회 갱신」 리듬으로 돌아가므로 초 표시를 끄면 확실히 절전됩니다.',
            '상태는 **프로세스 내 캐시**（`sSecondsEnabled`）로 유지. '
            '타이머가 매초 `Settings.Secure`를 읽지 않습니다（매초 1회 IPC 회피）.',
            '시간 포맷터를 전혀 생성하지 않습니다. 시스템 원시 텍스트를 재사용하여 `:ss`만 '
            '추가하므로 포맷터 오버헤드가 제로입니다.',
            '타이머가 보유한 것은 뷰의 **약한 참조**뿐이며 뷰가 `isAttachedToWindow()`인 동안만 '
            '지속. 뷰 소멸 후 자동 종료.',
        ],
        'h_build': '빌드',
        'build_intro': (
            '**JDK 17+**（17 또는 21 권장）와 **Android SDK Platform 35**'
            '（Build-Tools 35는 AGP가 자동 설치）가 필요합니다.'
        ),
        'h_backup': '백업 및 후속 개발（중요）',
        'backup_intro': (
            '`ClockSeconds/` 디렉터리에서는 **`build/`, `.gradle/`를 제외한 '
            '모든 파일**을 보관하세요（소스 부분은 약 140 KB에 불가）:'
        ),
        'th_backup_cat': '분류',
        'th_backup_files': '파일',
        'th_backup_note': '설명',
        'backup_rows': [
            ('빌드 스크립트',
             '`settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties`',
             '버전·종속·컴파일 설정'),
            ('Gradle Wrapper', '`gradlew`, `gradlew.bat`, `gradle/wrapper/*`', 'Gradle 8.14.5 고정'),
            ('소스 코드', '`app/src/main/java/**`（`.java` 3개）', '모듈 로직 본체'),
            ('리소스', '`app/src/main/res/**`',
             '문자열（기본 중국어 + 21개 언어 values-xx）, 테마'),
            ('Xposed 메타데이터', '`app/src/main/resources/META-INF/xposed/*`',
             '진입 클래스·scope·API 버전'),
            ('매니페스트', '`app/src/main/AndroidManifest.xml`', '권한, Activity'),
            ('문서', '`README*.md`', '21개 언어 설명 문서'),
            ('CI', '`.github/workflows/release.yml`', 'tag 푸시로 서명 빌드'),
        ],
        'h_trouble': '문제 해결',
        'trouble_summary': '문제 해결 단계 펼치기',
        'trouble_intro': 'logcat을 확인하세요:',
        'trouble': [
            ('b', '**Release 빌드**는 `BuildConfig.DEBUG` 게이트로 모든 진단 로그를 '
                  '제거（더 작고 깔끔）. logcat에 본 모듈 로그가 출력되지 않습니다.'),
            ('b', '조사가 필요하면 임시로 **debug 빌드**（`assembleDebug`, 프로덕션 키로 '
                  '서명되어 그대로 덮어설치 가능）를 설치하고 TAG로 필터링:'),
            ('code', ['adb logcat -s ColorOSClockSeconds']),
            ('b', '초가 전혀 표시되지 않을 때: 먼저 LSPosed에서 SystemUI 스코프를 체크하고 '
                  '재시작했는지 확인. 다음으로 스위치가 꺼진 채 영속화되지 않았는지 '
                  'adb에서 읽기/강제 쓰기:'),
            ('code', [
                'adb shell settings get secure coloros_clock_seconds_enabled   # 1=켜기 0=끄기',
                'adb shell settings put  secure coloros_clock_seconds_enabled 1',
            ]),
            ('b', '비 ColorOS 기기에서 동작하지 않을 때: debug 빌드를 설치하고 로그의 '
                  '`candidate:` / `CONFIG CLOCK:` 출력을 보아 해당 기기 상태 표시줄 시계의 '
                  '리소스명·클래스명이 인식 규칙에 포함되는지 확인. 없다면 규칙 한 줄 추가면 충분.'),
        ],
        'h_feedback': '피드백',
        'feedback': '질문, 제안 또는 사용 후기가 있으시면 [GitHub Issues](https://github.com/kelebao/ClockSeconds/issues)에 남겨 주세요. 가능한 한 빨리 답변드리겠습니다.',
        'h_license': 'License',
    },
}
