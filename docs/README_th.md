<div align="center">

[简体中文](../README.md) · [English](README_EN.md) · [繁體中文](README_zh-TW.md) · [日本語](README_ja.md) · [한국어](README_ko.md) · [Türkçe](README_tr.md) · [Русский](README_ru.md) · [Tiếng Việt](README_vi.md) · **ไทย** · [Deutsch](README_de.md) · [Español](README_es.md) · [Français](README_fr.md) · [Українська](README_uk.md) · [Polski](README_pl.md) · [Português (Brasil)](README_pt-BR.md) · [فارسی](README_fa.md) · [العربية](README_ar.md) · [Bahasa Indonesia](README_id.md) · [Italiano](README_it.md) · [Ελληνικά](README_el.md) · [ქართული](README_ka.md)

</div>

<div align="center">

# นาฬิกาแสดงวินาที (Clock Seconds)

แสดง**วินาที**บนนาฬิกาแถบสถานะ เปิด/ปิดด้วยการแตะครั้งเดียว และทำให้**หลักสิบของชั่วโมงเป็นสีแดง**เสมอ<br>ไม่พึ่งพาคลาสส่วนตัวของผู้ผลิตระบบแม้แต่ตัวใด ยืนยันแล้วบนเครื่องจริง ColorOS 15 พร้อมรองรับ Xiaomi, OnePlus, Honor, vivo และผู้ผลิตรายใหญ่อื่น ๆ

[![version](https://img.shields.io/badge/version-1.0.4-blue.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/releases/latest)
![android](https://img.shields.io/badge/Android-10%2B-brightgreen.svg?style=flat-square)
[![lsposed](https://img.shields.io/badge/LSPosed-module-orange.svg?style=flat-square)](https://github.com/Xposed-Modules-Repo/io.github.kelebao.colorosclockseconds/releases)
![coloros](https://img.shields.io/badge/ColorOS%2015-verified-success.svg?style=flat-square)
[![stars](https://img.shields.io/github/stars/kelebao/ClockSeconds.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/stargazers)
[![license](https://img.shields.io/badge/license-MIT-lightgrey.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/blob/main/LICENSE)

</div>

---

## ความสามารถ

| ความสามารถ | คำอธิบาย |
|------|------|
| แสดงวินาที | นาฬิกาแถบสถานะแสดงถึงวินาที (`HH:mm:ss` / `h:mm:ss a` ตามการตั้งค่า 12/24 ชั่วโมงของระบบ) |
| แตะเพื่อเปิด/ปิด | **แตะ**นาฬิกาแถบสถานะเพื่อเปิด/ปิดการแสดงวินาที สามารถเปลี่ยนเป็น**กดค้าง**ได้ในหน้าตั้งค่า |
| ตัวเลขชั่วโมงสีแดง | **หลักสิบของชั่วโมง**ถูกระบายสีแดง (เช่น `1` ใน `13`, `0` ใน `05`) ส่วนนาทีและวินาทีคงสีเดิมของระบบ ชั่วโมงที่เหลือหลักเดียว (เช่น `9:45`) ไม่มีหลักสิบจึงไม่ถูกแตะ ใช้ได้กับทุกผู้ผลิต |
| ภาษาของอินเทอร์เฟซ | อินเทอร์เฟซแอปรองรับ **21 ภาษา** (สลับตามภาษาของระบบอัตโนมัติ): 简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, ไทย, Deutsch, Español, Français, Українська, Polski, Português (Brasil), فارسی, العربية, Bahasa Indonesia, Italiano, Ελληνικά, ქართული |
| การเก็บสถานะถาวร | สถานะเก็บไว้ใน `Settings.Secure` ใช้ร่วมระหว่างโปรเซส และคงอยู่หลังจากรีสตาร์ท SystemUI |
| ขอบเขตการรองรับ | ยืนยันแล้วบน ColorOS 15 / 16 / 17 (OPPO / OnePlus / realme) และทำงานได้ทันทีบนผู้ผลิตอื่น ๆ ที่รูทแล้ว เช่น Xiaomi / Honor / Redmi ไม่พึ่งพาคลาสส่วนตัวของผู้ผลิต จึงไม่ทำให้ LSPosed เข้าสู่โหมดปลอดภัย รองรับต่ำถึง Android 10 |

## การติดตั้ง

1. ดาวน์โหลด APK รุ่นล่าสุดจากหน้า [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest)
2. เปิดใช้โมดูลใน **LSPosed** ขอบเขต: **อินเทอร์เฟซระบบ (`com.android.systemui`)**
3. รีสตาร์ทโปรเซส SystemUI หรือรีบูตเครื่อง
4. เปิดหน้าตั้งค่าจาก **ตัวจัดการ LSPosed** (ไม่สร้างไอคอนบนหน้าจอหลัก)

> ต้องมีรูท (LSPosed) การเขียน `Settings.Secure` ต้องให้สิทธิ์ `WRITE_SECURE_SETTINGS`:
> ```bash
> adb shell pm grant io.github.kelebao.colorosclockseconds android.permission.WRITE_SECURE_SETTINGS
> ```
> หากยังไม่ให้สิทธิ์ ก็ยังแตะนาฬิกาแถบสถานะเพื่อเปิด/ปิดได้ (การกระทำนี้ทำงานอยู่ในโปรเซส SystemUI ซึ่งมีสิทธิ์อยู่แล้ว)

## วิธีใช้

- **แตะนาฬิกาแถบสถานะ**: เปิด/ปิดการแสดงวินาที (ท่ามือเริ่มต้น)
- การเปิด/ปิดทำผ่านการตรวจจับการแตะแบบทั่วระบบ จึง**ไม่ขัดแย้งกับการ"ดึงแผงแจ้งเตือนลง"**——แตะล้วนเพื่อเปิด/ปิด ส่วนการปัดยังดึงแผงแจ้งเตือนลงตามปกติ ถ้าถนัดกว่านั้นสามารถเปลี่ยนท่ามือเป็น**กดค้าง**ได้ในหน้าตั้งค่า
- หน้าตั้งค่าสามารถดูสถานะปัจจุบัน ท่ามือที่เลือก และรีเฟรชสถานะได้

## หลักการทำงาน

โมดูลฮุคเพียง**โปรเซส SystemUI** เท่านั้น ใช้แนวทาง legacy Xposed ที่ผ่านการยืนยันแล้ว
**ไม่แตะคลาสส่วนตัวของผู้ผลิตแม้แต่ตัวหนึ่ง** เพราะฉะนั้นไม่ทำให้สถานะภายในของระบบเสียหาย และไม่ทำให้ LSPosed เข้าสู่โหมดปลอดภัย:

- **แสดงวินาที**: ฮุค `TextView.setText` (after) ที่เสถียร เพื่อแคชข้อความดั้งเดิมที่ระบบใส่ไว้บนนาฬิกาแถบสถานะ
  จากนั้นฮุค `TextView.onDraw` (before) เพื่อเขียนทับการแสดงผลเป็น `ข้อความดั้งเดิม:ss` หลังจากระบบตั้งข้อความเรียบร้อยแล้ว
  ขับเคลื่อนด้วย `Handler` ระยะ 1 วินาที วินาทีที่ถูกเพิ่มเข้ามาจะคงสีข้อความเดิมของนาฬิกาของระบบ (วัดได้เป็น `#ccffffff`)
  พร้อมกับระบายสีแดงให้"หลักสิบของชั่วโมง" (เติมชุดสีแดง `ForegroundColorSpan`ให้ตัวอักษรนั้น) ไม่ว่าการแสดงวินาทีจะเปิดหรือปิด ก็ยังคงสีแดงไว้
  ไม่ระบายสีแดงนาทีและวินาที ชั่วโมงที่เหลือหลักเดียวไม่มีหลักสิบจึงไม่ถูกแตะ
- **เปิด/ปิด**: แผงแจ้งเตือนของ ColorOS ขัดขวางการแตะในบริเวณแถบสถานะ ดังนั้น `OnTouchListener` ของนาฬิกาเองจึงไม่ได้รับเหตุการณ์เลย
  โมดูลจึงเปลี่ยนไปฮุค `View.dispatchTouchEvent` ของระบบทั่วทั้งหมด และพลิกสวิตช์ `Settings.Secure` เมื่อจุดกดตกอยู่ในกรอบหน้าจอ
  ของนาฬิกาที่จำได้ และเคลื่อนที่เกือบไม่มี (การแตะ) หรือกดค้างไว้ 500 มิลลิวินาที (กดค้าง) โมดูล**ไม่กลืนเหตุการณ์การแตะใด ๆ ทั้งสิ้น**
  เพราะฉะนั้นการดึงแผงแจ้งเตือนลงยังทำงานตามปกติ

## หมายเหตุเรื่องการประหยัดไฟ

- **เมื่อเปิดการแสดงวินาที**: โมดูลต้องใช้ตัวจับเวลาระยะ 1 วินาทีขับการรีเฟรช (นี่คือต้นทุนอยู่ในตัวของ"การแสดงวินาที" ประมาณ 60 ครั้งต่อนาทีของการวาดนาฬิกาใหม่)
- **เมื่อปิดการแสดงวินาที**: โมดูลจะ**หยุดตัวจับเวลาเหล่านั้นทันที** (`stopTicker`) ไม่มีการตื่นทุกวินาทีอีกต่อไป และไม่มีการอ่านข้ามโปรเซส
  กลับไปอยู่ในจังหวะ"รีเฟรชครั้งเดียวต่อนาที"แบบเดิมของระบบ เพราะฉะนั้นการปิดการแสดงวินาที จึงประหยัดพลังงานได้จริง
- สถานะถูกเก็บใน**แคชภายในโปรเซส** (`sSecondsEnabled`) ตัวจับเวลาไม่ไปอ่าน `Settings.Secure` ทุกวินาที (หลีกเลี่ยง IPC ทุกวินาที)
- โมดูลไม่สร้างตัวจัดรูปแบบเวลาใด ๆ ใช้ข้อความดั้งเดิมของระบบซ้ำแล้วเพียงต่ออกดู `:ss` เพราะฉะนั้นไม่มีโหลดจากตัวจัดรูปแบบ
- ตัวจับเวลาระยะถือ**การอ้างอิงแบบอ่อน**ต่อวิวเท่านั้น และทำงานต่อก็ต่อเมื่อวิวยัง `isAttachedToWindow()` เมื่อวิวถูกทำลายจะออกจากวงเองโดยอัตโนมัติ

## การ Build

ต้องมี **JDK 17+** (แนะนำ 17 หรือ 21) และ **Android SDK Platform 35** (Build-Tools 35 ติดตั้งเองโดย AGP)

```bash
export JAVA_HOME=/path/to/jdk-17-or-21
./gradlew assembleDebug
```

## การสำรองข้อมูลและการพัฒนาต่อ (สำคัญ)

ในไดเรกทอรี `ClockSeconds/` **ควรเก็บทุกไฟล์ ยกเว้น `build/` และ `.gradle/`** (ส่วนโค้ดมีเพียงราว 140 KB):

| ประเภท | ไฟล์ | หมายเหตุ |
|------|------|------|
| สคริปต์ Build | `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties` | เวอร์ชัน ตัวเชื่อมพาร์ท และพารามิเตอร์คอมไพล์ |
| Gradle Wrapper | `gradlew`, `gradlew.bat`, `gradle/wrapper/*` | ล็อก Gradle 8.14.5 |
| โค้ดต้นฉบับ | `app/src/main/java/**` (3 ไฟล์ `.java`) | ลอจิกหลักของโมดูล |
| รีซอร์ส | `app/src/main/res/**` | ข้อความ (ภาษาจีนเริ่มต้น + 21 ภาษา values-xx) และธีม |
| เมทาดาต้า Xposed | `app/src/main/resources/META-INF/xposed/*` | คลาสทางการเข้า ระยะให้บริการ และเวอร์ชัน API |
| Manifest | `app/src/main/AndroidManifest.xml` | สิทธิ์ และ Activity |
| เอกสาร | `README*.md` | เอกสาร 21 ภาษา |
| CI | `.github/workflows/release.yml` | ติดแท็กแล้วก็ได้ไฟล์เซ็นชื่อ |

## การแก้ไขปัญหา

<details>
<summary>ขยายขั้นตอนการแก้ไขปัญหา</summary>

ดู logcat:

- **บิลด์ Release** ใช้เงื่อนไข `BuildConfig.DEBUG` กรองล็อกวินิจฉัยออกหมดแล้ว (เล็กลง สะอาดขึ้น) logcat จะไม่แสดงล็อกของโมดูลนี้
- หากต้องตรวจสอบ ให้ติดตั้ง**บิลด์ debug**ชั่วคราว (`assembleDebug` เซ็นด้วยproduction key ตัวเดียวกับของรุ่นปล่อย จึงติดตั้งทับของ release ได้ตรง ๆ) จากนั้นกรองด้วย TAG:

  ```bash
  adb logcat -s ColorOSClockSeconds
  ```

- ถ้าไม่แสดงวินาทีเลย: ขอเช็กก่อนว่า LSPosed ติ๊กระยะ SystemUI และรีสตาร์ทแล้ว จากนั้นเช็กว่าสวิตช์ถูกบันทึกไว้ว่าปิดอยู่——
  อ่าน / บังคับเขียนผ่าน adb ได้:

  ```bash
  adb shell settings get secure coloros_clock_seconds_enabled   # 1=เปิด 0=ปิด
  adb shell settings put  secure coloros_clock_seconds_enabled 1
  ```

- ถ้าไม่มีผลบนเครื่องที่ไม่ใช่ ColorOS: ติดตั้งบิลด์ debug ดูล็อกที่บรรทัด `candidate:` / `CONFIG CLOCK:`
  เพื่อยืนยันว่าชื่อรีซอร์ส / ชื่อคลาสของนาฬิกาแถบสถานะบนเครื่องนั้น อยู่นอกกฎการจำหรือเปล่า ถ้าอยู่นอก เพียงเติมกฎหนึ่งบรรทัดก็เพียงพอ

</details>

## License

[MIT](LICENSE) © kelebao
