# -*- coding: utf-8 -*-
"""Slot translations for README generation: Bahasa Indonesia (id)."""

TEXTS = {
    'id': {
        'title': 'Detik di bilah status (Show Seconds in Time)',
        'tagline': (
            'Menampilkan **detik** pada jam bilah status, bisa diganti dengan ketukan. Anda bisa memberi '
            '**warna pada setiap angka** atau memasang **gradien** pada seluruh teks.<br>'
            '**Tekan lama** membuka jendela tembus pandang berisi **waktu jaringan** (geser, ukuran, gradien); '
            '**tugas sendiri** menyalakan detik pada jam yang dipilih.<br>'
            'Jalur utama hanya memakai API publik AOSP (kelas khusus OPPO / OnePlus / realme opsional dan '
            'dilewati bila tidak ada). Diuji pada ColorOS 15 / 16.'
        ),
        'h_features': 'Fitur',
        'th_feature': 'Fitur',
        'th_desc': 'Keterangan',
        'rows': [
            ('Detik di bilah status',
             '`HH:mm:ss` / `h:mm:ss a`, mengikuti format 12/24 jam dan zona waktu. Di akhir ditambahkan '
             '**spasi lebar nol** agar tema tidak menghapus warna, dan warna digambar ulang otomatis bila hilang '
             '(jeda 5 menit).'),
            ('Ganti dengan ketukan',
             '**Ketuk** jam untuk menyalakan/mematikan detik (bisa memilih **ketuk dua kali**); **tekan lama** '
             'dipakai untuk jendela pratinjau. Di dalam jendela tugas muncul pesan bahwa tugas yang mengatur.'),
            ('Warna per angka / gradien',
             'Enam angka **masing-masing satu warna** (10 preset + slider HSV + warna sistem) dan dua titik '
             'ditengahkan secara optis. Atau **gradien** dari warna awal ke akhir (6 set, kedua ujung bisa '
             'diubah, digelapkan di latar terang). Kartu pratinjau **menggambar sendiri** sehingga cocok dengan '
             'bilah status.'),
            ('Jendela tekan lama',
             'Jendela tembus pandang berisi **waktu jaringan**; geser untuk memindah, sudut ◣ untuk ukuran, '
             'posisi dan ukuran **diingat**; mendukung gradien dan angka sama lebar.'),
            ('Tugas sendiri',
             'Jendela **tiap jam** atau **tiap hari**: nyalakan detik N menit sebelum selama M menit lalu '
             'kembalikan. Perubahan waktu atau zona langsung dihitung ulang, plus pemeriksaan tiap 60 detik.'),
            ('Bandingkan waktu jaringan',
             'Menampilkan selisih dengan **waktu jaringan**; tombol **Sinkronkan** menyalakan penyetelan '
             'otomatis sistem.'),
            ('Tanpa adb',
             'Penulisan lewat `Settings.Secure`; tanpa `WRITE_SECURE_SETTINGS` dikirim broadcast dan SystemUI '
             'yang menulis — **tidak ada fitur yang butuh adb**.'),
            ('Ubin cepat',
             'Tambahkan ubin “Tampilkan detik” untuk berganti tanpa membuka aplikasi.'),
            ('Nama resource jam',
             'Bila jam tidak terdeteksi, tekan **Deteksi otomatis** atau isi nama resource (mis. `clock`).'),
            ('21 bahasa antarmuka',
             'Mengikuti bahasa sistem: 简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, '
             'ไทย, Deutsch, Español, Français, Українська, Polski, Português (Brasil), فارسی, العربية, '
             'Bahasa Indonesia, Italiano, Ελληνικά, ქართული; sisanya Inggris.'),
            ('Penyimpanan / cakupan',
             'Status disimpan di `Settings.Secure`, dipakai bersama antarproses dan bertahan setelah reboot. '
             '**Diuji pada ColorOS 15 / 16 (Android 15 / 16)**; inti hanya memakai API publik sehingga merek lain '
             'semestinya jalan, tetapi baru diuji di OPPO / OnePlus. Minimal Android 10.'),
        ],
        'h_colors': 'Mode warna',
        'colors_intro': (
            'Dua cara mewarnai, diganti di kartu **Warna angka**: **per angka** atau **gradien** seluruh teks.'
        ),
        'cap_digit': 'Per angka: tiap angka satu warna, dua titik ditengahkan optis',
        'cap_grad': 'Gradien: dari warna awal ke akhir, 6 set atau ujung sendiri',
        'h_install': 'Pemasangan',
        'install': [
            'Unduh APK dari halaman [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest)',
            'Aktifkan modul di **LSPosed** dan pilih cakupan **Antarmuka sistem** (`com.android.systemui`)',
            'Mulai ulang proses SystemUI atau perangkat',
            'Layar pengaturan dibuka dari **pengelola LSPosed** (tidak membuat ikon di launcher)',
        ],
        'note1': (
            'Butuh root (LSPosed). **Izin adb biasanya tidak perlu**: penulisan lewat `Settings.Secure` dan, tanpa '
            '`WRITE_SECURE_SETTINGS`, lewat broadcast yang dikerjakan SystemUI. Bila ingin layar pengaturan '
            'menulis **langsung**:'
        ),
        'note2': 'Tanpa memberi izin semuanya tetap jalan: ketukan, tugas, dan jendela berjalan di dalam SystemUI.',
        'h_usage': 'Pemakaian',
        'usage': [
            '**Ketuk jam**: detik nyala/mati (bawaan); di pengaturan bisa dipilih **ketuk dua kali**.',
            '**Tekan lama**: jendela tembus pandang berisi **waktu jaringan**, geser untuk memindah; ketuk jam '
            'untuk memunculkan **tutup** (kanan atas) dan **ukuran** (kiri bawah).',
            'Kartu **Warna angka** punya dua halaman dan tab itu sendiri berfungsi sebagai sakelar.',
            'Kartu **Tugas sendiri** menyusun jendela seperti “detik 2 menit sebelum tiap jam”.',
            'Pergantian memakai pemeriksaan ketukan global dan **tidak bentrok dengan menarik panel**.',
        ],
        'h_demo': 'Pratinjau',
        'cap_off': 'Detik mati: `15:06`, puluhan jam tetap berwarna',
        'cap_on': 'Detik nyala: `15:06:03`, menit dan detik memakai warna sistem',
        'h_how': 'Cara kerja',
        'how_intro': [
            'Modul hanya meng-hook **SystemUI** dan memakai API legacy Xposed yang stabil.',
            '**Inti hanya memakai API publik AOSP, kelas khusus vendor opsional dan dilewati bila tidak ada**, '
            'jadi tidak merusak sistem dan tidak memicu mode aman LSPosed:',
        ],
        'how': [
            '**Detik**: `TextView.setText` (after) menyimpan teks sistem, lalu `TextView.onDraw` (before) menulis '
            '`asli:ss`, diperbarui tiap detik lewat `Handler`.',
            '**Warna per angka**: `ForegroundColorSpan` per karakter (span asing dihapus); dua titik dinaikkan '
            '0.11em karena tata letak bersegmen banyak menaruh glif lebih rendah.',
            '**Gradien**: `CharacterStyle` memanggil `setShader` di `updateDrawState`; pratinjau **menggambar '
            'sendiri** (`GradientTimeView`) karena ColorOS 15 mengkuantisasi shader per segmen teks.',
            '**Pergantian**: panel menelan ketukan di bilah status, jadi modul meng-hook '
            '`View.dispatchTouchEvent` global dan hanya berganti bila ketukan berada di dalam jam tanpa geseran; '
            'event **tidak dikonsumsi**.',
            '**Tugas**: memeriksa “apakah waktu lokal di dalam jendela”, timer tepat di batas plus pemeriksaan '
            'tiap 60 detik.',
        ],
        'h_power': 'Konsumsi daya',
        'power': [
            '**Detik nyala**: timer tiap detik (biaya tak terhindar). Ukur SystemUI per inti: 7.0% mati, '
            '7.25% nyala (60 detik).',
            '**Detik mati**: timer **langsung berhenti** (`stopTicker`), tidak ada bangun tiap detik.',
            '**Perlindungan warna** melambat 2s → 4s → … → 30s dan berhenti setelah 5 menit tanpa kehilangan.',
            'Status **di-cache di proses**; tugas memakai `Handler.postDelayed`: **tanpa WakeLock dan '
            'AlarmManager**, tidak membangunkan tidur lelap.',
        ],
        'h_build': 'Membangun',
        'build_intro': 'Butuh **JDK 17+** (17 atau 21) dan **Android SDK Platform 35** (AGP mengunduh Build-Tools 35).',
        'h_backup': 'Cadangan dan kelanjutan pengembangan (penting)',
        'backup_intro': 'Di folder `ClockSeconds/` simpan **semua kecuali `build/` dan `.gradle/`** (kode sekitar 300 KB):',
        'th_backup_cat': 'Kategori',
        'th_backup_files': 'Berkas',
        'th_backup_note': 'Catatan',
        'backup_rows': [
            ('Berkas build', '`settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, '
             '`gradle.properties`, `keystore.properties`', 'Versi, dependensi, parameter, tanda tangan'),
            ('Gradle Wrapper', '`gradlew`, `gradlew.bat`, `gradle/wrapper/*`', 'Mengunci Gradle 8.14.5'),
            ('Kode', '`app/src/main/java/**` (20 `.java`)', 'Logika modul, pengaturan, kelas gambar'),
            ('Resource', '`app/src/main/res/**`', 'Teks (22 bahasa), tema'),
            ('Metadata Xposed', '`app/src/main/resources/META-INF/xposed/*`', 'Kelas masuk, cakupan, level API'),
            ('Manifest', '`app/src/main/AndroidManifest.xml`', 'Izin, aktivitas'),
            ('Alat', '`tools/**`', 'Pembuat README, pemeriksa i18n, skrip rilis'),
            ('Dokumen', '`README*.md`, `docs/**`, `CHANGELOG.md`', 'Penjelasan, tangkapan layar, riwayat perubahan'),
            ('CI', '`.github/workflows/*.yml`', 'Build bertanda tangan saat tag'),
        ],
        'h_trouble': 'Pemecahan masalah',
        'trouble_summary': 'Lihat langkah',
        'trouble_intro': 'Periksa logcat:',
        'trouble': [
            ('b', '**Build release** menyembunyikan semua log diagnostik di balik `BuildConfig.DEBUG`.'),
            ('b', 'Untuk menelusuri, pasang **build debug** (`assembleDebug`, kunci sama) lalu saring:'),
            ('code', ['adb logcat -s ColorOSClockSeconds']),
            ('b', 'Detik tidak muncul: periksa cakupan “Antarmuka sistem” di LSPosed lalu mulai ulang; kemudian baca '
                  'atau tulis sakelarnya:'),
            ('code', [
                'adb shell settings get secure coloros_clock_seconds_enabled   # 1=on 0=off',
                'adb shell settings put  secure coloros_clock_seconds_enabled 1',
            ]),
            ('b', 'Warna atau detik kembali sendiri: sistem atau tema menyusun ulang teks dan modul menggambar '
                  'lagi; bila tak pulih, cari `color-guard stopped` / `span-theft` di log debug.'),
        ],
        'h_feedback': 'Masukan',
        'feedback': 'Pertanyaan dan saran lewat [GitHub Issues](https://github.com/kelebao/ClockSeconds/issues).',
        'h_license': 'License',
    },
}
