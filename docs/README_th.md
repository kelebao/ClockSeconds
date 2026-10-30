<div align="center">

[简体中文](../README.md) · [English](README_EN.md) · [繁體中文](README_zh-TW.md) · [日本語](README_ja.md) · [한국어](README_ko.md) · [Türkçe](README_tr.md) · [Русский](README_ru.md) · [Tiếng Việt](README_vi.md) · **ไทย** · [Deutsch](README_de.md) · [Español](README_es.md) · [Français](README_fr.md) · [Українська](README_uk.md) · [Polski](README_pl.md) · [Português (Brasil)](README_pt-BR.md) · [فارسی](README_fa.md) · [العربية](README_ar.md) · [Bahasa Indonesia](README_id.md) · [Italiano](README_it.md) · [Ελληνικά](README_el.md) · [ქართული](README_ka.md)

</div>

<div align="center">

# แสดงวินาทีบนนาฬิกา (Show Seconds in Time)

แสดง **วินาที** บนแถบสถานะ แตะเพื่อเปิด/ปิด เลือกได้ระหว่าง **ระบายสีทีละหลัก** กับ **ไล่ระดับสี** ทั้งข้อความ<br>**กดค้าง** เปิดหน้าต่างกระจกฝ้าแสดง **เวลาจากเครือข่าย** (ลาก/ปรับขนาด/ไล่สีได้) และ **งานอัตโนมัติ** เปิดวินาทีตามเวลา<br>แกนหลักใช้เฉพาะ API สาธารณะของ AOSP (คลาสเฉพาะ OPPO / OnePlus / realme เป็นทางเลือก) ทดสอบจริงบน ColorOS 15 / 16

[![version](https://img.shields.io/badge/version-1.2.0-blue.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/releases/latest)
![android](https://img.shields.io/badge/Android-10%2B-brightgreen.svg?style=flat-square)
[![lsposed](https://img.shields.io/badge/LSPosed-module-orange.svg?style=flat-square)](https://github.com/Xposed-Modules-Repo/io.github.kelebao.colorosclockseconds/releases)
![coloros](https://img.shields.io/badge/ColorOS%2015%2F16-verified-success.svg?style=flat-square)
[![stars](https://img.shields.io/github/stars/kelebao/ClockSeconds.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/stargazers)
[![license](https://img.shields.io/badge/license-MIT-lightgrey.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/blob/main/LICENSE)

</div>

---

## ความสามารถ

| ความสามารถ | รายละเอียด |
|------|------|
| วินาทีบนแถบสถานะ | `HH:mm:ss` / `h:mm:ss a` รองรับ 12/24 ชั่วโมงและเขตเวลา ต่อท้ายด้วย **อักขระความกว้างศูนย์** กันธีมลบสี และวาดซ้ำอัตโนมัติเมื่อสีหลุด (พัก 5 นาที) |
| แตะเพื่อสลับ | **แตะ** เวลาเพื่อเปิด/ปิดวินาที (เลือก **แตะสองครั้ง** ได้) **กดค้าง** ใช้เปิดหน้าต่างดูตัวอย่าง ในช่วงที่มีงานอัตโนมัติจะแจ้งว่า "งานอัตโนมัติควบคุมอยู่" |
| สีทีละหลัก / ไล่ระดับ | หกหลัก **แต่ละหลักคนละสี** (10 สีสำเร็จรูป + แถบ HSV + ตามระบบ) และจุดสองจุดจัดกึ่งกลางเชิงสายตา หรือ **ไล่ระดับ** จากสีต้นทางถึงปลายทาง (6 ชุด ปรับสองหัวได้ บนพื้นสว่างจะลดความสว่าง) การ์ดตัวอย่าง **วาดเอง** จึงตรงกับแถบสถานะ |
| หน้าต่างกดค้าง | หน้าต่างกระจกฝ้าแสดง **เวลาจากเครือข่าย** ลากย้าย ปรับขนาดที่มุม ◣ จำตำแหน่งและขนาดไว้ รองรับไล่สีและเลขความกว้างเท่ากัน |
| งานอัตโนมัติ | ช่วงเวลา **ทุกชั่วโมง** หรือ **ทุกวัน**: เปิดวินาทีก่อน N นาที เป็นเวลา M นาที แล้วคืนค่า เปลี่ยนเวลาหรือเขตเวลาจะคำนวณใหม่ทันที พร้อมตรวจซ้ำทุก 60 วินาที |
| เทียบเวลาจากเครือข่าย | แสดงความต่างจาก **เวลาจากเครือข่าย** ปุ่ม **ซิงค์** จะเปิดการตั้งเวลาอัตโนมัติของระบบ |
| ไม่ต้องใช้ adb | เขียนผ่าน `Settings.Secure` หากไม่มี `WRITE_SECURE_SETTINGS` จะส่ง broadcast ให้ SystemUI เขียนแทน **ไม่ต้องใช้ adb** ในทุกฟีเจอร์ |
| ไทล์ในแผงควบคุม | เพิ่มไทล์ "แสดงวินาที" เพื่อเปิด/ปิดโดยไม่ต้องเปิดแอป |
| ชื่อรีซอร์สนาฬิกา | หากตรวจไม่พบ กด **ตรวจจับอัตโนมัติ** หรือกรอกชื่อรีซอร์สเอง (เช่น `clock`) |
| 21 ภาษาของอินเทอร์เฟซ | ตามภาษาของระบบ: 简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, ไทย, Deutsch, Español, Français, Українська, Polski, Português (Brasil), فارسی, العربية, Bahasa Indonesia, Italiano, Ελληνικά, ქართული และภาษาอื่นใช้ภาษาอังกฤษ |
| การจัดเก็บ / ขอบเขต | สถานะเก็บใน `Settings.Secure` ใช้ร่วมกันทุกโปรเซสและคงอยู่หลังรีบูต **ทดสอบบน ColorOS 15 / 16 (Android 15 / 16)** แกนหลักใช้ API สาธารณะจึงควรใช้กับยี่ห้ออื่นได้ แต่ทดสอบเฉพาะ OPPO / OnePlus ขั้นต่ำ Android 10 |

## รูปแบบการลงสี

สองแบบ สลับในการ์ด **สีหลัก** ด้วยแท็บ: **ทีละหลัก** หรือ **ไล่ระดับ** ทั้งข้อความ

<table><tr>
<td align="center" width="50%">
<img src="screenshots/colors-digit.png" alt="ทีละหลัก: แต่ละหลักคนละสี จุดสองจุดจัดกึ่งกลางเชิงสายตา" width="100%">
<br>
ทีละหลัก: แต่ละหลักคนละสี จุดสองจุดจัดกึ่งกลางเชิงสายตา
</td>
<td align="center" width="50%">
<img src="screenshots/colors-gradient.png" alt="ไล่ระดับ: จากสีต้นทางถึงปลายทาง 6 ชุด หรือเลือกสองหัวเอง" width="100%">
<br>
ไล่ระดับ: จากสีต้นทางถึงปลายทาง 6 ชุด หรือเลือกสองหัวเอง
</td>
</tr></table>

## ติดตั้ง

1. ดาวน์โหลด APK จากหน้า [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest)
2. เปิดโมดูลใน **LSPosed** และเลือกขอบเขต **System UI** (`com.android.systemui`)
3. รีสตาร์ตโปรเซส SystemUI หรือรีบูตเครื่อง
4. หน้า Settings เปิดจาก **ตัวจัดการ LSPosed** (จะไม่มีไอคอนในหน้าจอหลัก)

> ต้องมี root (LSPosed) **โดยปกติไม่ต้องให้สิทธิ์ adb**: โมดูลเขียนผ่าน `Settings.Secure` หากไม่มี `WRITE_SECURE_SETTINGS` จะส่ง broadcast ให้ SystemUI เขียนแทน ถ้าอยากให้หน้า Settings เขียน **โดยตรง** ให้รัน:
> ```bash
> adb shell pm grant io.github.kelebao.colorosclockseconds android.permission.WRITE_SECURE_SETTINGS
> ```
> ไม่ให้สิทธิ์ก็ใช้ได้: การแตะ งานอัตโนมัติ และหน้าต่างลอย ทำงานใน SystemUI ซึ่งมีสิทธิ์อยู่แล้ว

## การใช้งาน

- **แตะที่เวลา**: เปิด/ปิดวินาที (ค่าเริ่มต้น) ตั้งเป็น **แตะสองครั้ง** ได้
- **กดค้างที่เวลา**: หน้าต่างกระจกฝ้าแสดง **เวลาจากเครือข่าย** ลากย้ายได้ แตะที่เวลาเพื่อแสดงปุ่ม **ปิด** (ขวาบน) และ **ปรับขนาด** (ซ้ายล่าง)
- การ์ด **สีหลัก** มีสองหน้า และแท็บเองเป็นสวิตช์: **ทีละหลัก** หรือ **ไล่ระดับ**
- การ์ด **งานอัตโนมัติ** ตั้งช่วง "เปิดวินาทีก่อนถึงทุกชั่วโมง 2 นาที" ได้
- หน้า Settings แสดงสถานะ ท่าทาง สี ความต่างเวลาจากเครือข่าย และปุ่ม **ซิงค์** ในแตะเดียว

## ตัวอย่าง

<table><tr>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-off.jpg" alt="ปิดวินาที: `15:06` หลักสิบชั่วโมงคงสีที่เลือก" width="100%">
<br>
ปิดวินาที: `15:06` หลักสิบชั่วโมงคงสีที่เลือก
</td>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-on.jpg" alt="เปิดวินาที: `15:06:03` นาทีและวินาทีคงสีของระบบ" width="100%">
<br>
เปิดวินาที: `15:06:03` นาทีและวินาทีคงสีของระบบ
</td>
</tr></table>

## หลักการทำงาน

โมดูล hook เฉพาะ **SystemUI** และใช้ legacy Xposed API ที่เสถียร
**แกนหลักใช้เฉพาะ API สาธารณะของ AOSP คลาสเฉพาะแบรนด์เป็นทางเลือกและข้ามเมื่อไม่มี** จึงไม่ทำให้ระบบพังและไม่กระตุ้น safe mode ของ LSPosed:

- **วินาที**: hook `TextView.setText` (after) แคชข้อความเดิม แล้ว `TextView.onDraw` (before) เขียน `เดิม:ss` อัปเดตทุกวินาทีด้วย `Handler`
- **สีทีละหลัก**: ใส่ `ForegroundColorSpan` ทีละอักขระ (ลบ span แปลกปลอมก่อน) และยกจุดสองจุดขึ้น 0.11em ด้วย `CharacterStyle` เพราะเลย์เอาต์หลายท่อนวางกลิฟต่ำกว่า
- **ไล่ระดับ**: `CharacterStyle` เรียก `setShader` ใน `updateDrawState` การ์ดตัวอย่าง **วาดเอง** (`GradientTimeView`) เพราะ ColorOS 15 แบ่งช่วง shader ตาม run
- **สลับสถานะ**: แผงควบคุมดักการแตะบนแถบสถานะ โมดูลจึง hook `View.dispatchTouchEvent` ทั้งระบบ และสลับเฉพาะเมื่อจุดแตะอยู่บนนาฬิกาและแทบไม่ขยับ โดย **ไม่บริโภค** อีเวนต์
- **งานอัตโนมัติ**: เทียบ "เวลาท้องถิ่นอยู่ในช่วงหรือไม่" ตั้งเวลาที่ขอบช่วงแบบแม่นยำ และตรวจซ้ำทุก 60 วินาที

## การใช้พลังงาน

- **เปิดวินาที**: ใช้ตัวจับเวลาทุก 1 วินาที (ต้นทุนจำเป็น) วัด SystemUI ต่อหนึ่งคอร์: 7.0% ปิด / 7.25% เปิด (60 วินาที)
- **ปิดวินาที**: ตัวจับเวลา **หยุดทันที** (`stopTicker`) ไม่มีการตื่นทุกวินาที
- **การคงสี** ถอยหลัง 2s → 4s → … → 30s และหยุดเองเมื่อไม่หลุดสีนาน 5 นาที
- สถานะ **แคชในโปรเซส** ตัวจับเวลาไม่อ่าน `Settings.Secure` ทุกวินาที และงานอัตโนมัติใช้ `Handler.postDelayed` ไม่มี WakeLock/AlarmManager

## การคอมไพล์

ต้องมี **JDK 17+** (17 หรือ 21) และ **Android SDK Platform 35** (AGP จะดาวน์โหลด Build-Tools 35 ให้)

```bash
export JAVA_HOME=/path/to/jdk-17-or-21
./gradlew assembleDebug
```

## สำรองข้อมูลและการพัฒนาต่อ (สำคัญ)

ในโฟลเดอร์ `ClockSeconds/` เก็บ **ทุกอย่างยกเว้น `build/` และ `.gradle/`** (โค้ดราว 300 KB):

| หมวด | ไฟล์ | หมายเหตุ |
|------|------|------|
| ไฟล์บิลด์ | `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties`, `keystore.properties` | เวอร์ชัน ไลบรารี พารามิเตอร์ การเซ็น |
| Gradle Wrapper | `gradlew`, `gradlew.bat`, `gradle/wrapper/*` | ล็อก Gradle 8.14.5 |
| โค้ด | `app/src/main/java/**` (20 ไฟล์ `.java`) | ลอจิกโมดูล หน้า Settings คลาสวาด |
| รีซอร์ส | `app/src/main/res/**` | ข้อความ (22 ภาษา) ธีม |
| เมทาดาทา Xposed | `app/src/main/resources/META-INF/xposed/*` | คลาสทางเข้า ขอบเขต เวอร์ชัน API |
| Manifest | `app/src/main/AndroidManifest.xml` | สิทธิ์ Activity |
| สคริปต์ | `tools/**` | ตัวสร้าง README ตรวจ i18n และสคริปต์ปล่อยเวอร์ชัน |
| เอกสาร | `README*.md`, `docs/**`, `CHANGELOG.md` | คำอธิบาย ภาพหน้าจอ บันทึกเปลี่ยนเวอร์ชัน |
| CI | `.github/workflows/*.yml` | บิลด์เซ็นเมื่อมีแท็ก |

## แก้ปัญหา

<details>
<summary>ดูขั้นตอน</summary>

ดู logcat:

- **บิลด์ Release** ซ่อน log วินิจฉัยทั้งหมดหลัง `BuildConfig.DEBUG`
- เมื่อต้องดีบัก ให้ติดตั้ง **บิลด์ debug** (`assembleDebug` เซ็นคีย์เดียวกัน) แล้วกรอง:

  ```bash
  adb logcat -s ColorOSClockSeconds
  ```

- ไม่มีวินาทีเลย: ตรวจขอบเขต System UI ใน LSPosed แล้วรีสตาร์ต จากนั้นอ่านหรือเขียนสวิตช์:

  ```bash
  adb shell settings get secure coloros_clock_seconds_enabled   # 1=เปิด 0=ปิด
  adb shell settings put  secure coloros_clock_seconds_enabled 1
  ```

- สีหรือวินาทีกลับมาเอง: ระบบหรือธีมกำลังสร้างข้อความใหม่และโมดูลวาดซ้ำ หากไม่ฟื้นให้หา `color-guard stopped` / `span-theft` ใน log บิลด์ debug

</details>

## ข้อเสนอแนะ

คำถามและข้อเสนอ: เปิด [GitHub Issues](https://github.com/kelebao/ClockSeconds/issues)

## License

[MIT](LICENSE) © kelebao
