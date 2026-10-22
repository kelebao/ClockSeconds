# -*- coding: utf-8 -*-
"""Slot translations for README generation: Bahasa Indonesia (id)."""

TEXTS = {
    'id': {
        'title': 'Detik pada Waktu（Show Seconds in Time）',
        'tagline': (
            'Tampilkan **detik** pada waktu bilah status, alihkan dengan sekali sentuh, '
            'dan buat **puluhan waktu tetap merah**.<br>'
            'Tanpa kelas privat vendor mana pun. Terverifikasi di perangkat nyata dengan '
            'ColorOS 15; juga mencakup Xiaomi, OnePlus, Honor, vivo, Samsung, dan vendor besar lainnya.'
        ),
        'h_features': 'Fitur',
        'th_feature': 'Fitur',
        'th_desc': 'Keterangan',
        'rows': [
            ('Tampilan detik',
             'Waktu bilah status menampilkan hingga detik (`HH:mm:ss` / `h:mm:ss a`, mengikuti setelan 12/24 jam sistem)'),
            ('Sentuh untuk alih',
             '**Mengetuk** waktu bilah status akan mengubah tampilan detik on/off; gestur **ketuk dua kali** juga tersedia di halaman setelan'),
            ('Warna puluhan waktu',
             'Warna **puluhan waktu** dapat **disesuaikan** (palet 8 warna) atau **dimatikan** '
             'untuk kembali ke warna sistem; waktu satu angka (mis. `9:45`) tidak punya puluhan '
             'sehingga tidak tersentuh. Berfungsi di semua vendor'),
            ('Pratinjau tekan lama',
             '**Menekan lama** waktu bilah status memunculkan **jendela mengambang kaca buram** '
             'yang menampilkan **waktu jaringan** secara real time; bisa digeser, diubah '
             'ukurannya dengan ◣ (kiri bawah), ketuk waktu untuk memunculkan tombol **tutup** '
             '(✕ kanan atas) / **ubah ukuran**; ukuran teks menyesuaikan jendela'),
            ('Waktu jaringan',
             'Halaman setelan membandingkan jam Anda dengan **waktu standar jaringan** secara '
             'real time; jika selisih besar, ketuk **Sinkron** untuk menyalakan pengaturan waktu '
             'otomatis sistem'),
            ('Tanpa adb',
             'Setelan ditulis ke `Settings.Secure`; tanpa izin `WRITE_SECURE_SETTINGS` modul '
             'beralih ke broadcast yang diproses SystemUI — **tidak perlu izin adb**'),
            ('Bahasa antarmuka',
             'Antarmuka aplikasi diterjemahkan ke **21 bahasa** (otomatis mengikuti bahasa sistem): '
             '简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, ไทย, '
             'Deutsch, Español, Français, Українська, Polski, Português (Brasil), '
             'فارسی, العربية, Bahasa Indonesia, Italiano, Ελληνικά, ქართული'),
            ('Persistensi',
             'Status disimpan di `Settings.Secure`, dibagi antar proses, dan bertahan setelah SystemUI dimulai ulang'),
            ('Kompatibilitas',
             'Terverifikasi di ColorOS 15 / 16 / 17 (OPPO / OnePlus / realme); juga langsung berfungsi '
             'di vendor lain yang sudah root seperti Xiaomi / Honor / Redmi / Samsung. Tanpa kelas privat vendor, '
             'sehingga mode aman LSPosed tidak akan terpicu. Minimum Android 10'),            ('Tile cepat',
             'Tambahkan tile "Tampilkan detik" ke panel, alihkan dengan satu ketuk'),
            ('Float ditingkatkan',
             'Posisi & ukuran float **diingat**; menampilkan **tanggal**; **ketuk ganda** waktu juga mengalihkan'),
            ('Sadar hemat baterai',
             'Saat mode hemat aktif, penyegaran per detik dijeda (detik membeku), saat mati langsung dilanjut'),
            ('Deteksi dapat diatur',
             'Jika jam tidak terdeteksi, masukkan nama sumber (mis. clock) di Pengaturan. Kosong = aturan bawaan'),

        ],
        'h_install': 'Instalasi',
        'install': [
            'Unduh APK terbaru dari halaman [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest)',
            'Aktifkan modul di **LSPosed**, cakupan: **Antarmuka Sistem (`com.android.systemui`)**',
            'Mulai ulang proses SystemUI atau mulai ulang perangkat',
            'Buka halaman setelan dari **manajer LSPosed** (tidak ada ikon yang dibuat di peluncur)',
        ],
        'note1': (
            'Memerlukan root (LSPosed). Untuk memberikan `WRITE_SECURE_SETTINGS` agar bisa '
            'menulis ke `Settings.Secure`:'
        ),
        'note2': (
            'Tanpa izin itu Anda tetap bisa mengalihkan dengan mengetuk waktu bilah status '
            '(aksi itu berjalan di dalam proses SystemUI yang memang sudah punya izin).'
        ),
        'h_usage': 'Cara pakai',
        'usage': [
            '**Ketuk waktu bilah status**: mengalihkan tampilan detik on/off (gestur bawaan).',
            'Pengalihan diimplementasikan dengan uji sentuh global, jadi **tidak pernah bertabrakan '
            'dengan "menarik panel notifikasi ke bawah"**—— ketukan murni mengalihkan, tarikan tetap '
            'menarik panel ke bawah. Bila lebih suka, Anda bisa mengganti gestur menjadi '
            '**ketuk dua kali** di halaman setelan.',
            'Halaman setelan menampilkan status saat ini, gestur yang dipilih, dan tombol segarkan.',
        ],
        'h_demo': 'Pratinjau',
        'cap_off': 'Detik mati: `15:06` — puluhan waktu `1` tetap merah',
        'cap_on': 'Detik hidup: `15:06:03` — menit dan detik mempertahankan warna sistem',
        'h_how': 'Cara kerja',
        'how_intro': [
            'Modul hanya meng-hook **SystemUI**, dengan pendekatan legacy Xposed yang sudah teruji,',
            'dan **sama sekali tidak menyentuh kelas privat vendor mana pun**, sehingga tidak '
            'merusak status internal sistem dan tidak memicu mode aman LSPosed:',
        ],
        'how': [
            '**Detik**: hook `TextView.setText` (after) yang stabil untuk menyimpan mentahan teks '
            'yang sistem berikan pada waktu bilah status,\n'
            'lalu hook `TextView.onDraw` (before) untuk menimpa tampilan dengan `mentahanTeks:ss` '
            'setelah sistem selesai mengatur teks,\n'
            'didorong `Handler` 1 detik. Detik yang ditambahkan mewarisi warna teks waktu itu sendiri '
            '(terukur `#ccffffff`).\n'
            'Selain itu "puluhan waktu" diwarnai merah (sebuah `ForegroundColorSpan` merah pada satu '
            'karakter itu), baik tampilan detik dinyalakan maupun dimatikan;\n'
            'menit dan detik tidak pernah merah; waktu satu angka tidak punya puluhan sehingga tidak tersentuh.',
            '**Alihkan**: panel notifikasi ColorOS mencegat sentuh di wilayah bilah status, sehingga '
            '`OnTouchListener` waktu itu sendiri tidak pernah menerima peristiwa.\n'
            'Modul karena itu meng-hook `View.dispatchTouchEvent` global dan hanya membalik sakelar '
            '`Settings.Secure` ketika titik tekan jatuh di dalam\n'
            'batas layar waktu yang terdeteksi dan hampir tidak bergerak (ketuk) atau ketukan kedua dalam 350 ms '
            '(ketuk dua kali). Modul **tidak mengonsumsi**\n'
            'peristiwa sentuh apa pun, sehingga menarik panel notifikasi ke bawah bekerja seperti biasa.',
        ],
        'h_power': 'Catatan hemat daya',
        'power': [
            '**Detik NYALA**: modul memerlukan pewaktu 1 detik untuk mendorong penyegaran '
            '(ini adalah biaya melekat dari "menampilkan detik", sekitar 60 penggambaran ulang waktu per menit).',
            '**Detik MATI**: modul **segera menghentikan pewaktu itu** (`stopTicker`)—— '
            'sama sekali tidak ada kebangunan tiap detik dan tidak ada pembacaan antar proses.\n'
            'Ia kembali ke ritme asli sistem "menyegarkan sekali per menit", jadi mematikan tampilan '
            'detik memang lebih hemat daya.',
            'Status disimpan di **cache dalam proses** (`sSecondsEnabled`), sehingga pewaktu tidak '
            'membaca `Settings.Secure` setiap detik (menghindari IPC tiap detik).',
            'Tidak ada pemformat waktu yang dibuat: modul memakai ulang mentahan teks sistem dan '
            'hanya menambahkan `:ss`, jadi tidak ada overhead pemformatan.',
            'Pewaktu hanya menyimpan **referensi lemah** ke view dan hanya berlanjut selama view '
            '`isAttachedToWindow()`; setelah view dihancurkan ia keluar otomatis.',
        ],
        'h_build': 'Build',
        'build_intro': (
            'Membutuhkan **JDK 17+** (17 atau 21 direkomendasikan) dan **Android SDK Platform 35** '
            '(Build-Tools 35 dipasang otomatis oleh AGP).'
        ),
        'h_backup': 'Pencadangan & pengembangan lanjutan (penting)',
        'backup_intro': (
            'Di direktori `ClockSeconds/`, **semua berkas kecuali `build/` dan '
            '`.gradle/` harus dipertahankan** (bagian kode hanya sekitar 140 KB):'
        ),
        'th_backup_cat': 'Kategori',
        'th_backup_files': 'Berkas',
        'th_backup_note': 'Keterangan',
        'backup_rows': [
            ('Skrip build',
             '`settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties`',
             'Versi, dependensi, parameter kompilator'),
            ('Gradle Wrapper', '`gradlew`, `gradlew.bat`, `gradle/wrapper/*`', 'Mengunci Gradle 8.14.5'),
            ('Kode sumber', '`app/src/main/java/**` (3 berkas `.java`)', 'Inti logika modul'),
            ('Sumber daya', '`app/src/main/res/**`',
             'String (Mandarin sebagai bawaan + 21 bahasa values-xx), tema'),
            ('Metadata Xposed', '`app/src/main/resources/META-INF/xposed/*`',
             'Kelas masuk, cakupan, versi API'),
            ('Manifest', '`app/src/main/AndroidManifest.xml`', 'Izin, Activity'),
            ('Dokumentasi', '`README*.md`', 'Dokumentasi 21 bahasa'),
            ('CI', '`.github/workflows/release.yml`', 'Tag menghasilkan build tertandatangani'),
        ],
        'h_trouble': 'Pemecahan masalah',
        'trouble_summary': 'Buka langkah pemecahan masalah',
        'trouble_intro': 'Lihat logcat:',
        'trouble': [
            ('b', '**Build Release** telah membuang semua log diagnostik melalui gerbang '
                  '`BuildConfig.DEBUG` (lebih kecil, lebih bersih); tidak ada apa pun dari modul '
                  'ini di logcat.'),
            ('b', 'Untuk memeriksa, pasang sementara **build debug** (`assembleDebug`, '
                  'ditandatangani dengan kunci produksi yang sama jadi bisa langsung menimpa '
                  'instalan Release), lalu saring dengan TAG:'),
            ('code', ['adb logcat -s ColorOSClockSeconds']),
            ('b', 'Bila detik tidak muncul sama sekali: pastikan dulu LSPosed sudah mencentang '
                  'cakupan SystemUI dan sudah dimulai ulang; lalu periksa apakah sakelar '
                  'tersimpan dalam keadaan mati——\n'
                  'bisa dibaca / dipaksa lewat adb:'),
            ('code', [
                'adb shell settings get secure coloros_clock_seconds_enabled   # 1=nyala 0=mati',
                'adb shell settings put  secure coloros_clock_seconds_enabled 1',
            ]),
            ('b', 'Bila tidak berefek di perangkat non-ColorOS: pasang build debug dan lihat baris '
                  '`candidate:` / `CONFIG CLOCK:` di log\n'
                  'untuk memastikan nama sumber / nama kelas waktu bilah status perangkat Anda sudah '
                  'tercakup oleh aturan pengenalan; jika belum, cukup tambah satu baris aturan.'),
        ],
        'h_feedback': 'Umpan Balik',
        'feedback': 'Punya pertanyaan, saran, atau masukan? Silakan tinggalkan pesan di [GitHub Issues](https://github.com/kelebao/ClockSeconds/issues), saya akan membalas secepatnya.',
        'h_license': 'License',
    },
}
