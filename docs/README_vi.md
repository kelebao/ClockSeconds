<div align="center">

[简体中文](../README.md) · [English](README_EN.md) · [繁體中文](README_zh-TW.md) · [日本語](README_ja.md) · [한국어](README_ko.md) · [Türkçe](README_tr.md) · [Русский](README_ru.md) · **Tiếng Việt** · [ไทย](README_th.md) · [Deutsch](README_de.md) · [Español](README_es.md) · [Français](README_fr.md) · [Українська](README_uk.md) · [Polski](README_pl.md) · [Português (Brasil)](README_pt-BR.md) · [فارسی](README_fa.md) · [العربية](README_ar.md) · [Bahasa Indonesia](README_id.md) · [Italiano](README_it.md) · [Ελληνικά](README_el.md) · [ქართული](README_ka.md)

</div>

<div align="center">

# Hiển thị giây（Show Seconds in Time）

Hiển thị **giây** trên thời gian thanh trạng thái, bật/tắt bằng một lần chạm, và giữ **hàng chục của giờ luôn màu đỏ**.<br>Không dùng bất kỳ lớp私有 nào của nhà sản xuất. Đã xác minh trên máy thật ColorOS 15; đồng thời hỗ trợ Xiaomi, OnePlus, Honor, vivo, Samsung và các hãng lớn khác.

[![version](https://img.shields.io/badge/version-1.0.9-blue.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/releases/latest)
![android](https://img.shields.io/badge/Android-10%2B-brightgreen.svg?style=flat-square)
[![lsposed](https://img.shields.io/badge/LSPosed-module-orange.svg?style=flat-square)](https://github.com/Xposed-Modules-Repo/io.github.kelebao.colorosclockseconds/releases)
![coloros](https://img.shields.io/badge/ColorOS%2015-verified-success.svg?style=flat-square)
[![stars](https://img.shields.io/github/stars/kelebao/ClockSeconds.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/stargazers)
[![license](https://img.shields.io/badge/license-MIT-lightgrey.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/blob/main/LICENSE)

</div>

---

## Tính năng

| Tính năng | Mô tả |
|------|------|
| Hiển thị giây | Thời gian thanh trạng thái hiển thị đến giây (`HH:mm:ss` / `h:mm:ss a`, theo cài đặt 12/24 giờ của hệ thống) |
| Chạm để bật/tắt | **Chạm** vào thời gian thanh trạng thái để bật/tắt hiển thị giây; cũng có thể đổi thành **nhấn đúp** trong trang cài đặt |
| Màu chữ số giờ | Màu **hàng chục của giờ** có thể **tùy chỉnh** (8 màu) hoặc **tắt** để trở lại màu hệ thống; giờ một chữ số (ví dụ `9:45`) không có hàng chục nên không bị đổi. Có hiệu lực trên mọi nhà sản xuất |
| Xem trước khi nhấn giữ | **Nhấn giữ** thời gian thanh trạng thái để hiện **cửa sổ nổi mờ** hiển thị **giờ mạng** thời gian thực; kéo để di chuyển, kéo ◣ góc dưới trái để đổi kích thước, nhấn vào thời gian để hiện nút **đóng** (✕ góc phải) / **kéo giãn**; cỡ chữ tự theo cửa sổ |
| Giờ mạng | Trang cài đặt so sánh đồng hồ với **giờ chuẩn mạng** theo thời gian thực; nếu lệch lớn hãy nhấn **Đồng bộ** để bật tự động đặt giờ hệ thống |
| Không cần adb | Cài đặt ghi vào `Settings.Secure`; nếu không có quyền `WRITE_SECURE_SETTINGS`, module tự chuyển sang broadcast do SystemUI xử lý — **không cần cấp quyền adb** |
| Ngôn ngữ giao diện | Giao diện ứng dụng đã hỗ trợ **21 ngôn ngữ** (tự động theo ngôn ngữ hệ thống): 简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, ไทย, Deutsch, Español, Français, Українська, Polski, Português (Brasil), فارسی, العربية, Bahasa Indonesia, Italiano, Ελληνικά, ქართული |
| Lưu trữ bền vững | Trạng thái lưu trong `Settings.Secure`, dùng chung giữa các tiến trình, giữ nguyên sau khi khởi động lại SystemUI |
| Phạm vi tương thích | Đã xác minh trên ColorOS 15 / 16 / 17 (OPPO / OnePlus / realme); cũng hoạt động ngay trên các hãng đã root khác như Xiaomi / Honor / Redmi / Samsung. Không dùng lớp私有 của hãng nên không kích hoạt chế độ an toàn LSPosed. Hỗ trợ tối thiểu Android 10 |
| Ô vuông nhanh | Thêm ô "Hiện giây" vào bảng thông báo, bật/tắt bằng một chạm |
| Cải tiến float | Vị trí & kích thước float được **nhớ**; **nhấn đúp** thời gian cũng bật/tắt giây |
| Tiết kiệm pin | Bật tiết kiệm pin thì tạm dừng làm mới từng giây (giây đông cứng), tắt thì phục hồi ngay |
| Nhận diện cấu hình | Nếu đồng hồ không tự nhận diện, hãy nhấn **Tự phát hiện** trong Cài đặt, hoặc nhập tên tài nguyên (vd clock) thủ công |

## Cài đặt

1. Tải APK mới nhất từ trang [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest)
2. Bật module trong **LSPosed**, phạm vi: **Giao diện hệ thống (`com.android.systemui`)**
3. Khởi động lại tiến trình SystemUI hoặc khởi động lại thiết bị
4. Mở trang cài đặt từ **trình quản lý LSPosed** (không tạo biểu tượng trên màn hình chính)

> Cần root (LSPosed). Để ghi vào `Settings.Secure` cần cấp quyền `WRITE_SECURE_SETTINGS`:
> ```bash
> adb shell pm grant io.github.kelebao.colorosclockseconds android.permission.WRITE_SECURE_SETTINGS
> ```
> Khi chưa cấp quyền vẫn có thể bật/tắt bằng cách chạm vào thời gian thanh trạng thái (thao tác này chạy bên trong tiến trình SystemUI, vốn đã có quyền sẵn).

## Cách dùng

- **Chạm vào thời gian thanh trạng thái**: bật/tắt hiển thị giây (cử chỉ mặc định).
- Việc bật/tắt được thực hiện bằng phép kiểm tra chạm toàn cục, nên **không bao giờ xung đột với "kéo xuống thanh thông báo"** — chạm thuần túy thì bật/tắt, vuốt vẫn kéo thanh thông báo như thường. Nếu quen hơn, bạn có thể đổi cử chỉ thành **nhấn đúp** trong trang cài đặt.
- Trang cài đặt cho xem trạng thái hiện tại, cử chỉ đang dùng và làm mới trạng thái.

## Xem trước

<table><tr>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-off.jpg" alt="Tắt giây: `15:06` — chữ số hàng chục của giờ `1` vẫn màu đỏ" width="100%">
<br>
Tắt giây: `15:06` — chữ số hàng chục của giờ `1` vẫn màu đỏ
</td>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-on.jpg" alt="Bật giây: `15:06:03` — phút và giây giữ màu hệ thống" width="100%">
<br>
Bật giây: `15:06:03` — phút và giây giữ màu hệ thống
</td>
</tr></table>

## Nguyên lý hoạt động

Module chỉ hook **một tiến trình SystemUI**, theo mẫu legacy Xposed vững chắc,
**hoàn toàn không chạm vào bất kỳ lớp私有 nào của nhà sản xuất**, nên không phá vỡ trạng thái nội bộ hệ thống và không kích hoạt chế độ an toàn LSPosed:

- **Hiển thị giây**: hook `TextView.setText` (after) ổn định để lưu văn bản gốc mà hệ thống đặt lên thời gian thanh trạng thái,
  sau đó hook `TextView.onDraw` (before) để ghi đè hiển thị thành `văn bản gốc:ss` ngay sau khi hệ thống đã đặt xong văn bản,
  với `Handler` chu kỳ 1 giây điều khiển. Giây được thêm vào giữ nguyên màu chữ của thời gian hệ thống (đo thực tế là `#ccffffff`).
  Đồng thời tô đỏ "hàng chục của giờ" (thêm một đoạn `ForegroundColorSpan` màu đỏ cho ký tự đó), dù bật hay tắt hiển thị giây đều giữ màu đỏ;
  phút và giây không bị tô đỏ; giờ một chữ số không có hàng chục nên không bị đổi.
- **Bật/tắt**: Bảng điều khiển thông báo của ColorOS chặn sự kiện chạm ở vùng thanh trạng thái, nên `OnTouchListener` của chính thời gian không nhận được sự kiện.
  Vì vậy module chuyển sang hook `View.dispatchTouchEvent` toàn cục, chỉ lật công tắc `Settings.Secure` khi điểm nhấn rơi trong biên màn hình
  của thời gian đã nhận diện và gần như không dịch chuyển (chạm) hoặc là lần chạm thứ hai trong 350ms (nhấn đúp). Module **không tiêu thụ**
  bất kỳ sự kiện chạm nào, nên việc kéo xuống thanh thông báo vẫn như thường.

## Giải thích về tiết kiệm điện

- **Khi bật hiển thị giây**: module cần bộ đếm thời gian chu kỳ 1 giây để làm mới (đây là chi phí cố hữu của việc "hiện giây", khoảng 60 lần vẽ lại thời gian mỗi phút).
- **Khi tắt hiển thị giây**: module **dừng ngay bộ đếm thời gian đó** (`stopTicker`), không còn bất kỳ lần đánh thức mỗi giây, cũng không còn đọc liên tiến trình,
  trở về nhịp "làm mới mỗi phút một lần" vốn có của hệ thống, nên tắt hiển thị giây thực sự tiết kiệm điện hơn.
- Trạng thái được lưu trong **bộ nhớ đệm nội tiến trình** (`sSecondsEnabled`), bộ đếm thời gian không đọc `Settings.Secure` mỗi giây (tránh IPC mỗi giây).
- Module không tạo bất kỳ bộ định dạng thời gian nào, tái sử dụng trực tiếp văn bản gốc của hệ thống và chỉ nối thêm `:ss`, nên không có chi phí bộ định dạng.
- Bộ đếm thời gian chỉ giữ **tham chiếu yếu** tới view, và chỉ tiếp tục khi view `isAttachedToWindow()`, view bị hủy thì tự thoát.

## Build

Cần **JDK 17+** (khuyến nghị 17 hoặc 21) và **Android SDK Platform 35** (Build-Tools 35 do AGP tự cài).

```bash
export JAVA_HOME=/path/to/jdk-17-or-21
./gradlew assembleDebug
```

## Sao lưu & phát triển tiếp (quan trọng)

Trong thư mục `ClockSeconds/`, **mọi file trừ `build/` và `.gradle/` đều phải giữ lại** (phần mã nguồn chỉ khoảng 140 KB):

| Loại | Tệp | Ghi chú |
|------|------|------|
| Script build | `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties` | Phiên bản, phụ thuộc, tham số biên dịch |
| Gradle Wrapper | `gradlew`, `gradlew.bat`, `gradle/wrapper/*` | Cố định Gradle 8.14.5 |
| Mã nguồn | `app/src/main/java/**` (3 tệp `.java`) | Thân logic của module |
| Tài nguyên | `app/src/main/res/**` | Chuỗi (mặc định tiếng Trung + 21 ngôn ngữ values-xx), chủ đề |
| Siêu dữ liệu Xposed | `app/src/main/resources/META-INF/xposed/*` | Lớp vào, phạm vi, phiên bản API |
| Manifest | `app/src/main/AndroidManifest.xml` | Quyền, Activity |
| Tài liệu | `README*.md` | Tài liệu 21 ngôn ngữ |
| CI | `.github/workflows/release.yml` | Đẩy tag là ra bản ký |

## Xử lý sự cố

<details>
<summary>Mở các bước xử lý sự cố</summary>

Xem logcat:

- **Bản build Release** đã dùng cổng `BuildConfig.DEBUG` để loại bỏ toàn bộ log chẩn đoán (nhỏ hơn, sạch hơn); logcat không in ra log của module này.
- Khi cần xử lý sự cố, hãy tạm cài **bản build debug** (`assembleDebug`, ký bằng cùng khóa production nên cài đè trực tiếp lên bản release), rồi lọc theo TAG:

  ```bash
  adb logcat -s ColorOSClockSeconds
  ```

- Nếu hoàn toàn không hiện giây: trước hết xác nhận LSPosed đã tick phạm vi SystemUI và đã khởi động lại; sau đó kiểm tra công tắc có bị lưu ở trạng thái tắt không ——
  có thể đọc / ghi cưỡng chế qua adb:

  ```bash
  adb shell settings get secure coloros_clock_seconds_enabled   # 1=bật 0=tắt
  adb shell settings put  secure coloros_clock_seconds_enabled 1
  ```

- Nếu không có tác dụng trên máy không phải ColorOS: cài bản debug xem log ở dòng `candidate:` / `CONFIG CLOCK:`,
  xác nhận tên tài nguyên / tên lớp của thời gian thanh trạng thái trên máy đó có nằm trong quy tắc nhận diện không; nếu không thì chỉ cần bổ sung một dòng quy tắc.

</details>

## Phản hồi

Có câu hỏi, góp ý hoặc chia sẻ? Hãy để lại lời nhắn tại [GitHub Issues](https://github.com/kelebao/ClockSeconds/issues), tôi sẽ phản hồi sớm nhất có thể.

## License

[MIT](LICENSE) © kelebao
