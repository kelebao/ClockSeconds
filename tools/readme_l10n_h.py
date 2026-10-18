# -*- coding: utf-8 -*-
"""Slot translations for README generation: Tiếng Việt (vi)."""

TEXTS = {
    'vi': {
        'title': 'Hiển thị giây (Clock Seconds)',
        'tagline': (
            'Hiển thị **giây** trên đồng hồ thanh trạng thái, bật/tắt bằng một lần chạm, '
            'và giữ **hàng chục của giờ luôn màu đỏ**.<br>'
            'Không dùng bất kỳ lớp私有 nào của nhà sản xuất. Đã xác minh trên máy thật '
            'ColorOS 15; đồng thời hỗ trợ Xiaomi, OnePlus, Honor, vivo và các hãng lớn khác.'
        ),
        'h_features': 'Tính năng',
        'th_feature': 'Tính năng',
        'th_desc': 'Mô tả',
        'rows': [
            ('Hiển thị giây',
             'Đồng hồ thanh trạng thái hiển thị đến giây (`HH:mm:ss` / `h:mm:ss a`, theo cài đặt 12/24 giờ của hệ thống)'),
            ('Chạm để bật/tắt',
             '**Chạm** vào đồng hồ thanh trạng thái để bật/tắt hiển thị giây; cũng có thể đổi thành **nhấn giữ** trong trang cài đặt'),
            ('Chữ số giờ màu đỏ',
             '**Hàng chục của giờ** được tô màu đỏ (chữ `1` trong `13`, chữ `0` trong `05`); '
             'phút và giây giữ nguyên màu hệ thống. Giờ một chữ số (ví dụ `9:45`) không có hàng chục nên không bị đổi. '
             'Có hiệu lực trên mọi nhà sản xuất'),
            ('Ngôn ngữ giao diện',
             'Giao diện ứng dụng đã hỗ trợ **21 ngôn ngữ** (tự động theo ngôn ngữ hệ thống): '
             '简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, ไทย, '
             'Deutsch, Español, Français, Українська, Polski, Português (Brasil), '
             'فارسی, العربية, Bahasa Indonesia, Italiano, Ελληνικά, ქართული'),
            ('Lưu trữ bền vững',
             'Trạng thái lưu trong `Settings.Secure`, dùng chung giữa các tiến trình, giữ nguyên sau khi khởi động lại SystemUI'),
            ('Phạm vi tương thích',
             'Đã xác minh trên ColorOS 15 / 16 / 17 (OPPO / OnePlus / realme); cũng hoạt động ngay '
             'trên các hãng đã root khác như Xiaomi / Honor / Redmi. Không dùng lớp私有 của hãng nên '
             'không kích hoạt chế độ an toàn LSPosed. Hỗ trợ tối thiểu Android 10'),
        ],
        'h_install': 'Cài đặt',
        'install': [
            'Tải APK mới nhất từ trang [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest)',
            'Bật module trong **LSPosed**, phạm vi: **Giao diện hệ thống (`com.android.systemui`)**',
            'Khởi động lại tiến trình SystemUI hoặc khởi động lại thiết bị',
            'Mở trang cài đặt từ **trình quản lý LSPosed** (không tạo biểu tượng trên màn hình chính)',
        ],
        'note1': (
            'Cần root (LSPosed). Để ghi vào `Settings.Secure` cần cấp quyền '
            '`WRITE_SECURE_SETTINGS`:'
        ),
        'note2': (
            'Khi chưa cấp quyền vẫn có thể bật/tắt bằng cách chạm vào đồng hồ thanh trạng thái '
            '(thao tác này chạy bên trong tiến trình SystemUI, vốn đã có quyền sẵn).'
        ),
        'h_usage': 'Cách dùng',
        'usage': [
            '**Chạm vào đồng hồ thanh trạng thái**: bật/tắt hiển thị giây (cử chỉ mặc định).',
            'Việc bật/tắt được thực hiện bằng phép kiểm tra chạm toàn cục, nên **không bao giờ xung đột '
            'với "kéo xuống thanh thông báo"** — chạm thuần túy thì bật/tắt, vuốt vẫn kéo thanh thông báo '
            'như thường. Nếu quen hơn, bạn có thể đổi cử chỉ thành **nhấn giữ** trong trang cài đặt.',
            'Trang cài đặt cho xem trạng thái hiện tại, cử chỉ đang dùng và làm mới trạng thái.',
        ],
        'h_demo': 'Xem trước',
        'cap_off': 'Tắt giây: `15:06` — chữ số hàng chục của giờ `1` vẫn màu đỏ',
        'cap_on': 'Bật giây: `15:06:03` — phút và giây giữ màu hệ thống',
        'h_how': 'Nguyên lý hoạt động',
        'how_intro': [
            'Module chỉ hook **một tiến trình SystemUI**, theo mẫu legacy Xposed vững chắc,',
            '**hoàn toàn không chạm vào bất kỳ lớp私有 nào của nhà sản xuất**, nên không phá vỡ '
            'trạng thái nội bộ hệ thống và không kích hoạt chế độ an toàn LSPosed:',
        ],
        'how': [
            '**Hiển thị giây**: hook `TextView.setText` (after) ổn định để lưu văn bản gốc mà hệ thống '
            'đặt lên đồng hồ thanh trạng thái,\n'
            'sau đó hook `TextView.onDraw` (before) để ghi đè hiển thị thành `văn bản gốc:ss` '
            'ngay sau khi hệ thống đã đặt xong văn bản,\n'
            'với `Handler` chu kỳ 1 giây điều khiển. Giây được thêm vào giữ nguyên màu chữ của đồng hồ '
            'hệ thống (đo thực tế là `#ccffffff`).\n'
            'Đồng thời tô đỏ "hàng chục của giờ" (thêm một đoạn `ForegroundColorSpan` màu đỏ cho ký tự đó), '
            'dù bật hay tắt hiển thị giây đều giữ màu đỏ;\n'
            'phút và giây không bị tô đỏ; giờ một chữ số không có hàng chục nên không bị đổi.',
            '**Bật/tắt**: Bảng điều khiển thông báo của ColorOS chặn sự kiện chạm ở vùng thanh trạng thái, '
            'nên `OnTouchListener` của chính đồng hồ không nhận được sự kiện.\n'
            'Vì vậy module chuyển sang hook `View.dispatchTouchEvent` toàn cục, chỉ lật công tắc '
            '`Settings.Secure` khi điểm nhấn rơi trong biên màn hình\n'
            'của đồng hồ đã nhận diện và gần như không dịch chuyển (chạm) hoặc được giữ 500ms (nhấn giữ). '
            'Module **không tiêu thụ**\n'
            'bất kỳ sự kiện chạm nào, nên việc kéo xuống thanh thông báo vẫn như thường.',
        ],
        'h_power': 'Giải thích về tiết kiệm điện',
        'power': [
            '**Khi bật hiển thị giây**: module cần bộ đếm thời gian chu kỳ 1 giây để làm mới '
            '(đây là chi phí cố hữu của việc "hiện giây", khoảng 60 lần vẽ lại đồng hồ mỗi phút).',
            '**Khi tắt hiển thị giây**: module **dừng ngay bộ đếm thời gian đó** (`stopTicker`), '
            'không còn bất kỳ lần đánh thức mỗi giây, cũng không còn đọc liên tiến trình,\n'
            'trở về nhịp "làm mới mỗi phút một lần" vốn có của hệ thống, nên tắt hiển thị giây '
            'thực sự tiết kiệm điện hơn.',
            'Trạng thái được lưu trong **bộ nhớ đệm nội tiến trình** (`sSecondsEnabled`), bộ đếm thời gian '
            'không đọc `Settings.Secure` mỗi giây (tránh IPC mỗi giây).',
            'Module không tạo bất kỳ bộ định dạng thời gian nào, tái sử dụng trực tiếp văn bản gốc của '
            'hệ thống và chỉ nối thêm `:ss`, nên không có chi phí bộ định dạng.',
            'Bộ đếm thời gian chỉ giữ **tham chiếu yếu** tới view, và chỉ tiếp tục khi view '
            '`isAttachedToWindow()`, view bị hủy thì tự thoát.',
        ],
        'h_build': 'Build',
        'build_intro': (
            'Cần **JDK 17+** (khuyến nghị 17 hoặc 21) và **Android SDK Platform 35** '
            '(Build-Tools 35 do AGP tự cài).'
        ),
        'h_backup': 'Sao lưu & phát triển tiếp (quan trọng)',
        'backup_intro': (
            'Trong thư mục `ClockSeconds/`, **mọi file trừ `build/` và `.gradle/` '
            'đều phải giữ lại** (phần mã nguồn chỉ khoảng 140 KB):'
        ),
        'th_backup_cat': 'Loại',
        'th_backup_files': 'Tệp',
        'th_backup_note': 'Ghi chú',
        'backup_rows': [
            ('Script build',
             '`settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties`',
             'Phiên bản, phụ thuộc, tham số biên dịch'),
            ('Gradle Wrapper', '`gradlew`, `gradlew.bat`, `gradle/wrapper/*`', 'Cố định Gradle 8.14.5'),
            ('Mã nguồn', '`app/src/main/java/**` (3 tệp `.java`)', 'Thân logic của module'),
            ('Tài nguyên', '`app/src/main/res/**`',
             'Chuỗi (mặc định tiếng Trung + 21 ngôn ngữ values-xx), chủ đề'),
            ('Siêu dữ liệu Xposed', '`app/src/main/resources/META-INF/xposed/*`',
             'Lớp vào, phạm vi, phiên bản API'),
            ('Manifest', '`app/src/main/AndroidManifest.xml`', 'Quyền, Activity'),
            ('Tài liệu', '`README*.md`', 'Tài liệu 21 ngôn ngữ'),
            ('CI', '`.github/workflows/release.yml`', 'Đẩy tag là ra bản ký'),
        ],
        'h_trouble': 'Xử lý sự cố',
        'trouble_summary': 'Mở các bước xử lý sự cố',
        'trouble_intro': 'Xem logcat:',
        'trouble': [
            ('b', '**Bản build Release** đã dùng cổng `BuildConfig.DEBUG` để loại bỏ toàn bộ '
                  'log chẩn đoán (nhỏ hơn, sạch hơn); logcat không in ra log của module này.'),
            ('b', 'Khi cần xử lý sự cố, hãy tạm cài **bản build debug** (`assembleDebug`, ký bằng '
                  'cùng khóa production nên cài đè trực tiếp lên bản release), rồi lọc theo TAG:'),
            ('code', ['adb logcat -s ColorOSClockSeconds']),
            ('b', 'Nếu hoàn toàn không hiện giây: trước hết xác nhận LSPosed đã tick phạm vi SystemUI '
                  'và đã khởi động lại; sau đó kiểm tra công tắc có bị lưu ở trạng thái tắt không ——\n'
                  'có thể đọc / ghi cưỡng chế qua adb:'),
            ('code', [
                'adb shell settings get secure coloros_clock_seconds_enabled   # 1=bật 0=tắt',
                'adb shell settings put  secure coloros_clock_seconds_enabled 1',
            ]),
            ('b', 'Nếu không có tác dụng trên máy không phải ColorOS: cài bản debug xem log ở dòng '
                  '`candidate:` / `CONFIG CLOCK:`,\n'
                  'xác nhận tên tài nguyên / tên lớp của đồng hồ thanh trạng thái trên máy đó có nằm '
                  'trong quy tắc nhận diện không; nếu không thì chỉ cần bổ sung một dòng quy tắc.'),
        ],
        'h_feedback': 'Phản hồi',
        'feedback': 'Có câu hỏi, góp ý hoặc chia sẻ? Hãy để lại lời nhắn tại [GitHub Issues](https://github.com/kelebao/ClockSeconds/issues), tôi sẽ phản hồi sớm nhất có thể.',
        'h_license': 'License',
    },
}
