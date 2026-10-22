<div align="center">

[简体中文](../README.md) · [English](README_EN.md) · [繁體中文](README_zh-TW.md) · [日本語](README_ja.md) · [한국어](README_ko.md) · [Türkçe](README_tr.md) · [Русский](README_ru.md) · [Tiếng Việt](README_vi.md) · [ไทย](README_th.md) · [Deutsch](README_de.md) · [Español](README_es.md) · [Français](README_fr.md) · [Українська](README_uk.md) · [Polski](README_pl.md) · [Português (Brasil)](README_pt-BR.md) · **فارسی** · [العربية](README_ar.md) · [Bahasa Indonesia](README_id.md) · [Italiano](README_it.md) · [Ελληνικά](README_el.md) · [ქართული](README_ka.md)

</div>

<div align="center">

# نمایش ثانیه زمان（Show Seconds in Time）

**ثانیه**ها را روی زمان نوار وضعیت نمایش دهید، با یک ضربه تبدیل کنید و **رقم دهگان زمان را همیشه قرمز** نگه دارید.<br>بدون هیچ کلاس خصوصی سازنده. روی دستگاه واقعی با ColorOS 15 تأیید شده؛ همچنین شیائومی، وان‌پلاس، آنر، ویوو، سامسونگ و سایر سازندگان بزرگ را پوشش می‌دهد.

[![version](https://img.shields.io/badge/version-1.0.7-blue.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/releases/latest)
![android](https://img.shields.io/badge/Android-10%2B-brightgreen.svg?style=flat-square)
[![lsposed](https://img.shields.io/badge/LSPosed-module-orange.svg?style=flat-square)](https://github.com/Xposed-Modules-Repo/io.github.kelebao.colorosclockseconds/releases)
![coloros](https://img.shields.io/badge/ColorOS%2015-verified-success.svg?style=flat-square)
[![stars](https://img.shields.io/github/stars/kelebao/ClockSeconds.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/stargazers)
[![license](https://img.shields.io/badge/license-MIT-lightgrey.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/blob/main/LICENSE)

</div>

---

## قابلیت‌ها

| قابلیت | توضیح |
|------|------|
| نمایش ثانیه | زمان نوار وضعیت ثانیه‌ها را نشان می‌دهد (`HH:mm:ss` / `h:mm:ss a`، بر اساس تنظیم 12/24 ساعته سیستم) |
| ضربه برای تبدیل | **ضربه زدن** روی زمان نوار وضعیت نمایش ثانیه را روشن/خاموش می‌کند؛ در صفحه تنظیمات می‌توان آن را به **ضربه دوگانه** تغییر داد |
| رنگ رقم زمان | رنگ **رقم دهگان زمان** قابل **سفارشیسازی** (پالت 8 رنگ) یا **خاموش کردن** برای بازگشت به رنگ سیستم است؛ زمان‌های یک‌رقمی (مثل `9:45`) رقم دهگان ندارند و دست‌نخورده می‌مانند. روی همه سازندگان مؤثر است |
| پیش نمایش با فشار طولانی | **فشار طولانی** روی زمان نوار وضعیت، **پنجره شناور مات نیمه شفاف** را با **زمان شبکه** به صورت زنده باز میکند؛ قابل جابهجایی، تغییر اندازه با ◣ (پایین چپ)، لمس زمان دکمههای **بستن** (✕ بالا راست) / **تغییر اندازه** را نشان میدهد؛ اندازه متن با پنجره تطبیق میدهد |
| زمان شبکه | صفحه تنظیمات ساعت را با **زمان استاندارد شبکه** به صورت زنده مقایسه میکند؛ در صورت انحراف زیاد **همگامسازی** را بزنید تا تنظیم خودکار ساعت سیستم روشن شود |
| بدون adb | تنظیمات در `Settings.Secure` ذخیره میشود؛ بدون مجوز `WRITE_SECURE_SETTINGS` ماژول بهطور خودکار بهbroadcast که SystemUI پردازش میکند سوییچ میکند — **نیاز به adb نیست** |
| زبان‌های رابط | رابط کاربری برنامه به **21 زبان** ترجمه شده (که به‌طور خودکار از زبان سیستم پیروی می‌کند): 简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, ไทย, Deutsch, Español, Français, Українська, Polski, Português (Brasil), فارسی, العربية, Bahasa Indonesia, Italiano, Ελληνικά, ქართული |
| ماندگاری | وضعیت در `Settings.Secure` ذخیره می‌شود، بین فرآیندها مشترک است و پس از راه‌اندازی مجدد SystemUI باقی می‌ماند |
| سازگاری | روی ColorOS 15 / 16 / 17 (OPPO / وان‌پلاس / ریلمی) تأیید شده؛ روی سایر سازندگان روت‌شده مثل شیائومی / آنر / ردمی / سامسونگ هم بی‌دردسر کار می‌کند. بدون کلاس خصوصی سازنده، بنابراین حالت ایمن LSPosed فعال نمی‌شود. حداقل اندروید 10 |

## نصب

1. newest APK را از صفحه [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest) دانلود کنید
2. ماژول را در **LSPosed** فعال کنید، دامنه: **رابط سیستم (`com.android.systemui`)**
3. فرآیند SystemUI یا دستگاه را دوباره راه‌اندازی کنید
4. صفحه تنظیمات را از **مدیر LSPosed** باز کنید (آیکنی روی صفحه اصلی ساخته نمی‌شود)

> به روت (LSPosed) نیاز دارد. برای نوشتن در `Settings.Secure` باید مجوز `WRITE_SECURE_SETTINGS` داده شود:
> ```bash
> adb shell pm grant io.github.kelebao.colorosclockseconds android.permission.WRITE_SECURE_SETTINGS
> ```
> بدون آن هم می‌توانید با ضربه زدن روی زمان نوار وضعیت تبدیل کنید (آن کار داخل فرآیند SystemUI اجرا می‌شود و از قبل مجوز داشته است).

## روش استفاده

- **ضربه زدن روی زمان نوار وضعیت**: نمایش ثانیه را روشن/خاموش می‌کند (حرکت پیش‌فرض).
- تبدیل با آزمون اصابت لمسی سراسری پیاده‌سازی شده، پس **هرگز با «کشیدن پنل اعلان» تضاد پیدا نمی‌کند**—— ضربه خالص تبدیل می‌کند و کشیدن همچنان پنل را پایین می‌آورد. اگر ترجیح می‌دهید، می‌توانید حرکت را در صفحه تنظیمات به **ضربه دوگانه** تغییر دهید.
- صفحه تنظیمات وضعیت فعلی، حرکت انتخابی و دکمه بازآوری را نشان می‌دهد.

## پیش‌نمایش

<table><tr>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-off.jpg" alt="ثانیه خاموش: `15:06` — رقم دهگان زمان `1` قرمز می‌ماند" width="100%">
<br>
ثانیه خاموش: `15:06` — رقم دهگان زمان `1` قرمز می‌ماند
</td>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-on.jpg" alt="ثانیه روشن: `15:06:03` — دقیقه و ثانیه رنگ سیستم را حفظ می‌کنند" width="100%">
<br>
ثانیه روشن: `15:06:03` — دقیقه و ثانیه رنگ سیستم را حفظ می‌کنند
</td>
</tr></table>

## چطور کار می‌کند

ماژول فقط **SystemUI** را هوک می‌کند و از رویکرد آزموده‌شده legacy Xposed استفاده می‌کند،
و **هیچ‌گاه به هیچ کلاس خصوصی سازنده دست نمی‌زند**، بنابراین وضعیت داخلی سیستم را خراب نمی‌کند و حالت ایمن LSPosed فعال نمی‌شود:

- **ثانیه‌ها**: هوک پایدار `TextView.setText` (after) متن خامی را که سیستم روی زمان نوار وضعیت می‌گذارد حافظه نهان می‌کند،
  سپس `TextView.onDraw` (before) هوک می‌شود تا نمایش با `متن خام:ss` بازنویسی شود، بعد از اینکه سیستم متن را تنظیم کرد،
  با یک `Handler` یک‌ثانیه‌ای. ثانیه‌های افزوده رنگ متن خود زمان را به ارث می‌برند (اندازه‌گیری شده `#ccffffff`).
  همچنین «رقم دهگان زمان» قرمز می‌شود (یک `ForegroundColorSpan` قرمز روی همان یک نویسه)، بی‌توجه به اینکه نمایش ثانیه روشن باشد یا خاموش؛
  دقیقه و ثانیه هرگز قرمز نمی‌شوند؛ زمان‌های یک‌رقمی رقم دهگان ندارند و دست‌نخورده می‌مانند.
- **تبدیل**: پنل اعلان ColorOS لمس‌های محدوده نوار وضعیت را رهگیری می‌کند، بنابراین `OnTouchListener` خود زمان هرگز رویداد دریافت نمی‌کند.
  ماژول برای همین `View.dispatchTouchEvent` سراسری را هوک می‌کند و فقط وقتی سوییچ `Settings.Secure` را برمی‌گرداند که نقطه فشار داخل
  محدوده صفحه زمان شناسایی‌شده بیفتد و تقریباً هیچ جابه‌جایی نداشته باشد (ضربه) یا ضربه دوم در عرض 350 میلی‌ثانیه (ضربه دوگانه). ماژول **هیچ رویداد لمسی را
  مصرف نمی‌کند**، بنابراین کشیدن پنل اعلان مثل همیشه کار می‌کند.

## توضیح صرفه‌جویی انرژی

- **ثانیه روشن**: ماژول به یک تایمر یک‌ثانیه‌ای برای راندن بازآوری نیاز دارد (هزینه ذاتی نمایش ثانیه، حدود 60 بار در دقیقه).
- **ثانیه خاموش**: ماژول **بی‌درنگ آن تایمر را متوقف می‌کند** (`stopTicker`)—— هیچ بیدارباش ثانیه‌ای و هیچ خواندن بین‌فرآیندی وجود ندارد.
  به ریتم اصلی سیستم «یک بار در دقیقه بازآوری کردن» برمی‌گردد، پس خاموش کردن نمایش ثانیه واقعاً کم‌مصرف‌تر است.
- وضعیت در **حافظه نهان درون‌فرآیندی** (`sSecondsEnabled`) نگه داشته می‌شود، پس تایمر هر ثانیه `Settings.Secure` را نمی‌خواند (IPC هر ثانیه اجتناب می‌شود).
- هیچ قالب‌بندی‌کننده زمانی ساخته نمی‌شود: ماژول متن خام سیستم را بازاستفاده می‌کند و فقط `:ss` را می‌افزاید، پس سربار قالب‌بندی صفر است.
- تایمر تنها **ارجاع ضعیف** به ویو را نگه می‌دارد و تا زمانی که ویو `isAttachedToWindow()` باشد ادامه می‌دهد؛ با نابودی ویو خودکار پایان می‌یابد.

## بیلد

به **JDK 17+** (۱۷ یا ۲۱ توصیه می‌شود) و **Android SDK Platform 35** (Build-Tools 35 خودکار توسط AGP نصب می‌شود) نیاز دارد.

```bash
export JAVA_HOME=/path/to/jdk-17-or-21
./gradlew assembleDebug
```

## پشتیبان‌گیری و توسعه بعدی (مهم)

در پوشه `ClockSeconds/`، **هر فایلی جز `build/` و `.gradle/` باید حفظ شود** (بخش کد فقط حدود ۱۴۰ کیلوبایت است):

| دسته | فایل | توضیح |
|------|------|------|
| اسکریپت‌های بیلد | `settings.gradle.kts`، `build.gradle.kts`، `app/build.gradle.kts`، `gradle.properties` | نسخه، وابستگی‌ها، پارامترهای کامپایلر |
| Gradle Wrapper | `gradlew`، `gradlew.bat`، `gradle/wrapper/*` | Gradle 8.14.5 قفل می‌شود |
| کد منبع | `app/src/main/java/**` (۳ فایل `.java`) | بدنه منطق ماژول |
| منابع | `app/src/main/res/**` | رشته‌ها (چینی پیش‌فرض + ۲۱ زبان values-xx)، پوسته |
| فراداده Xposed | `app/src/main/resources/META-INF/xposed/*` | کلاس ورودی، دامنه، نسخه API |
| مانیفست | `app/src/main/AndroidManifest.xml` | مجوزها، Activity |
| مستندات | `README*.md` | مستندات به ۲۱ زبان |
| CI | `.github/workflows/release.yml` | تگ، بیلد امضاشده می‌سازد |

## عیب‌یابی

<details>
<summary>مراحل عیب‌یابی را باز کنید</summary>

logcat را ببینید:

- **بیلدهای Release** همه گزارش‌های تشخیصی را با دروازه `BuildConfig.DEBUG` حذف می‌کنند (کوچک‌تر و تمیزتر)؛ چیزی از این ماژول در logcat نیست.
- برای بررسی موقتاً **بیلد debug** (`assembleDebug`، با همان کلید تولید امضا شده، پس روی Release نصب می‌شود) را نصب و با TAG فیلتر کنید:

  ```bash
  adb logcat -s ColorOSClockSeconds
  ```

- اگر ثانیه‌ها هیچ‌وقت نشان داده نشدند: اول بررسی کنید LSPosed دامنه SystemUI را تیک زده و راه‌اندازی مجدد شده؛ بعد بررسی کنید سوییچ به‌صورت خاموش ذخیره نشده باشد——
  با adb خوانده / اجباراً نوشته می‌شود:

  ```bash
  adb shell settings get secure coloros_clock_seconds_enabled   # 1=روشن 0=خاموش
  adb shell settings put  secure coloros_clock_seconds_enabled 1
  ```

- اگر روی دستگاه غیر ColorOS کار نکرد: بیلد debug را نصب کنید و خطوط `candidate:` / `CONFIG CLOCK:` در گزارش را ببینید
  تا بفهمید نام منبع / نام کلاس زمان نوار وضعیت دستگاه شما در قواعد شناسایی هست یا نه؛ اگر نیست، افزودن یک خط قاعده کافی است.

</details>

## بازخورد

سؤال، پیشنهاد یا نظری دارید؟ در [GitHub Issues](https://github.com/kelebao/ClockSeconds/issues) پیام بگذارید؛ در اسرع وقت پاسخ می‌دهم.

## License

[MIT](LICENSE) © kelebao
