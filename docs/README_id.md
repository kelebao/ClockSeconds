<div align="center">

[简体中文](../README.md) · [English](README_EN.md) · [繁體中文](README_zh-TW.md) · [日本語](README_ja.md) · [한국어](README_ko.md) · [Türkçe](README_tr.md) · [Русский](README_ru.md) · [Tiếng Việt](README_vi.md) · [ไทย](README_th.md) · [Deutsch](README_de.md) · [Español](README_es.md) · [Français](README_fr.md) · [Українська](README_uk.md) · [Polski](README_pl.md) · [Português (Brasil)](README_pt-BR.md) · [فارسی](README_fa.md) · [العربية](README_ar.md) · **Bahasa Indonesia** · [Italiano](README_it.md) · [Ελληνικά](README_el.md) · [ქართული](README_ka.md)

</div>

<div align="center">

# Detik di bilah status (Show Seconds in Time)

Menampilkan **detik** pada jam bilah status, bisa diganti dengan ketukan. Anda bisa memberi **warna pada setiap angka** atau memasang **gradien** pada seluruh teks.<br>**Tekan lama** membuka jendela tembus pandang berisi **waktu jaringan** (geser, ukuran, gradien); **tugas sendiri** menyalakan detik pada jam yang dipilih.<br>Jalur utama hanya memakai API publik AOSP (kelas khusus OPPO / OnePlus / realme opsional dan dilewati bila tidak ada). Diuji pada ColorOS 15 / 16.

[![version](https://img.shields.io/badge/version-1.2.0-blue.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/releases/latest)
![android](https://img.shields.io/badge/Android-10%2B-brightgreen.svg?style=flat-square)
[![lsposed](https://img.shields.io/badge/LSPosed-module-orange.svg?style=flat-square)](https://github.com/Xposed-Modules-Repo/io.github.kelebao.colorosclockseconds/releases)
![coloros](https://img.shields.io/badge/ColorOS%2015%2F16-verified-success.svg?style=flat-square)
[![stars](https://img.shields.io/github/stars/kelebao/ClockSeconds.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/stargazers)
[![license](https://img.shields.io/badge/license-MIT-lightgrey.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/blob/main/LICENSE)

</div>

---

## Fitur

| Fitur | Keterangan |
|------|------|
| Detik di bilah status | `HH:mm:ss` / `h:mm:ss a`, mengikuti format 12/24 jam dan zona waktu. Di akhir ditambahkan **spasi lebar nol** agar tema tidak menghapus warna, dan warna digambar ulang otomatis bila hilang (jeda 5 menit). |
| Ganti dengan ketukan | **Ketuk** jam untuk menyalakan/mematikan detik (bisa memilih **ketuk dua kali**); **tekan lama** dipakai untuk jendela pratinjau. Di dalam jendela tugas muncul pesan bahwa tugas yang mengatur. |
| Warna per angka / gradien | Enam angka **masing-masing satu warna** (10 preset + slider HSV + warna sistem) dan dua titik ditengahkan secara optis. Atau **gradien** dari warna awal ke akhir (6 set, kedua ujung bisa diubah, digelapkan di latar terang). Kartu pratinjau **menggambar sendiri** sehingga cocok dengan bilah status. |
| Jendela tekan lama | Jendela tembus pandang berisi **waktu jaringan**; geser untuk memindah, sudut ◣ untuk ukuran, posisi dan ukuran **diingat**; mendukung gradien dan angka sama lebar. |
| Tugas sendiri | Jendela **tiap jam** atau **tiap hari**: nyalakan detik N menit sebelum selama M menit lalu kembalikan. Perubahan waktu atau zona langsung dihitung ulang, plus pemeriksaan tiap 60 detik. |
| Bandingkan waktu jaringan | Menampilkan selisih dengan **waktu jaringan**; tombol **Sinkronkan** menyalakan penyetelan otomatis sistem. |
| Tanpa adb | Penulisan lewat `Settings.Secure`; tanpa `WRITE_SECURE_SETTINGS` dikirim broadcast dan SystemUI yang menulis — **tidak ada fitur yang butuh adb**. |
| Ubin cepat | Tambahkan ubin “Tampilkan detik” untuk berganti tanpa membuka aplikasi. |
| Nama resource jam | Bila jam tidak terdeteksi, tekan **Deteksi otomatis** atau isi nama resource (mis. `clock`). |
| 21 bahasa antarmuka | Mengikuti bahasa sistem: 简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, ไทย, Deutsch, Español, Français, Українська, Polski, Português (Brasil), فارسی, العربية, Bahasa Indonesia, Italiano, Ελληνικά, ქართული; sisanya Inggris. |
| Penyimpanan / cakupan | Status disimpan di `Settings.Secure`, dipakai bersama antarproses dan bertahan setelah reboot. **Diuji pada ColorOS 15 / 16 (Android 15 / 16)**; inti hanya memakai API publik sehingga merek lain semestinya jalan, tetapi baru diuji di OPPO / OnePlus. Minimal Android 10. |

## Mode warna

Dua cara mewarnai, diganti di kartu **Warna angka**: **per angka** atau **gradien** seluruh teks.

<table><tr>
<td align="center" width="50%">
<img src="screenshots/colors-digit.png" alt="Per angka: tiap angka satu warna, dua titik ditengahkan optis" width="100%">
<br>
Per angka: tiap angka satu warna, dua titik ditengahkan optis
</td>
<td align="center" width="50%">
<img src="screenshots/colors-gradient.png" alt="Gradien: dari warna awal ke akhir, 6 set atau ujung sendiri" width="100%">
<br>
Gradien: dari warna awal ke akhir, 6 set atau ujung sendiri
</td>
</tr></table>

## Pemasangan

1. Unduh APK dari halaman [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest)
2. Aktifkan modul di **LSPosed** dan pilih cakupan **Antarmuka sistem** (`com.android.systemui`)
3. Mulai ulang proses SystemUI atau perangkat
4. Layar pengaturan dibuka dari **pengelola LSPosed** (tidak membuat ikon di launcher)

> Butuh root (LSPosed). **Izin adb biasanya tidak perlu**: penulisan lewat `Settings.Secure` dan, tanpa `WRITE_SECURE_SETTINGS`, lewat broadcast yang dikerjakan SystemUI. Bila ingin layar pengaturan menulis **langsung**:
> ```bash
> adb shell pm grant io.github.kelebao.colorosclockseconds android.permission.WRITE_SECURE_SETTINGS
> ```
> Tanpa memberi izin semuanya tetap jalan: ketukan, tugas, dan jendela berjalan di dalam SystemUI.

## Pemakaian

- **Ketuk jam**: detik nyala/mati (bawaan); di pengaturan bisa dipilih **ketuk dua kali**.
- **Tekan lama**: jendela tembus pandang berisi **waktu jaringan**, geser untuk memindah; ketuk jam untuk memunculkan **tutup** (kanan atas) dan **ukuran** (kiri bawah).
- Kartu **Warna angka** punya dua halaman dan tab itu sendiri berfungsi sebagai sakelar.
- Kartu **Tugas sendiri** menyusun jendela seperti “detik 2 menit sebelum tiap jam”.
- Pergantian memakai pemeriksaan ketukan global dan **tidak bentrok dengan menarik panel**.

## Pratinjau

<table><tr>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-off.jpg" alt="Detik mati: `15:06`, puluhan jam tetap berwarna" width="100%">
<br>
Detik mati: `15:06`, puluhan jam tetap berwarna
</td>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-on.jpg" alt="Detik nyala: `15:06:03`, menit dan detik memakai warna sistem" width="100%">
<br>
Detik nyala: `15:06:03`, menit dan detik memakai warna sistem
</td>
</tr></table>

## Cara kerja

Modul hanya meng-hook **SystemUI** dan memakai API legacy Xposed yang stabil.
**Inti hanya memakai API publik AOSP, kelas khusus vendor opsional dan dilewati bila tidak ada**, jadi tidak merusak sistem dan tidak memicu mode aman LSPosed:

- **Detik**: `TextView.setText` (after) menyimpan teks sistem, lalu `TextView.onDraw` (before) menulis `asli:ss`, diperbarui tiap detik lewat `Handler`.
- **Warna per angka**: `ForegroundColorSpan` per karakter (span asing dihapus); dua titik dinaikkan 0.11em karena tata letak bersegmen banyak menaruh glif lebih rendah.
- **Gradien**: `CharacterStyle` memanggil `setShader` di `updateDrawState`; pratinjau **menggambar sendiri** (`GradientTimeView`) karena ColorOS 15 mengkuantisasi shader per segmen teks.
- **Pergantian**: panel menelan ketukan di bilah status, jadi modul meng-hook `View.dispatchTouchEvent` global dan hanya berganti bila ketukan berada di dalam jam tanpa geseran; event **tidak dikonsumsi**.
- **Tugas**: memeriksa “apakah waktu lokal di dalam jendela”, timer tepat di batas plus pemeriksaan tiap 60 detik.

## Konsumsi daya

- **Detik nyala**: timer tiap detik (biaya tak terhindar). Ukur SystemUI per inti: 7.0% mati, 7.25% nyala (60 detik).
- **Detik mati**: timer **langsung berhenti** (`stopTicker`), tidak ada bangun tiap detik.
- **Perlindungan warna** melambat 2s → 4s → … → 30s dan berhenti setelah 5 menit tanpa kehilangan.
- Status **di-cache di proses**; tugas memakai `Handler.postDelayed`: **tanpa WakeLock dan AlarmManager**, tidak membangunkan tidur lelap.

## Membangun

Butuh **JDK 17+** (17 atau 21) dan **Android SDK Platform 35** (AGP mengunduh Build-Tools 35).

```bash
export JAVA_HOME=/path/to/jdk-17-or-21
./gradlew assembleDebug
```

## Cadangan dan kelanjutan pengembangan (penting)

Di folder `ClockSeconds/` simpan **semua kecuali `build/` dan `.gradle/`** (kode sekitar 300 KB):

| Kategori | Berkas | Catatan |
|------|------|------|
| Berkas build | `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties`, `keystore.properties` | Versi, dependensi, parameter, tanda tangan |
| Gradle Wrapper | `gradlew`, `gradlew.bat`, `gradle/wrapper/*` | Mengunci Gradle 8.14.5 |
| Kode | `app/src/main/java/**` (20 `.java`) | Logika modul, pengaturan, kelas gambar |
| Resource | `app/src/main/res/**` | Teks (22 bahasa), tema |
| Metadata Xposed | `app/src/main/resources/META-INF/xposed/*` | Kelas masuk, cakupan, level API |
| Manifest | `app/src/main/AndroidManifest.xml` | Izin, aktivitas |
| Alat | `tools/**` | Pembuat README, pemeriksa i18n, skrip rilis |
| Dokumen | `README*.md`, `docs/**`, `CHANGELOG.md` | Penjelasan, tangkapan layar, riwayat perubahan |
| CI | `.github/workflows/*.yml` | Build bertanda tangan saat tag |

## Pemecahan masalah

<details>
<summary>Lihat langkah</summary>

Periksa logcat:

- **Build release** menyembunyikan semua log diagnostik di balik `BuildConfig.DEBUG`.
- Untuk menelusuri, pasang **build debug** (`assembleDebug`, kunci sama) lalu saring:

  ```bash
  adb logcat -s ColorOSClockSeconds
  ```

- Detik tidak muncul: periksa cakupan “Antarmuka sistem” di LSPosed lalu mulai ulang; kemudian baca atau tulis sakelarnya:

  ```bash
  adb shell settings get secure coloros_clock_seconds_enabled   # 1=on 0=off
  adb shell settings put  secure coloros_clock_seconds_enabled 1
  ```

- Warna atau detik kembali sendiri: sistem atau tema menyusun ulang teks dan modul menggambar lagi; bila tak pulih, cari `color-guard stopped` / `span-theft` di log debug.

</details>

## Masukan

Pertanyaan dan saran lewat [GitHub Issues](https://github.com/kelebao/ClockSeconds/issues).

## License

[MIT](LICENSE) © kelebao
