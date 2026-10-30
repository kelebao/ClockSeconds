<div align="center">

[简体中文](../README.md) · [English](README_EN.md) · [繁體中文](README_zh-TW.md) · [日本語](README_ja.md) · [한국어](README_ko.md) · [Türkçe](README_tr.md) · [Русский](README_ru.md) · [Tiếng Việt](README_vi.md) · [ไทย](README_th.md) · [Deutsch](README_de.md) · [Español](README_es.md) · [Français](README_fr.md) · [Українська](README_uk.md) · [Polski](README_pl.md) · [Português (Brasil)](README_pt-BR.md) · **فارسی** · [العربية](README_ar.md) · [Bahasa Indonesia](README_id.md) · [Italiano](README_it.md) · [Ελληνικά](README_el.md) · [ქართული](README_ka.md)

</div>

<div align="center">

# ثانیه‌ها در نوار وضعیت (Show Seconds in Time)

**ثانیه** را روی ساعت نوار وضعیت نشان می‌دهد و با لمس روشن/خاموش می‌شود. می‌توانید به **هر رقم یک رنگ** داد یا **گرادیان** را روی کل متن گذاشت.<br>**نگه‌داشتن** پنجره نیمه‌شفاف **زمان شبکه** را باز می‌کند (جابه‌جایی، اندازه، گرادیان)؛ **کارهای خودکار** ثانیه را در ساعت مشخص روشن می‌کنند.<br>مسیر اصلی فقط از APIهای عمومی AOSP استفاده می‌کند (کلاس‌های اختصاصی OPPO / OnePlus / realme اختیاری هستند و در صورت نبود رد می‌شوند). روی ColorOS 15 / 16 آزمایش شده است.

[![version](https://img.shields.io/badge/version-1.2.0-blue.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/releases/latest)
![android](https://img.shields.io/badge/Android-10%2B-brightgreen.svg?style=flat-square)
[![lsposed](https://img.shields.io/badge/LSPosed-module-orange.svg?style=flat-square)](https://github.com/Xposed-Modules-Repo/io.github.kelebao.colorosclockseconds/releases)
![coloros](https://img.shields.io/badge/ColorOS%2015%2F16-verified-success.svg?style=flat-square)
[![stars](https://img.shields.io/github/stars/kelebao/ClockSeconds.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/stargazers)
[![license](https://img.shields.io/badge/license-MIT-lightgrey.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/blob/main/LICENSE)

</div>

---

## قابلیت‌ها

| قابلیت | توضیح |
|------|------|
| ثانیه در نوار وضعیت | `HH:mm:ss` / `h:mm:ss a` و قالب ۱۲/۲۴ ساعتی و منطقه زمانی را رعایت می‌کند. در پایان یک **فاصله با عرض صفر** افزوده می‌شود تا پوسته‌ها رنگ‌ها را نزنند؛ در صورت از دست رفتن رنگ، متن خودکار دوباره کشیده می‌شود (توقف ۵ دقیقه‌ای). |
| لمس برای تغییر | **لمس** ساعت ثانیه را روشن/خاموش می‌کند (می‌توان **دو لمس** را انتخاب کرد)؛ **نگه‌داشتن** برای پنجره پیش‌نمایش است. داخل یک کار خودکار پیام «کنترل در اختیار کار» نشان داده می‌شود. |
| رنگ هر رقم / گرادیان | شش رقم **هر یک به یک رنگ** (۱۰ رنگ آماده + لغزنده‌های HSV + رنگ سیستم) و دو نقطه از نظر بصری وسط‌چین. یا **گرادیان** از رنگ آغاز به پایان (۶ مجموعه، دو سر قابل ویرایش، در پس‌زمینه روشن تیره می‌شود). کارت پیش‌نمایش **خودش می‌کشد** و پیکسل‌به‌پیکسل با نوار وضعیت می‌خواند. |
| پنجره با نگه‌داشتن | پنجره نیمه‌شفاف با **زمان شبکه**؛ جابه‌جایی، تغییر اندازه از گوشه ◣، موقعیت و اندازه **ذخیره می‌شوند**؛ گرادیان و ارقام هم‌عرض. |
| کارهای خودکار | بازه‌های **هر ساعت** یا **هر روز**: روشن کردن ثانیه N دقیقه قبل به مدت M دقیقه و بازگشت. تغییر زمان یا منطقه زمانی فوراً بازمحاسبه می‌شود، به‌همراه بررسی هر ۶۰ ثانیه. |
| مقایسه با زمان شبکه | اختلاف با **زمان شبکه** را نشان می‌دهد؛ دکمه **همگام‌سازی** تنظیم خودکار زمان سیستم را روشن می‌کند. |
| بدون نیاز به adb | نوشتن از طریق `Settings.Secure` انجام می‌شود؛ بدون `WRITE_SECURE_SETTINGS` یک broadcast نوشتن را به SystemUI می‌سپارد — **هیچ قابلیتی به adb نیاز ندارد**. |
| کاشی در پنل | کاشی «نمایش ثانیه» را اضافه کنید تا بدون باز کردن برنامه تغییر دهید. |
| نام منبع ساعت | اگر ساعت شناسایی نشد، **تشخیص خودکار** را بزنید یا نام منبع را وارد کنید (مثلاً `clock`). |
| ۲۱ زبان رابط | پیرو زبان سیستم: 简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, ไทย, Deutsch, Español, Français, Українська, Polski, Português (Brasil), فارسی, العربية, Bahasa Indonesia, Italiano, Ελληνικά, ქართული؛ بقیه انگلیسی. |
| ذخیره‌سازی / دامنه | وضعیت در `Settings.Secure` ذخیره می‌شود، بین فرایندها مشترک است و پس از راه‌اندازی مجدد می‌ماند. **روی ColorOS 15 / 16 (Android 15 / 16) آزمایش شده**؛ هسته فقط از APIهای عمومی استفاده می‌کند، پس سازندگان دیگر هم باید کار کنند، اما فقط OPPO / OnePlus آزموده شده‌اند. حداقل Android 10. |

## حالت‌های رنگ

دو روش رنگ‌آمیزی که در کارت **رنگ ارقام** با زبانه عوض می‌شود: **هر رقم** یا **گرادیان** روی کل متن.

<table><tr>
<td align="center" width="50%">
<img src="screenshots/colors-digit.png" alt="هر رقم: هر رقم یک رنگ، دو نقطه از نظر بصری وسط‌چین" width="100%">
<br>
هر رقم: هر رقم یک رنگ، دو نقطه از نظر بصری وسط‌چین
</td>
<td align="center" width="50%">
<img src="screenshots/colors-gradient.png" alt="گرادیان: از رنگ آغاز به پایان، ۶ مجموعه یا دو سر دلخواه" width="100%">
<br>
گرادیان: از رنگ آغاز به پایان، ۶ مجموعه یا دو سر دلخواه
</td>
</tr></table>

## نصب

1. APK را از صفحه [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest) بگیرید
2. ماژول را در **LSPosed** روشن کنید و دامنه **System UI** (`com.android.systemui`) را بزنید
3. فرایند SystemUI یا دستگاه را دوباره راه‌اندازی کنید
4. صفحه تنظیمات از **مدیر LSPosed** باز می‌شود (نشانه‌ای در لانچر ساخته نمی‌شود)

> به root (LSPosed) نیاز است. **معمولاً به دسترسی adb نیازی نیست**: نوشتن از طریق `Settings.Secure` و در نبود `WRITE_SECURE_SETTINGS` با broadcast توسط SystemUI انجام می‌شود. اگر می‌خواهید صفحه تنظیمات **مستقیم** بنویسد:
> ```bash
> adb shell pm grant io.github.kelebao.colorosclockseconds android.permission.WRITE_SECURE_SETTINGS
> ```
> بدون اجازه هم همه‌چیز کار می‌کند: لمس، کارها و پنجره داخل SystemUI اجرا می‌شوند.

## استفاده

- **لمس ساعت**: ثانیه روشن/خاموش (پیش‌فرض)؛ در تنظیمات می‌توان **دو لمس** گذاشت.
- **نگه‌داشتن**: پنجره نیمه‌شفاف با **زمان شبکه**، با کشیدن جابه‌جا می‌شود؛ با لمس ساعت دکمه‌های **بستن** (بالا راست) و **اندازه** (پایین چپ) ظاهر می‌شوند.
- کارت **رنگ ارقام** دو صفحه دارد و خود زبانه نقش کلید را دارد.
- کارت **کارهای خودکار** بازه‌هایی مثل «ثانیه، ۲ دقیقه پیش از هر ساعت» می‌سازد.
- تغییر وضعیت از بررسی سراسری لمس استفاده می‌کند و **با کشیدن پنل** تعارض ندارد.

## پیش‌نمایش

<table><tr>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-off.jpg" alt="ثانیه خاموش: `15:06`، دهگان ساعت رنگ انتخابی را نگه می‌دارد" width="100%">
<br>
ثانیه خاموش: `15:06`، دهگان ساعت رنگ انتخابی را نگه می‌دارد
</td>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-on.jpg" alt="ثانیه روشن: `15:06:03`، دقیقه و ثانیه با رنگ سیستم" width="100%">
<br>
ثانیه روشن: `15:06:03`، دقیقه و ثانیه با رنگ سیستم
</td>
</tr></table>

## نحوه کار

ماژول فقط **SystemUI** را هوک می‌کند و از API قدیمی و پایدار Xposed استفاده می‌کند.
**هسته تنها بر APIهای عمومی AOSP تکیه دارد و کلاس‌های اختصاصی سازنده اختیاری‌اند و در نبودشان رد می‌شوند**، بنابراین سیستم را خراب نمی‌کند و حالت ایمن LSPosed را فعال نمی‌سازد:

- **ثانیه**: `TextView.setText` (after) متن سیستم را ذخیره می‌کند، سپس `TextView.onDraw` (before) `اصل:ss` را می‌نویسد و هر ثانیه با `Handler` تازه می‌شود.
- **رنگ هر رقم**: یک `ForegroundColorSpan` برای هر نویسه (spanهای بیگانه حذف می‌شوند)؛ دو نقطه 0.11em بالا می‌رود چون چیدمان چندبخشی گلیف را پایین‌تر می‌گذارد.
- **گرادیان**: `CharacterStyle` در `updateDrawState` تابع `setShader` را صدا می‌زند؛ پیش‌نمایش **خودش می‌کشد** (`GradientTimeView`) چون ColorOS 15 شیدر را بر اساس بخش متن کوانتیزه می‌کند.
- **تغییر وضعیت**: پنل لمس‌های نوار را می‌گیرد، بنابراین ماژول `View.dispatchTouchEvent` را سراسری هوک می‌کند و تنها وقتی لمس داخل ساعت و بدون کشیدن باشد تغییر می‌دهد؛ رویداد **مصرف نمی‌شود**.
- **کارهای خودکار**: بررسی «آیا زمان محلی در بازه است»، تایمرهای دقیق روی مرزها و بازبینی هر ۶۰ ثانیه.

## مصرف باتری

- **ثانیه روشن**: تایمر هر ثانیه (هزینه ناگزیر). اندازه‌گیری SystemUI روی یک هسته: ۷٫۰٪ خاموش، ۷٫۲۵٪ روشن (۶۰ ثانیه).
- **ثانیه خاموش**: تایمر **فوراً متوقف می‌شود** (`stopTicker`) و بیدارشدن هر ثانیه وجود ندارد.
- **محافظ رنگ** از ۲s به ۴s و … تا ۳۰s عقب می‌رود و پس از ۵ دقیقه بدون از دست رفتن رنگ پایان می‌یابد.
- وضعیت **در فرایند کش می‌شود**؛ کارها از `Handler.postDelayed` استفاده می‌کنند: **بدون WakeLock و AlarmManager**، خواب عمیق بیدار نمی‌شود.

## ساخت

به **JDK 17+** (۱۷ یا ۲۱) و **Android SDK Platform 35** نیاز است (AGP خودش Build-Tools 35 را می‌گیرد).

```bash
export JAVA_HOME=/path/to/jdk-17-or-21
./gradlew assembleDebug
```

## پشتیبان‌گیری و ادامه توسعه (مهم)

در پوشه `ClockSeconds/` **همه‌چیز جز `build/` و `.gradle/`** را نگه دارید (کد حدود ۳۰۰ کیلوبایت):

| دسته | فایل‌ها | یادداشت |
|------|------|------|
| فایل‌های ساخت | `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties`, `keystore.properties` | نسخه‌ها، وابستگی‌ها، پارامترها، امضا |
| Gradle Wrapper | `gradlew`, `gradlew.bat`, `gradle/wrapper/*` | Gradle 8.14.5 را ثابت می‌کند |
| کد | `app/src/main/java/**` (۲۰ فایل `.java`) | منطق ماژول، تنظیمات، کلاس‌های رسم |
| منابع | `app/src/main/res/**` | متن‌ها (۲۲ زبان)، پوسته‌ها |
| فراداده Xposed | `app/src/main/resources/META-INF/xposed/*` | کلاس ورود، دامنه، سطح API |
| Manifest | `app/src/main/AndroidManifest.xml` | مجوزها، اکتیویتی‌ها |
| ابزارها | `tools/**` | سازنده README، بررسی i18n، اسکریپت انتشار |
| مستندات | `README*.md`, `docs/**`, `CHANGELOG.md` | توضیح، تصاویر، تاریخچه تغییرات |
| CI | `.github/workflows/*.yml` | ساخت امضاشده روی تگ |

## عیب‌یابی

<details>
<summary>نمایش مراحل</summary>

logcat را ببینید:

- **نسخه‌های Release** همه لاگ‌های عیب‌یابی را پشت `BuildConfig.DEBUG` پنهان می‌کنند.
- برای اشکال‌زدایی **نسخه debug** را نصب کنید (`assembleDebug` با همان کلید) و فیلتر کنید:

  ```bash
  adb logcat -s ColorOSClockSeconds
  ```

- ثانیه نیست: دامنه «System UI» را در LSPosed بررسی و دوباره راه‌اندازی کنید؛ سپس کلید را بخوانید یا بنویسید:

  ```bash
  adb shell settings get secure coloros_clock_seconds_enabled   # 1=on 0=off
  adb shell settings put  secure coloros_clock_seconds_enabled 1
  ```

- رنگ یا ثانیه خودبه‌خود برمی‌گردد: سیستم یا پوسته متن را بازمی‌سازد و ماژول دوباره می‌کشد؛ اگر برنگشت در لاگ debug به دنبال `color-guard stopped` / `span-theft` بگردید.

</details>

## بازخورد

پرسش و پیشنهاد در [GitHub Issues](https://github.com/kelebao/ClockSeconds/issues).

## License

[MIT](LICENSE) © kelebao
