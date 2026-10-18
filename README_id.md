<div align="center">

[简体中文](README.md) · [English](README_EN.md) · [繁體中文](README_zh-TW.md) · [日本語](README_ja.md) · [한국어](README_ko.md) · [Türkçe](README_tr.md) · [Русский](README_ru.md) · [Tiếng Việt](README_vi.md) · [ไทย](README_th.md) · [Deutsch](README_de.md) · [Español](README_es.md) · [Français](README_fr.md) · [Українська](README_uk.md) · [Polski](README_pl.md) · [Português (Brasil)](README_pt-BR.md) · [فارسی](README_fa.md) · [العربية](README_ar.md) · **Bahasa Indonesia** · [Italiano](README_it.md) · [Ελληνικά](README_el.md) · [ქართული](README_ka.md)

</div>

<div align="center">

# Detik di Jam (Clock Seconds)

Tampilkan **detik** pada jam bilah status, alihkan dengan sekali sentuh, dan buat **puluhan jam tetap merah**.<br>Tanpa kelas privat vendor mana pun. Terverifikasi di perangkat nyata dengan ColorOS 15; juga mencakup Xiaomi, OnePlus, Honor, vivo, dan vendor besar lainnya.

![version](https://img.shields.io/badge/version-1.0.4-blue.svg?style=flat-square)
![android](https://img.shields.io/badge/Android-10%2B-brightgreen.svg?style=flat-square)
![lsposed](https://img.shields.io/badge/LSPosed-module-orange.svg?style=flat-square)
![coloros](https://img.shields.io/badge/ColorOS%2015-verified-success.svg?style=flat-square)
![stars](https://img.shields.com/github/stars/kelebao/ColorOS15ClockSeconds.svg?style=flat-square)
![license](https://img.shields.io/badge/license-MIT-lightgrey.svg?style=flat-square)

</div>

---

## Fitur

| Fitur | Keterangan |
|------|------|
| Tampilan detik | Jam bilah status menampilkan hingga detik (`HH:mm:ss` / `h:mm:ss a`, mengikuti setelan 12/24 jam sistem) |
| Sentuh untuk alih | **Mengetuk** jam bilah status akan mengubah tampilan detik on/off; gestur **tekan lama** juga tersedia di halaman setelan |
| Puluhan jam merah | **Puluhan jam** diwarnai merah (angka `1` pada `13`, angka `0` pada `05`); menit dan detik tetap berwarna sistem. Jam satu angka (mis. `9:45`) tidak punya puluhan sehingga tidak tersentuh. Berfungsi di semua vendor |
| Bahasa antarmuka | Antarmuka aplikasi diterjemahkan ke **21 bahasa** (otomatis mengikuti bahasa sistem): 简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, ไทย, Deutsch, Español, Français, Українська, Polski, Português (Brasil), فارسی, العربية, Bahasa Indonesia, Italiano, Ελληνικά, ქართული |
| Persistensi | Status disimpan di `Settings.Secure`, dibagi antar proses, dan bertahan setelah SystemUI dimulai ulang |
| Kompatibilitas | Terverifikasi di ColorOS 15 / 16 / 17 (OPPO / OnePlus / realme); juga langsung berfungsi di vendor lain yang sudah root seperti Xiaomi / Honor / Redmi. Tanpa kelas privat vendor, sehingga mode aman LSPosed tidak akan terpicu. Minimum Android 10 |

## Instalasi

1. Unduh APK terbaru dari halaman [**Releases**](https://github.com/kelebao/ColorOS15ClockSeconds/releases/latest)
2. Aktifkan modul di **LSPosed**, cakupan: **Antarmuka Sistem (`com.android.systemui`)**
3. Mulai ulang proses SystemUI atau mulai ulang perangkat
4. Buka halaman setelan dari **manajer LSPosed** (tidak ada ikon yang dibuat di peluncur)

> Memerlukan root (LSPosed). Untuk memberikan `WRITE_SECURE_SETTINGS` agar bisa menulis ke `Settings.Secure`:
> ```bash
> adb shell pm grant io.github.kelebao.colorosclockseconds android.permission.WRITE_SECURE_SETTINGS
> ```
> Tanpa izin itu Anda tetap bisa mengalihkan dengan mengetuk jam bilah status (aksi itu berjalan di dalam proses SystemUI yang memang sudah punya izin).

## Cara pakai

- **Ketuk jam bilah status**: mengalihkan tampilan detik on/off (gestur bawaan).
- Pengalihan diimplementasikan dengan uji sentuh global, jadi **tidak pernah bertabrakan dengan "menarik panel notifikasi ke bawah"**—— ketukan murni mengalihkan, tarikan tetap menarik panel ke bawah. Bila lebih suka, Anda bisa mengganti gestur menjadi **tekan lama** di halaman setelan.
- Halaman setelan menampilkan status saat ini, gestur yang dipilih, dan tombol segarkan.

## Cara kerja

Modul hanya meng-hook **SystemUI**, dengan pendekatan legacy Xposed yang sudah teruji,
dan **sama sekali tidak menyentuh kelas privat vendor mana pun**, sehingga tidak merusak status internal sistem dan tidak memicu mode aman LSPosed:

- **Detik**: hook `TextView.setText` (after) yang stabil untuk menyimpan mentahan teks yang sistem berikan pada jam bilah status,
  lalu hook `TextView.onDraw` (before) untuk menimpa tampilan dengan `mentahanTeks:ss` setelah sistem selesai mengatur teks,
  didorong `Handler` 1 detik. Detik yang ditambahkan mewarisi warna teks jam itu sendiri (terukur `#ccffffff`).
  Selain itu "puluhan jam" diwarnai merah (sebuah `ForegroundColorSpan` merah pada satu karakter itu), baik tampilan detik dinyalakan maupun dimatikan;
  menit dan detik tidak pernah merah; jam satu angka tidak punya puluhan sehingga tidak tersentuh.
- **Alihkan**: panel notifikasi ColorOS mencegat sentuh di wilayah bilah status, sehingga `OnTouchListener` jam itu sendiri tidak pernah menerima peristiwa.
  Modul karena itu meng-hook `View.dispatchTouchEvent` global dan hanya membalik sakelar `Settings.Secure` ketika titik tekan jatuh di dalam
  batas layar jam yang terdeteksi dan hampir tidak bergerak (ketuk) atau ditahan 500 ms (tekan lama). Modul **tidak mengonsumsi**
  peristiwa sentuh apa pun, sehingga menarik panel notifikasi ke bawah bekerja seperti biasa.

## Catatan hemat daya

- **Detik NYALA**: modul memerlukan pewaktu 1 detik untuk mendorong penyegaran (ini adalah biaya melekat dari "menampilkan detik", sekitar 60 penggambaran ulang jam per menit).
- **Detik MATI**: modul **segera menghentikan pewaktu itu** (`stopTicker`)—— sama sekali tidak ada kebangunan tiap detik dan tidak ada pembacaan antar proses.
  Ia kembali ke ritme asli sistem "menyegarkan sekali per menit", jadi mematikan tampilan detik memang lebih hemat daya.
- Status disimpan di **cache dalam proses** (`sSecondsEnabled`), sehingga pewaktu tidak membaca `Settings.Secure` setiap detik (menghindari IPC tiap detik).
- Tidak ada pemformat waktu yang dibuat: modul memakai ulang mentahan teks sistem dan hanya menambahkan `:ss`, jadi tidak ada overhead pemformatan.
- Pewaktu hanya menyimpan **referensi lemah** ke view dan hanya berlanjut selama view `isAttachedToWindow()`; setelah view dihancurkan ia keluar otomatis.

## Build

Membutuhkan **JDK 17+** (17 atau 21 direkomendasikan) dan **Android SDK Platform 35** (Build-Tools 35 dipasang otomatis oleh AGP).

```bash
export JAVA_HOME=/path/to/jdk-17-or-21
./gradlew assembleDebug
```

## Pencadangan & pengembangan lanjutan (penting)

Di direktori `ColorOS15ClockSeconds/`, **semua berkas kecuali `build/` dan `.gradle/` harus dipertahankan** (bagian kode hanya sekitar 140 KB):

| Kategori | Berkas | Keterangan |
|------|------|------|
| Skrip build | `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties` | Versi, dependensi, parameter kompilator |
| Gradle Wrapper | `gradlew`, `gradlew.bat`, `gradle/wrapper/*` | Mengunci Gradle 8.14.5 |
| Kode sumber | `app/src/main/java/**` (3 berkas `.java`) | Inti logika modul |
| Sumber daya | `app/src/main/res/**` | String (Mandarin sebagai bawaan + 21 bahasa values-xx), tema |
| Metadata Xposed | `app/src/main/resources/META-INF/xposed/*` | Kelas masuk, cakupan, versi API |
| Manifest | `app/src/main/AndroidManifest.xml` | Izin, Activity |
| Dokumentasi | `README*.md` | Dokumentasi 21 bahasa |
| CI | `.github/workflows/release.yml` | Tag menghasilkan build tertandatangani |

## Pemecahan masalah

<details>
<summary>Buka langkah pemecahan masalah</summary>

Lihat logcat:

- **Build Release** telah membuang semua log diagnostik melalui gerbang `BuildConfig.DEBUG` (lebih kecil, lebih bersih); tidak ada apa pun dari modul ini di logcat.
- Untuk memeriksa, pasang sementara **build debug** (`assembleDebug`, ditandatangani dengan kunci produksi yang sama jadi bisa langsung menimpa instalan Release), lalu saring dengan TAG:

  ```bash
  adb logcat -s ColorOSClockSeconds
  ```

- Bila detik tidak muncul sama sekali: pastikan dulu LSPosed sudah mencentang cakupan SystemUI dan sudah dimulai ulang; lalu periksa apakah sakelar tersimpan dalam keadaan mati——
  bisa dibaca / dipaksa lewat adb:

  ```bash
  adb shell settings get secure coloros_clock_seconds_enabled   # 1=nyala 0=mati
  adb shell settings put  secure coloros_clock_seconds_enabled 1
  ```

- Bila tidak berefek di perangkat non-ColorOS: pasang build debug dan lihat baris `candidate:` / `CONFIG CLOCK:` di log
  untuk memastikan nama sumber / nama kelas jam bilah status perangkat Anda sudah tercakup oleh aturan pengenalan; jika belum, cukup tambah satu baris aturan.

</details>

## License

[MIT](LICENSE) © kelebao
