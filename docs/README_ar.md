<div align="center">

[简体中文](../README.md) · [English](README_EN.md) · [繁體中文](README_zh-TW.md) · [日本語](README_ja.md) · [한국어](README_ko.md) · [Türkçe](README_tr.md) · [Русский](README_ru.md) · [Tiếng Việt](README_vi.md) · [ไทย](README_th.md) · [Deutsch](README_de.md) · [Español](README_es.md) · [Français](README_fr.md) · [Українська](README_uk.md) · [Polski](README_pl.md) · [Português (Brasil)](README_pt-BR.md) · [فارسی](README_fa.md) · **العربية** · [Bahasa Indonesia](README_id.md) · [Italiano](README_it.md) · [Ελληνικά](README_el.md) · [ქართული](README_ka.md)

</div>

<div align="center">

# عرض الثواني في الساعة (Clock Seconds)

اعرض **الثواني** على ساعة شريط الحالة، بدّل بلمسة واحدة، وأبقِ **خانة العشرات من الساعة حمراء** دائمًا.<br>بدون أي أصناف خاصة بالمصنّع. تم التحقق على جهاز حقيقي بنظام ColorOS 15؛ ويغطي كذلك شاومي وون‌بلس وهونور وvivo وغيرهم من المصنّعين الكبار.

[![version](https://img.shields.io/badge/version-1.0.4-blue.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/releases/latest)
![android](https://img.shields.io/badge/Android-10%2B-brightgreen.svg?style=flat-square)
[![lsposed](https://img.shields.io/badge/LSPosed-module-orange.svg?style=flat-square)](https://github.com/Xposed-Modules-Repo/io.github.kelebao.colorosclockseconds/releases)
![coloros](https://img.shields.io/badge/ColorOS%2015-verified-success.svg?style=flat-square)
[![stars](https://img.shields.io/github/stars/kelebao/ClockSeconds.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/stargazers)
[![license](https://img.shields.io/badge/license-MIT-lightgrey.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/blob/main/LICENSE)

</div>

---

## الميزات

| الميزة | الوصف |
|------|------|
| عرض الثواني | تعرض ساعة شريط الحالة الثواني (`HH:mm:ss` / `h:mm:ss a`، حسب إعداد 12/24 ساعة في النظام) |
| اللمس للتبديل | **لمس** ساعة شريط الحالة يبدّل عرض الثواني؛ ويمكن أيضًا اختيار **الضغط المطوّل** في صفحة الإعدادات |
| خانة العشرات حمراء | تُصبغ **خانة العشرات من الساعة** بالأحمر (مثل `1` في `13` و`0` في `05`)؛ وتبقى الدقائق والثواني بلون النظام. الساعات ذات المنزلة الواحدة (مثل `9:45`) لا تحتوي خانة عشرات وتبقى كما هي. فعّال على جميع المصنّعين |
| لغات الواجهة | واجهة التطبيق مترجمة إلى **21 لغة** (تتبع لغة النظام تلقائيًا): 简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, ไทย, Deutsch, Español, Français, Українська, Polski, Português (Brasil), فارسی, العربية, Bahasa Indonesia, Italiano, Ελληνικά, ქართული |
| الاستمرارية | تُحفظ الحالة في `Settings.Secure`، وتشارَك بين العمليات، وتبقى بعد إعادة تشغيل SystemUI |
| التوافق | تم التحقق على ColorOS 15 / 16 / 17 (OPPO / ون‌بلس / ريلمي)؛ ويعمل أيضًا مباشرة على مصنّعين آخرين بصلاحية الروت مثل شاومي / هونور / ريدمي. بدون أصناف خاصة بالمصنّع، لذا لا يُفعّل الوضع الآمن لـ LSPosed. الحد الأدنى أندرويد 10 |

## التثبيت

1. نزّل أحدث APK من صفحة [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest)
2. فعّل الوحدة في **LSPosed**، النطاق: **واجهة النظام (`com.android.systemui`)**
3. أعد تشغيل عملية SystemUI أو أعد تشغيل الجهاز
4. افتح صفحة الإعدادات من **مدير LSPosed** (لا يُنشأ أيقونة على الشاشة الرئيسية)

> يتطلب روت (LSPosed). لمنح الإذن `WRITE_SECURE_SETTINGS` للكتابة في `Settings.Secure`:
> ```bash
> adb shell pm grant io.github.kelebao.colorosclockseconds android.permission.WRITE_SECURE_SETTINGS
> ```
> بدونه لا يزال بإمكانك التبديل بلمس ساعة شريط الحالة (هذا الإجراء يعمل داخل عملية SystemUI والتي تمتلك الإذن أصلًا).

## الاستخدام

- **لمس ساعة شريط الحالة**: يبدّل عرض الثواني (الإيماءة الافتراضية).
- يُنفّذ التبديل عبر اختبار لمس شامل، لذا **لا يتعارض أبدًا مع «سحب لوحة الإشعارات للأسفل»**—— اللمسة النقية تبدّل، والسحب ما زال يسحب اللوحة للأسفل. إن فضّلت ذلك، يمكنك تغيير الإيماءة إلى **الضغط المطوّل** في صفحة الإعدادات.
- تعرض صفحة الإعدادات الحالة الحالية والإيماءة المختارة وزر تحديث.

## معاينة

<table><tr>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-off.jpg" alt="الثواني مُعطّلة: `15:06` — تبقى خانة العشرات من الساعة `1` حمراء" width="100%">
<br>
الثواني مُعطّلة: `15:06` — تبقى خانة العشرات من الساعة `1` حمراء
</td>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-on.jpg" alt="الثواني مُفعّلة: `15:06:03` — الدقائق والثواني تحافظ على لون النظام" width="100%">
<br>
الثواني مُفعّلة: `15:06:03` — الدقائق والثواني تحافظ على لون النظام
</td>
</tr></table>

## كيف يعمل

تخصّ الوحدة **SystemUI** فقط، باستخدام منهج legacy Xposed المُجرَّب،
و**لا تمسّ أي صنف خاص بمصنّع إطلاقًا**، فلا تُفسد الحالة الداخلية للنظام ولا تُفعّل الوضع الآمن لـ LSPosed:

- **الثواني**: خطّاف `TextView.setText` (after) المستقر يخزّن مؤقتًا النص الخام الذي يضعه النظام على ساعة شريط الحالة،
  ثم خطّاف `TextView.onDraw` (before) يعيد كتابة العرض ليصبح `النص الخام:ss` بعد أن يضبط النظام النص،
  مدفوعًا بـ `Handler` كل ثانية. الثواني المضافة ترث لون نص الساعة نفسه (المقاس `#ccffffff`).
  بالإضافة إلى ذلك تُصبغ «خانة العشرات من الساعة» بالأحمر (لون `ForegroundColorSpan` أحمر على ذلك الحرف الواحد)، سواء أكان عرض الثواني مفعّلًا أم لا؛
  الدقائق والثواني لا تُصبغ أبدًا؛ الساعات ذات المنزلة الواحدة لا تحتوي خانة عشرات وتبقى كما هي.
- **التبديل**: تعترض لوحة إشعارات ColorOS اللمسات في منطقة شريط الحالة، لذا لا يستقبل `OnTouchListener` الخاص بالساعة أي أحداث.
  لذلك تُخطّف الوحدة `View.dispatchTouchEvent` العامة ولا تقلب مفتاح `Settings.Secure` إلا حين تقع نقطة الضغط داخل
  حدود الساعة المتعرَّف عليها على الشاشة ودون أي حركة تقريبًا (لمسة) أو بضغطة مستمرة 500 مللي ثانية (ضغط مطوّل). الوحدة **لا تستهلك**
  أي حدث لمس، لذا يعمل سحب لوحة الإشعارات للأسفل كالمعتاد.

## ملاحظة عن توفير الطاقة

- **الثواني مفعّلة**: تحتاج الوحدة إلى مؤقّت بثانية واحدة لدفع التحديثات (هذا ثمن عرض الثواني المتأصل، حوالي 60 إعادة رسم للساعة في الدقيقة).
- **الثواني معطّلة**: **تُوقف الوحدة ذلك المؤقّت فورًا** (`stopTicker`)—— لا استيقاظ كل ثانية ولا قراءات بين العمليات.
  تعود إلى إيقاع النظام الأصلي «تحديث مرة كل دقيقة»، لذا فإن تعطيل عرض الثواني يوفر طاقة فعليًا.
- تُخزّن الحالة في **ذاكرة مؤقتة داخل العملية** (`sSecondsEnabled`)، لذا لا يقرأ المؤقّت `Settings.Secure` كل ثانية (تجنّب IPC كل ثانية).
- لا يُبنى أي منسّق وقت: تعيد الوحدة استخدام النص الخام للنظام وتكتفي بإضافة `:ss`، لذا لا توجد أعباء تنسيق.
- يحمل المؤقّت **مرجعًا ضعيفًا** فقط للعرض، ولا يستمر إلا ما دام العرض `isAttachedToWindow()`؛ وبعد إتلاف العرض يخرج تلقائيًا.

## البناء

يتطلب **JDK 17+** (يُنصح بـ 17 أو 21) و**Android SDK Platform 35** (يثبّت AGP أدوات البناء 35 تلقائيًا).

```bash
export JAVA_HOME=/path/to/jdk-17-or-21
./gradlew assembleDebug
```

## النسخ الاحتياطي والتطوير اللاحق (مهم)

في مجلد `ClockSeconds/`، يجب **الاحتفاظ بكل الملفات ما عدا `build/` و`.gradle/`** (جزء الشيفرة لا يزيد عن 140 كيلوبايت تقريبًا):

| الفئة | الملف | الوصف |
|------|------|------|
| سكربتات البناء | `settings.gradle.kts`، `build.gradle.kts`، `app/build.gradle.kts`، `gradle.properties` | الإصدار والتبعيات ومعاملات المُصرِّف |
| Gradle Wrapper | `gradlew`، `gradlew.bat`، `gradle/wrapper/*` | تثبيت Gradle 8.14.5 |
| الشيفرة المصدرية | `app/src/main/java/**` (٣ ملفات `.java`) | جسم منطق الوحدة |
| الموارد | `app/src/main/res/**` | النصوص (الصينية افتراضيًا + ٢١ لغة values-xx)، السمة |
| بيانات Xposed الوصفية | `app/src/main/resources/META-INF/xposed/*` | صنف الدخول، النطاق، إصدار API |
| المانيفست | `app/src/main/AndroidManifest.xml` | الأذونات، Activity |
| الوثائق | `README*.md` | وثائق بـ ٢١ لغة |
| CI | `.github/workflows/release.yml` | الوسم يُنتج بناءً موقّعًا |

## استكشاف الأخطاء

<details>
<summary>وسّع خطوات استكشاف الأخطاء</summary>

افحص سجل النظام:

- **أبنية Release** تُجرّد كل سجلات التشخيص عبر بوابة `BuildConfig.DEBUG` (أصغر وأنظف)؛ لا يظهر شيء من هذه الوحدة في logcat.
- للفحص، ثبّت مؤقتًا **البناء debug** (`assembleDebug`، موقّع بنفس مفتاح الإنتاج فيُثبَّت فوق Release مباشرة) ثم صفِّ بالوسم:

  ```bash
  adb logcat -s ColorOSClockSeconds
  ```

- إن لم تظهر الثواني إطلاقًا: تأكّد أولًا أن نطاق SystemUI مؤشَّر في LSPosed وتمت إعادة التشغيل؛ ثم افحص إن كان المفتاح قد حُفظ كمعطّل——
  يمكن قراءته / كتابته قسرًا عبر adb:

  ```bash
  adb shell settings get secure coloros_clock_seconds_enabled   # 1=مفعّل 0=معطّل
  adb shell settings put  secure coloros_clock_seconds_enabled 1
  ```

- إن لم يعمل على جهاز غير ColorOS: ثبّت بناء debug وانظر إلى سطري `candidate:` / `CONFIG CLOCK:` في السجل
  لتتحقق مما إذا كان اسم المورد / اسم صنف ساعة شريط الحالة في جهازك مشمولًا بقواعد التعرّف؛ وإن لم يكن، فإضافة سطر قاعدة واحد يكفي.

</details>

## License

[MIT](LICENSE) © kelebao
