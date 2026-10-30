<div align="center">

[简体中文](../README.md) · [English](README_EN.md) · [繁體中文](README_zh-TW.md) · [日本語](README_ja.md) · [한국어](README_ko.md) · [Türkçe](README_tr.md) · [Русский](README_ru.md) · [Tiếng Việt](README_vi.md) · [ไทย](README_th.md) · [Deutsch](README_de.md) · [Español](README_es.md) · [Français](README_fr.md) · [Українська](README_uk.md) · [Polski](README_pl.md) · [Português (Brasil)](README_pt-BR.md) · [فارسی](README_fa.md) · **العربية** · [Bahasa Indonesia](README_id.md) · [Italiano](README_it.md) · [Ελληνικά](README_el.md) · [ქართული](README_ka.md)

</div>

<div align="center">

# إظهار الثواني في شريط الحالة (Show Seconds in Time)

يعرض **الثواني** في ساعة شريط الحالة، ويُبدَّل باللمس. يمكنك **تلوين كل رقم بلون** أو تطبيق **تدرّج** على النص كله.<br>**الضغط المطوّل** يفتح نافذة شبه شفافة تعرض **وقت الشبكة** (سحب، تحجيم، تدرّج)، و**المهام التلقائية** تُشغّل الثواني في الساعة المطلوبة.<br>المسار الأساسي يستخدم واجهات AOSP العامة فقط (أصناف OPPO / OnePlus / realme الخاصة اختيارية وتُتجاهل عند غيابها). مُختبَر على ColorOS 15 / 16.

[![version](https://img.shields.io/badge/version-1.2.0-blue.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/releases/latest)
![android](https://img.shields.io/badge/Android-10%2B-brightgreen.svg?style=flat-square)
[![lsposed](https://img.shields.io/badge/LSPosed-module-orange.svg?style=flat-square)](https://github.com/Xposed-Modules-Repo/io.github.kelebao.colorosclockseconds/releases)
![coloros](https://img.shields.io/badge/ColorOS%2015%2F16-verified-success.svg?style=flat-square)
[![stars](https://img.shields.io/github/stars/kelebao/ClockSeconds.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/stargazers)
[![license](https://img.shields.io/badge/license-MIT-lightgrey.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/blob/main/LICENSE)

</div>

---

## الميزات

| الميزة | التفصيل |
|------|------|
| الثواني في شريط الحالة | `HH:mm:ss` / `h:mm:ss a` مع احترام صيغة 12/24 ساعة والمنطقة الزمنية. تُضاف **مسافة بعرض صفر** في النهاية كي لا تحذف السمات الألوان، وعند فقدان اللون يُعاد الرسم تلقائيًا (توقف 5 دقائق). |
| التبديل باللمس | **لمس** الوقت يبدّل الثواني (يمكن اختيار **لمس مزدوج**)، و**الضغط المطوّل** مخصّص لنافذة المعاينة. داخل مهمة تظهر رسالة أن المهمة هي المتحكمة. |
| لون كل رقم / تدرّج | ستة أرقام **كلٌّ بلونه** (10 ألوان جاهزة + أشرطة HSV + لون النظام) والنقطتان في الوسط بصريًا. أو **تدرّج** من لون البداية إلى النهاية (6 مجموعات، طرفا التدرّج قابلان للتعديل، ويُغمَّق على الخلفية الفاتحة). بطاقة المعاينة **ترسم نفسها** فتطابق شريط الحالة بكسلًا ببكسل. |
| نافذة الضغط المطوّل | نافذة شبه شفافة تعرض **وقت الشبكة**؛ اسحب للتحريك، وغيّر الحجم من الزاوية ◣، ويُحفظ الموضع والحجم. تدعم التدرّج والأرقام متساوية العرض. |
| المهام التلقائية | فترات **كل ساعة** أو **كل يوم**: تشغيل الثواني قبل N دقيقة لمدة M دقيقة ثم الإرجاع. تغيير الوقت أو المنطقة الزمنية يُعاد حسابه فورًا، مع مراجعة كل 60 ثانية. |
| مقارنة مع وقت الشبكة | يعرض الفرق عن **وقت الشبكة**، وزر **مزامنة** يفعّل الضبط التلقائي للنظام. |
| بلا حاجة إلى adb | الكتابة تتم عبر `Settings.Secure`، وبدون `WRITE_SECURE_SETTINGS` يُرسَل broadcast ليكتب SystemUI — **لا تحتاج أي ميزة إلى adb**. |
| بلاطة سريعة | أضف بلاطة «إظهار الثواني» للتبديل دون فتح التطبيق. |
| اسم مورد الساعة | إن لم تُكتشف الساعة اضغط **الكشف التلقائي** أو أدخل اسم المورد (مثل `clock`). |
| 21 لغة للواجهة | تتبع لغة النظام: 简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, ไทย, Deutsch, Español, Français, Українська, Polski, Português (Brasil), فارسی, العربية, Bahasa Indonesia, Italiano, Ελληνικά, ქართული؛ وغيرها بالإنجليزية. |
| التخزين / النطاق | الحالة محفوظة في `Settings.Secure`، مشتركة بين العمليات وتبقى بعد إعادة التشغيل. **مُختبَر على ColorOS 15 / 16 (Android 15 / 16)**؛ النواة تستخدم واجهات عامة فقط لذا يُفترض عمل الشركات الأخرى، لكن لم يُختبَر سوى OPPO / OnePlus. الحد الأدنى Android 10. |

## أنماط التلوين

طريقتان للتلوين تُبدَّلان في بطاقة **ألوان الأرقام**: **لكل رقم** أو **تدرّج** على النص كله.

<table><tr>
<td align="center" width="50%">
<img src="screenshots/colors-digit.png" alt="لكل رقم: كل رقم بلون، والنقطتان في الوسط بصريًا" width="100%">
<br>
لكل رقم: كل رقم بلون، والنقطتان في الوسط بصريًا
</td>
<td align="center" width="50%">
<img src="screenshots/colors-gradient.png" alt="تدرّج: من لون البداية إلى النهاية، 6 مجموعات أو طرفان مخصّصان" width="100%">
<br>
تدرّج: من لون البداية إلى النهاية، 6 مجموعات أو طرفان مخصّصان
</td>
</tr></table>

## التثبيت

1. نزّل ملف APK من صفحة [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest)
2. فعّل الوحدة في **LSPosed** وحدّد النطاق **واجهة النظام** (`com.android.systemui`)
3. أعد تشغيل عملية SystemUI أو الجهاز
4. تُفتح شاشة الإعدادات من **مدير LSPosed** (لا يُنشأ أيقونة في المشغّل)

> يلزم root (LSPosed). **لا حاجة عمومًا إلى صلاحيات adb**: الكتابة تتم عبر `Settings.Secure`، وبدون `WRITE_SECURE_SETTINGS` عبر broadcast تكتبه SystemUI. لكتابة شاشة الإعدادات **مباشرة**:
> ```bash
> adb shell pm grant io.github.kelebao.colorosclockseconds android.permission.WRITE_SECURE_SETTINGS
> ```
> بدون منح الصلاحية يعمل كل شيء: اللمس والمهام والنافذة تعمل داخل SystemUI.

## الاستخدام

- **المس الوقت**: تشغيل/إيقاف الثواني (افتراضيًا)، ويمكن ضبط **لمس مزدوج** في الإعدادات.
- **اضغط مطوّلًا**: نافذة شبه شفافة تعرض **وقت الشبكة**، اسحب للتحريك، ولمس الوقت يُظهر **إغلاق** (أعلى اليمين) و**تحجيم** (أسفل اليسار).
- بطاقة **ألوان الأرقام** فيها صفحتان، والعلامة نفسها هي المفتاح.
- بطاقة **المهام التلقائية** تخطط فترات مثل «الثواني قبل كل ساعة بدقيقتين».
- التبديل يعتمد على فحص لمس شامل و**لا يتعارض مع سحب لوحة الإشعارات**.

## معاينة

<table><tr>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-off.jpg" alt="الثواني متوقفة: `15:06`، وعشرات الساعة تحفظ اللون" width="100%">
<br>
الثواني متوقفة: `15:06`، وعشرات الساعة تحفظ اللون
</td>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-on.jpg" alt="الثواني مفعّلة: `15:06:03`، والدقائق والثواني بلون النظام" width="100%">
<br>
الثواني مفعّلة: `15:06:03`، والدقائق والثواني بلون النظام
</td>
</tr></table>

## كيف يعمل

الوحدة تعترض **SystemUI** فقط وتستخدم واجهة Xposed القديمة المستقرة.
**النواة تعتمد على واجهات AOSP العامة فقط، وأصناف الشركات اختيارية وتُتجاهل عند غيابها**، لذلك لا تُعطِّل النظام ولا تُفعّل الوضع الآمن في LSPosed:

- **الثواني**: `TextView.setText` (after) يخزّن نص النظام، ثم `TextView.onDraw` (before) يكتب `الأصل:ss`، ويُحدَّث كل ثانية عبر `Handler`.
- **لون كل رقم**: `ForegroundColorSpan` لكل حرف (مع إزالة أي span غريب)؛ وتُرفع النقطتان 0.11em لأن التخطيط متعدد المقاطع يضع الحرف أخفض.
- **التدرّج**: `CharacterStyle` ينادي `setShader` في `updateDrawState`؛ والمعاينة **ترسم نفسها** (`GradientTimeView`) لأن ColorOS 15 يُكمّم التظليل حسب مقاطع النص.
- **التبديل**: لوحة الإشعارات تعترض اللمس على الشريط، لذا تعترض الوحدة `View.dispatchTouchEvent` عالميًا وتبدّل فقط إذا وقع اللمس داخل الساعة بلا سحب؛ والحدث **لا يُستهلك**.
- **المهام**: فحص «هل الوقت المحلي داخل الفترة»، ومؤقتات دقيقة على الحدود ومراجعة كل 60 ثانية.

## استهلاك الطاقة

- **الثواني مفعّلة**: مؤقت كل ثانية (كلفة لا مفر منها). قياس SystemUI لكل نواة: 7.0% مطفأ، 7.25% مفعّل (60 ثانية).
- **الثواني متوقفة**: المؤقت **يتوقف فورًا** (`stopTicker`) ولا يوجد إيقاظ كل ثانية.
- **حماية اللون** تتباطأ 2s → 4s → … → 30s وتنتهي بعد 5 دقائق دون فقدان اللون.
- الحالة **مخزّنة في العملية**؛ والمهام تستخدم `Handler.postDelayed`: **بلا WakeLock ولا AlarmManager**، فلا إيقاظ من النوم العميق.

## البناء

تحتاج **JDK 17+** (17 أو 21) و**Android SDK Platform 35** (وسينزّل AGP أدوات البناء 35).

```bash
export JAVA_HOME=/path/to/jdk-17-or-21
./gradlew assembleDebug
```

## النسخ الاحتياطي ومتابعة التطوير (مهم)

داخل مجلد `ClockSeconds/` احفظ **كل شيء عدا `build/` و`.gradle/`** (الشيفرة نحو 300 كيلوبايت):

| الفئة | الملفات | ملاحظة |
|------|------|------|
| ملفات البناء | `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties`, `keystore.properties` | الإصدارات والاعتماديات والمعاملات والتوقيع |
| Gradle Wrapper | `gradlew`, `gradlew.bat`, `gradle/wrapper/*` | يثبّت Gradle 8.14.5 |
| الشيفرة | `app/src/main/java/**` (20 ملف `.java`) | منطق الوحدة والإعدادات وأصناف الرسم |
| الموارد | `app/src/main/res/**` | النصوص (22 لغة) والسمات |
| بيانات Xposed | `app/src/main/resources/META-INF/xposed/*` | صنف الدخول والنطاق ومستوى API |
| Manifest | `app/src/main/AndroidManifest.xml` | الأذونات والأنشطة |
| الأدوات | `tools/**` | مولّد README وفحص i18n وسكربت الإصدار |
| المستندات | `README*.md`, `docs/**`, `CHANGELOG.md` | الوصف والصور وسجل التغييرات |
| CI | `.github/workflows/*.yml` | بناء موقّع عند الوسم |

## حل المشكلات

<details>
<summary>عرض الخطوات</summary>

راجع logcat:

- **نسخ Release** تخفي كل سجلات التشخيص خلف `BuildConfig.DEBUG`.
- للتشخيص ثبّت **نسخة debug** (`assembleDebug` بالمفتاح نفسه) ثم صفِّ:

  ```bash
  adb logcat -s ColorOSClockSeconds
  ```

- لا تظهر الثواني: تحقق من نطاق «واجهة النظام» في LSPosed وأعد التشغيل، ثم اقرأ أو اكتب المفتاح:

  ```bash
  adb shell settings get secure coloros_clock_seconds_enabled   # 1=on 0=off
  adb shell settings put  secure coloros_clock_seconds_enabled 1
  ```

- الألوان أو الثواني تعود من نفسها: النظام أو السمة يعيد بناء النص والوحدة تعيد الرسم؛ وإن لم تُستعَد فابحث في سجل debug عن `color-guard stopped` / `span-theft`.

</details>

## الملاحظات

الأسئلة والمقترحات عبر [GitHub Issues](https://github.com/kelebao/ClockSeconds/issues).

## License

[MIT](LICENSE) © kelebao
