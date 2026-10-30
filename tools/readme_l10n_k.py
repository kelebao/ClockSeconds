# -*- coding: utf-8 -*-
"""Slot translations for README generation: Español (es)."""

TEXTS = {
    'es': {
        'title': 'Segundos en la barra de estado (Show Seconds in Time)',
        'tagline': (
            'Muestra los **segundos** en el reloj de la barra de estado, se alterna con un toque. Puedes dar '
            '**un color a cada dígito** o aplicar un **degradado** a toda la cadena.<br>'
            '**Mantén pulsado** para abrir una ventana translúcida con la **hora de red** (arrastrar, tamaño, '
            'degradado); las **tareas propias** activan los segundos a la hora indicada.<br>'
            'El camino principal solo usa API públicas de AOSP (las clases privadas de OPPO / OnePlus / realme '
            'son opcionales y se omiten si no existen). Probado en ColorOS 15 / 16.'
        ),
        'h_features': 'Funciones',
        'th_feature': 'Función',
        'th_desc': 'Detalle',
        'rows': [
            ('Segundos en la barra',
             '`HH:mm:ss` / `h:mm:ss a`, respeta el formato de 12/24 h y la zona horaria. Al final se añade un '
             '**espacio de ancho cero** para que los temas no borren los colores; si se pierden, se redibuja solo '
             '(pausa de 5 minutos).'),
            ('Alternar con un toque',
             '**Tocar** la hora activa o desactiva los segundos (se puede elegir **doble toque**); **mantener '
             'pulsado** queda para la ventana de vista previa. Dentro de una tarea avisa de que la controla la tarea.'),
            ('Color por dígito / degradado',
             'Seis dígitos **cada uno con su color** (10 preajustes + barras HSV + color del sistema) y los dos '
             'puntos centrados ópticamente. O un **degradado** de color inicial a final (6 juegos, extremos '
             'editables, se oscurece sobre fondo claro). La tarjeta de vista previa **se dibuja sola**, así que '
             'coincide píxel a píxel con la barra.'),
            ('Vista previa al mantener pulsado',
             'Ventana translúcida con la **hora de red**; arrastra para mover, esquina ◣ para el tamaño, posición '
             'y tamaño **se recuerdan**; admite degradado y cifras de ancho fijo.'),
            ('Tareas propias',
             'Ventanas **cada hora** o **cada día**: activar los segundos N minutos antes durante M minutos y '
             'luego restaurar. Si cambias la hora o la zona se recalcula al instante, con repaso cada 60 segundos.'),
            ('Comparación con la hora de red',
             'Muestra la desviación frente a la **hora de red**; un toque en **Sincronizar** activa el ajuste '
             'automático del sistema.'),
            ('Sin necesidad de adb',
             'La configuración se escribe en `Settings.Secure`; sin `WRITE_SECURE_SETTINGS` se envía un broadcast '
             'y la SystemUI escribe por ti: **ninguna función requiere adb**.'),
            ('Baldosa rápida',
             'Añade la baldosa «Mostrar segundos» para alternar sin abrir la aplicación.'),
            ('Nombre del recurso del reloj',
             'Si no se detecta, pulsa **Detección automática** o escribe el nombre del recurso (por ejemplo `clock`).'),
            ('21 idiomas de interfaz',
             'Sigue el idioma del sistema: 简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, '
             'ไทย, Deutsch, Español, Français, Українська, Polski, Português (Brasil), فارسی, العربية, '
             'Bahasa Indonesia, Italiano, Ελληνικά, ქართული; el resto usa inglés.'),
            ('Almacenamiento / alcance',
             'El estado se guarda en `Settings.Secure`, se comparte entre procesos y persiste tras reiniciar. '
             '**Probado en ColorOS 15 / 16 (Android 15 / 16)**; el núcleo usa solo API públicas, así que otros '
             'fabricantes deberían funcionar, pero solo se ha probado en OPPO / OnePlus. Mínimo Android 10.'),
        ],
        'h_colors': 'Modos de color',
        'colors_intro': (
            'Dos formas de colorear, se cambian en la tarjeta **Colores de dígitos**: **por dígito** '
            'o **degradado** en toda la cadena.'
        ),
        'cap_digit': 'Por dígito: cada cifra con su color, puntos suspensivos centrados ópticamente',
        'cap_grad': 'Degradado: de color inicial a final, 6 juegos o extremos propios',
        'h_install': 'Instalación',
        'install': [
            'Descarga el APK desde [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest)',
            'Activa el módulo en **LSPosed** y marca el ámbito **Interfaz del sistema** (`com.android.systemui`)',
            'Reinicia el proceso de SystemUI o el dispositivo',
            'Los ajustes se abren desde el **gestor de LSPosed** (no se crea icono en el lanzador)',
        ],
        'note1': (
            'Requiere root (LSPosed). **Normalmente no hacen falta permisos adb**: se escribe en '
            '`Settings.Secure` y, sin `WRITE_SECURE_SETTINGS`, mediante broadcast que resuelve la SystemUI. '
            'Para que los ajustes escriban **directamente**, concede:'
        ),
        'note2': 'Sin conceder nada funciona igual: toques, tareas y ventana corren dentro de la SystemUI.',
        'h_usage': 'Uso',
        'usage': [
            '**Toca la hora**: segundos on/off (por defecto); en ajustes puedes poner **doble toque**.',
            '**Mantén pulsado**: ventana translúcida con la **hora de red**, arrastra para mover; al tocar la hora '
            'aparecen **cerrar** (arriba derecha) y **tamaño** (abajo izquierda).',
            'La tarjeta **Colores de dígitos** tiene dos páginas y la pestaña es el interruptor.',
            'La tarjeta **Tareas propias** planifica ventanas como «segundos 2 minutos antes de cada hora».',
            'El cambio usa una comprobación global del toque y **no interfiere con bajar el panel**.',
        ],
        'h_demo': 'Vista previa',
        'cap_off': 'Sin segundos: `15:06`, la decena de hora conserva su color',
        'cap_on': 'Con segundos: `15:06:03`, minutos y segundos con el color del sistema',
        'h_how': 'Cómo funciona',
        'how_intro': [
            'El módulo solo engancha la **SystemUI** y usa la API legacy estable de Xposed.',
            '**El núcleo usa solo API públicas de AOSP; las clases de fabricante son opcionales y se omiten si no '
            'existen**, por lo que no rompe el sistema ni dispara el modo seguro de LSPosed:',
        ],
        'how': [
            '**Segundos**: `TextView.setText` (after) guarda el texto del sistema y `TextView.onDraw` (before) '
            'escribe `original:ss`, refresco cada segundo con `Handler`.',
            '**Color por dígito**: un `ForegroundColorSpan` por carácter (se quitan spans ajenos); los dos puntos '
            'se suben 0.11em porque los diseños con varios tramos dejan el glifo más bajo.',
            '**Degradado**: `CharacterStyle` llama a `setShader` en `updateDrawState`; la vista previa **se dibuja '
            'sola** (`GradientTimeView`) porque ColorOS 15 cuantiza el shader por tramo de texto.',
            '**Alternar**: el panel intercepta los toques en la barra, así que el módulo engancha '
            '`View.dispatchTouchEvent` global y solo cambia si el toque cae dentro del reloj sin arrastrar; el '
            'evento **no se consume**.',
            '**Tareas**: comprobar «¿la hora local cae en la ventana?» con temporizadores exactos en los bordes y '
            'repaso cada 60 segundos.',
        ],
        'h_power': 'Consumo',
        'power': [
            '**Con segundos**: temporizador cada segundo (coste inevitable). Medido en SystemUI por núcleo: '
            '7.0% apagado, 7.25% encendido (60 s).',
            '**Sin segundos**: el temporizador **se detiene** (`stopTicker`), no hay despertares por segundo.',
            '**Protección de color** retrocede 2s → 4s → … → 30s y termina tras 5 minutos sin pérdidas.',
            'El estado se **cachea en el proceso**; las tareas usan `Handler.postDelayed`: **sin WakeLock ni '
            'AlarmManager**, no despiertan el sueño profundo.',
        ],
        'h_build': 'Compilar',
        'build_intro': 'Hace falta **JDK 17+** (17 o 21) y **Android SDK Platform 35** (AGP descarga Build-Tools 35).',
        'h_backup': 'Copia y continuación del desarrollo (importante)',
        'backup_intro': 'En la carpeta `ClockSeconds/` guarda **todo menos `build/` y `.gradle/`** (el código son ~300 KB):',
        'th_backup_cat': 'Categoría',
        'th_backup_files': 'Archivos',
        'th_backup_note': 'Nota',
        'backup_rows': [
            ('Archivos de compilación', '`settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, '
             '`gradle.properties`, `keystore.properties`', 'Versiones, dependencias, parámetros, firma'),
            ('Gradle Wrapper', '`gradlew`, `gradlew.bat`, `gradle/wrapper/*`', 'Fija Gradle 8.14.5'),
            ('Código', '`app/src/main/java/**` (20 `.java`)', 'Lógica del módulo, ajustes, clases de dibujo'),
            ('Recursos', '`app/src/main/res/**`', 'Textos (22 idiomas), temas'),
            ('Metadatos Xposed', '`app/src/main/resources/META-INF/xposed/*`', 'Clase de entrada, ámbito, nivel de API'),
            ('Manifest', '`app/src/main/AndroidManifest.xml`', 'Permisos, actividades'),
            ('Herramientas', '`tools/**`', 'Generador de README, comprobación i18n, script de release'),
            ('Documentos', '`README*.md`, `docs/**`, `CHANGELOG.md`', 'Descripción, capturas, registro de cambios'),
            ('CI', '`.github/workflows/*.yml`', 'Compilación firmada con etiqueta'),
        ],
        'h_trouble': 'Solución de problemas',
        'trouble_summary': 'Ver pasos',
        'trouble_intro': 'Mira logcat:',
        'trouble': [
            ('b', 'Las **compilaciones Release** ocultan todos los registros de diagnóstico tras `BuildConfig.DEBUG`.'),
            ('b', 'Para depurar instala una **compilación debug** (`assembleDebug`, misma clave) y filtra:'),
            ('code', ['adb logcat -s ColorOSClockSeconds']),
            ('b', 'No aparecen segundos: revisa el ámbito «Interfaz del sistema» en LSPosed y reinicia; luego lee o '
                  'escribe el interruptor:'),
            ('code', [
                'adb shell settings get secure coloros_clock_seconds_enabled   # 1=on 0=off',
                'adb shell settings put  secure coloros_clock_seconds_enabled 1',
            ]),
            ('b', 'Los colores o los segundos vuelven solos: el sistema o el tema reconstruye el texto y el módulo '
                  'redibuja; si no se recupera busca `color-guard stopped` / `span-theft` en el log debug.'),
        ],
        'h_feedback': 'Sugerencias',
        'feedback': 'Dudas y propuestas en [GitHub Issues](https://github.com/kelebao/ClockSeconds/issues).',
        'h_license': 'License',
    },
}
