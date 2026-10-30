# -*- coding: utf-8 -*-
"""Slot translations for README generation: Tiếng Việt (vi)."""

TEXTS = {
    'vi': {
        'title': 'Hiển thị giây trên đồng hồ (Show Seconds in Time)',
        'tagline': (
            'Hiển thị **giây** trên thanh trạng thái, chạm để bật/tắt. Có thể **tô màu từng chữ số** '
            'hoặc phủ **dải màu chuyển** lên cả chuỗi.<br>'
            '**Nhấn giữ** để mở cửa sổ kính mờ hiển thị **giờ mạng** (kéo, đổi kích thước, có cả dải màu); '
            '**tác vụ tự đặt** tự bật giây đúng giờ.<br>'
            'Phần lõi chỉ dùng API công khai của AOSP (lớp riêng của OPPO / OnePlus / realme là tuỳ chọn, '
            'không có thì bỏ qua). Đã kiểm chứng trên ColorOS 15 / 16.'
        ),
        'h_features': 'Tính năng',
        'th_feature': 'Tính năng',
        'th_desc': 'Mô tả',
        'rows': [
            ('Giây trên thanh trạng thái',
             '`HH:mm:ss` / `h:mm:ss a`, theo định dạng 12/24 giờ và múi giờ của hệ thống. Cuối chuỗi được thêm '
             '**khoảng trắng độ rộng 0** để module giao diện không xoá mất màu, và tự vẽ lại khi mất màu (nghỉ 5 phút).'),
            ('Chạm để chuyển',
             '**Chạm** vào thời gian để bật/tắt giây (có thể chọn **chạm hai lần**); **nhấn giữ** dành cho cửa sổ xem trước; '
             'trong khung giờ có tác vụ, chạm sẽ báo "tác vụ đang điều khiển".'),
            ('Màu từng chữ số / dải màu',
             'Sáu chữ số **mỗi số một màu** (10 màu sẵn + thanh HSV + theo hệ thống), dấu hai chấm tự căn quang học; '
             'hoặc **dải màu** từ màu đầu đến màu cuối (6 bộ sẵn, tự chọn hai đầu, nền sáng tự làm tối). '
             'Thẻ xem trước được **tự vẽ** nên khớp từng điểm ảnh với thanh trạng thái.'),
            ('Cửa sổ xem trước',
             'Cửa sổ kính mờ hiển thị **giờ mạng**; kéo để di chuyển, kéo góc ◣ để đổi kích thước, vị trí và kích thước '
             '**được ghi nhớ**; có dải màu, chữ số đều nhau.'),
            ('Tác vụ tự đặt',
             'Khung **mỗi giờ** hoặc **mỗi ngày**: bật giây trước N phút trong M phút rồi trả lại như cũ. '
             'Đổi giờ hệ thống hoặc múi giờ sẽ tính lại ngay, kèm bước dự phòng 60 giây.'),
            ('So sánh giờ mạng',
             'Hiển thị chênh lệch giữa **giờ mạng** và giờ máy; nhấn **Đồng bộ** để bật "tự động đặt giờ" của hệ thống.'),
            ('Không cần cấp quyền adb',
             'Ghi cấu hình ưu tiên qua `Settings.Secure`; nếu thiếu `WRITE_SECURE_SETTINGS` thì dùng broadcast để '
             'SystemUI ghi hộ — **không cần lệnh adb** cho bất kỳ tính năng nào.'),
            ('Ô trong bảng thông báo',
             'Thêm ô "Hiện giây" vào bảng thông báo để bật/tắt mà không cần mở ứng dụng.'),
            ('Tên tài nguyên đồng hồ',
             'Nếu không tự nhận ra, nhấn **Tự dò** hoặc tự nhập tên tài nguyên (ví dụ `clock`).'),
            ('21 ngôn ngữ giao diện',
             'Theo ngôn ngữ hệ thống: 简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, ไทย, '
             'Deutsch, Español, Français, Українська, Polski, Português (Brasil), فارسی, العربية, Bahasa Indonesia, '
             'Italiano, Ελληνικά, ქართული; ngôn ngữ khác dùng tiếng Anh.'),
            ('Lưu trữ / phạm vi',
             'Trạng thái lưu trong `Settings.Secure`, dùng chung giữa các tiến trình và giữ sau khi khởi động lại. '
             '**Đã kiểm chứng trên ColorOS 15 / 16 (Android 15 / 16)**; lõi chỉ dùng API công khai nên hãng khác '
             'cũng nên chạy được, nhưng mới thử trên OPPO / OnePlus. Tối thiểu Android 10.'),
        ],
        'h_colors': 'Kiểu tô màu',
        'colors_intro': (
            'Hai cách tô màu, chuyển trong thẻ **Màu chữ số**: **từng chữ số** (mỗi số một màu) '
            'hoặc **dải màu** cho cả chuỗi.'
        ),
        'cap_digit': 'Từng chữ số: mỗi số một màu, dấu hai chấm căn quang học',
        'cap_grad': 'Dải màu: từ màu đầu đến màu cuối, 6 bộ sẵn hoặc tự chọn',
        'h_install': 'Cài đặt',
        'install': [
            'Tải APK mới nhất từ [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest)',
            'Bật module trong **LSPosed** và chọn phạm vi **Giao diện hệ thống** (`com.android.systemui`)',
            'Khởi động lại tiến trình SystemUI hoặc khởi động lại máy',
            'Mở trang cài đặt từ **trình quản lý LSPosed** (không tạo biểu tượng ở màn hình chính)',
        ],
        'note1': (
            'Cần root (LSPosed). **Thường không cần quyền adb**: ghi qua `Settings.Secure`, thiếu '
            '`WRITE_SECURE_SETTINGS` thì dùng broadcast để SystemUI ghi hộ. Muốn trang cài đặt ghi **trực tiếp**, hãy cấp:'
        ),
        'note2': 'Không cấp cũng không ảnh hưởng: chạm, tác vụ và cửa sổ nổi đều chạy trong SystemUI.',
        'h_usage': 'Sử dụng',
        'usage': [
            '**Chạm vào thời gian**: bật/tắt giây (mặc định); có thể chọn **chạm hai lần** trong cài đặt.',
            '**Nhấn giữ thời gian**: cửa sổ kính mờ hiện **giờ mạng**, kéo để di chuyển, chạm vào thời gian để hiện nút '
            '**đóng** (trên phải) và **đổi cỡ** (dưới trái).',
            'Thẻ **Màu chữ số** có hai trang và chính thẻ tab là công tắc: **từng chữ số** hoặc **dải màu**.',
            'Thẻ **Tác vụ tự đặt** đặt các khung kiểu "bật giây trước mỗi giờ 2 phút".',
            'Việc chuyển dùng kiểm tra chạm toàn cục nên **không xung đột với việc kéo bảng thông báo**.',
            'Trang cài đặt hiển thị trạng thái, thao tác, màu, chênh lệch giờ mạng và nút **đồng bộ** một chạm.',
        ],
        'h_demo': 'Xem trước',
        'cap_off': 'Tắt giây: `15:06`, hàng chục giờ giữ màu đã chọn',
        'cap_on': 'Bật giây: `15:06:03`, phút và giây giữ màu hệ thống',
        'h_how': 'Nguyên lý',
        'how_intro': [
            'Module chỉ hook **SystemUI** và dùng API legacy Xposed ổn định.',
            '**Lõi chỉ dùng API công khai của AOSP, lớp riêng của hãng là tuỳ chọn và bỏ qua khi không có**, '
            'nên không phá trạng thái hệ thống và không kích hoạt chế độ an toàn của LSPosed:',
        ],
        'how': [
            '**Giây**: hook `TextView.setText` (after) để lưu văn bản gốc, rồi `TextView.onDraw` (before) ghi '
            '`gốc:ss`, cập nhật mỗi giây bằng `Handler`.',
            '**Màu từng số**: mỗi chữ số một `ForegroundColorSpan` (bỏ span lạ trước); dấu hai chấm được nâng '
            '0.11em bằng `CharacterStyle` vì bố cục nhiều run đặt glyph thấp hơn.',
            '**Dải màu**: `CharacterStyle` gọi `setShader` trong `updateDrawState`; thẻ xem trước **tự vẽ** '
            '(`GradientTimeView`) vì ColorOS 15 lượng tử hoá shader theo run.',
            '**Chuyển trạng thái**: bảng thông báo nuốt chạm trên thanh trạng thái nên module hook '
            '`View.dispatchTouchEvent` toàn cục và chỉ đổi khi điểm nhấn nằm trong đồng hồ và gần như không di chuyển. '
            'Không **tiêu thụ** sự kiện.',
            '**Tác vụ**: xác định "giờ địa phương có trong khung không", hẹn giờ chính xác ở biên và dự phòng 60 giây.',
        ],
        'h_power': 'Tiêu thụ điện',
        'power': [
            '**Bật giây**: cần hẹn giờ 1 giây (chi phí cố hữu). Đo SystemUI một nhân: 7.0% khi tắt, 7.25% khi bật (60 s).',
            '**Tắt giây**: hẹn giờ **dừng ngay** (`stopTicker`), không đánh thức mỗi giây.',
            '**Giữ màu** giãn 2s → 4s → … → 30s và tự dừng sau 5 phút không mất màu.',
            'Trạng thái **lưu trong tiến trình**, không đọc `Settings.Secure` mỗi giây.',
            'Dự phòng tác vụ dùng `Handler.postDelayed`: **không WakeLock, không AlarmManager**.',
        ],
        'h_build': 'Biên dịch',
        'build_intro': 'Cần **JDK 17+** (khuyến nghị 17 hoặc 21) và **Android SDK Platform 35** (AGP tự tải Build-Tools 35).',
        'h_backup': 'Sao lưu và tiếp tục phát triển (quan trọng)',
        'backup_intro': 'Trong thư mục `ClockSeconds/` giữ **mọi thứ trừ `build/` và `.gradle/`** (phần mã chỉ ~300 KB):',
        'th_backup_cat': 'Loại',
        'th_backup_files': 'Tệp',
        'th_backup_note': 'Ghi chú',
        'backup_rows': [
            ('Tệp biên dịch',
             '`settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties`, `keystore.properties`',
             'Phiên bản, phụ thuộc, tham số, ký'),
            ('Gradle Wrapper', '`gradlew`, `gradlew.bat`, `gradle/wrapper/*`', 'Ghim Gradle 8.14.5'),
            ('Mã nguồn', '`app/src/main/java/**` (20 tệp `.java`)', 'Logic module, trang cài đặt, lớp vẽ'),
            ('Tài nguyên', '`app/src/main/res/**`', 'Chuỗi (22 ngôn ngữ), chủ đề'),
            ('Siêu dữ liệu Xposed', '`app/src/main/resources/META-INF/xposed/*`', 'Lớp vào, phạm vi, mức API'),
            ('Manifest', '`app/src/main/AndroidManifest.xml`', 'Quyền, activity'),
            ('Script công cụ', '`tools/**`', 'Bộ tạo README, kiểm tra i18n, script phát hành'),
            ('Tài liệu', '`README*.md`, `docs/**`, `CHANGELOG.md`', 'Mô tả, ảnh chụp, nhật ký'),
            ('CI', '`.github/workflows/*.yml`', 'Bản ký khi tạo tag'),
        ],
        'h_trouble': 'Khắc phục',
        'trouble_summary': 'Xem các bước',
        'trouble_intro': 'Xem logcat:',
        'trouble': [
            ('b', '**Bản release** ẩn mọi log chẩn đoán sau `BuildConfig.DEBUG`.'),
            ('b', 'Khi cần gỡ lỗi, cài **bản debug** (`assembleDebug`, ký cùng khoá) rồi lọc:'),
            ('code', ['adb logcat -s ColorOSClockSeconds']),
            ('b', 'Không có giây: kiểm tra phạm vi System UI trong LSPosed và khởi động lại; sau đó đọc hoặc ghi đè công tắc:'),
            ('code', [
                'adb shell settings get secure coloros_clock_seconds_enabled   # 1=bật 0=tắt',
                'adb shell settings put  secure coloros_clock_seconds_enabled 1',
            ]),
            ('b', 'Màu hoặc giây tự trở về: hệ thống hoặc chủ đề đang dựng lại văn bản và module tự vẽ lại; '
                  'nếu mãi không hồi, tìm `color-guard stopped` / `span-theft` trong log debug.'),
            ('b', 'Không chạy trên máy không phải ColorOS: xem `candidate:` / `CONFIG CLOCK:` trong bản debug để biết '
                  'lớp hoặc tên tài nguyên đồng hồ có khớp quy tắc nhận diện không.'),
        ],
        'h_feedback': 'Phản hồi',
        'feedback': 'Câu hỏi, góp ý: mở [GitHub Issues](https://github.com/kelebao/ClockSeconds/issues).',
        'h_license': 'License',
    },
}
