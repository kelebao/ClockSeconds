# -*- coding: utf-8 -*-
"""Slot translations for README generation: Español (es)."""

TEXTS = {
    'es': {
        'title': 'Segundos en la hora（Show Seconds in Time）',
        'tagline': (
            'Muestra los **segundos** en la hora de la barra de estado, alterna con un solo toque '
            'y mantén la **decena de la hora en rojo**.<br>'
            'Sin clases privadas de fabricante. Verificado en dispositivo real con ColorOS 15; '
            'también cubre Xiaomi, OnePlus, Honor, vivo, Samsung y otros fabricantes importantes.'
        ),
        'h_features': 'Funciones',
        'th_feature': 'Función',
        'th_desc': 'Descripción',
        'rows': [
            ('Segundos',
             'El hora de la barra de estado muestra los segundos (`HH:mm:ss` / `h:mm:ss a`, sigue el ajuste de 12/24 horas del sistema)'),
            ('Tocar para alternar',
             '**Tocar** la hora de la barra de estado alterna la visualización de segundos; también se puede cambiar a **doble toque** en la página de ajustes'),
            ('Color de la decena',
             'El color de la **decena de la hora** es **personalizable** (paleta de 8 colores) o '
             '**desactivable** para volver al color del sistema; las horas de un solo dígito '
             '(p. ej. `9:45`) no tienen decena y quedan intactas. Efectivo en todos los fabricantes'),
            ('Vista al mantener pulsado',
             '**Mantener pulsada** la hora de la barra de estado abre una **ventana flotante de '
             'cristal esmerilado** con la **hora de red** en tiempo real; arrastrable, '
             'redimensionable con ◣ (abajo a la izquierda), toca la hora para ver **cerrar** '
             '(✕ arriba a la derecha) / **redimensionar**; el tamaño del texto se adapta a la ventana'),
            ('Hora de red',
             'La página de ajustes compara tu reloj con la **hora estándar de red** en tiempo real; '
             'si la desviación es grande, toca **Sincronizar** para activar la hora automática del sistema'),
            ('Sin adb',
             'Los ajustes se escriben en `Settings.Secure`; sin el permiso `WRITE_SECURE_SETTINGS` '
             'el módulo usa automáticamente un broadcast que procesa SystemUI — '
             '**no se necesita adb**'),
            ('Idiomas de la interfaz',
             'La interfaz de la app está traducida a **21 idiomas** (sigue automáticamente el idioma del sistema): '
             '简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, ไทย, '
             'Deutsch, Español, Français, Українська, Polski, Português (Brasil), '
             'فارسی, العربية, Bahasa Indonesia, Italiano, Ελληνικά, ქართული'),
            ('Persistencia',
             'El estado es un segundo toque guarda en `Settings.Secure`, se comparte entre procesos y sobrevive a los reinicios de SystemUI'),
            ('Compatibilidad',
             'Verificado en ColorOS 15 / 16 / 17 (OPPO / OnePlus / realme); también funciona tal cual '
             'en otros fabricantes con root como Xiaomi / Honor / Redmi / Samsung. Sin clases privadas de '
             'fabricante, así que no dispara el modo seguro de LSPosed. Mínimo Android 10'),
        ],
        'h_install': 'Instalación',
        'install': [
            'Descarga el APK más reciente de la página [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest)',
            'Activa el módulo en **LSPosed**, ámbito: **Interfaz del sistema (`com.android.systemui`)**',
            'Reinicia el proceso SystemUI o reinicia el dispositivo',
            'Abre la página de configuración desde el **administrador de LSPosed** (no es un segundo toque crea icono en el lanzador)',
        ],
        'note1': (
            'Requiere root (LSPosed). Para conceder `WRITE_SECURE_SETTINGS` y poder escribir '
            'en `Settings.Secure`:'
        ),
        'note2': (
            'Sin ella todavía puedes alternar tocando la hora de la barra de estado '
            '(esa acción se ejecuta dentro del proceso SystemUI, que ya tiene el permiso de fábrica).'
        ),
        'h_usage': 'Uso',
        'usage': [
            '**Toca la hora de la barra de estado**: alterna la visualización de segundos (gesto predeterminado).',
            'El alternado es un segundo toque implementa con una prueba de acierto de toque global, así que **nunca '
            'entra en conflicto con "deslizar hacia abajo el panel de notificaciones"**—— un toque puro '
            'alterna, un deslizamiento sigue bajando el panel. Si lo prefieres, puedes cambiar el '
            'gesto a **doble toque** en la página de configuración.',
            'La página de configuración muestra el estado actual, el gesto elegido y un botón de actualización.',
        ],
        'h_demo': 'Vista previa',
        'cap_off': 'Segundos desactivados: `15:06` — la decena de la hora `1` sigue en rojo',
        'cap_on': 'Segundos activados: `15:06:03` — minutos y segundos mantienen el color del sistema',
        'h_how': 'Cómo funciona',
        'how_intro': [
            'El módulo solo engancha **SystemUI**, con el enfoque legacy Xposed bien probado,',
            'y **nunca toca ninguna clase privada de fabricante**, por lo que no rompe el estado '
            'interno del sistema ni dispara el modo seguro de LSPosed:',
        ],
        'how': [
            '**Segundos**: engancha el estable `TextView.setText` (after) para cachear el texto '
            'en crudo que el sistema pone en la hora de la barra de estado,\n'
            'luego engancha `TextView.onDraw` (before) para sobrescribir la visualización con '
            '`textoCrudo:ss` después de que el sistema lo haya fijado,\n'
            'impulsado por un `Handler` de 1 segundo. Los segundos añadidos heredan el color de '
            'texto propio de la hora (medido `#ccffffff`).\n'
            'Además, la decena de la hora se tiñe de rojo (un `ForegroundColorSpan` rojo sobre ese '
            'único carácter), tanto si los segundos se muestran como si no;\n'
            'los minutos y los segundos nunca se tiñen; las horas de un solo dígito no tienen decena '
            'y quedan intactas.',
            '**Alternar**: El panel de notificaciones de ColorOS intercepta los toques en la zona '
            'de la barra de estado, así que el `OnTouchListener` propio de la hora nunca recibe eventos.\n'
            'Por eso el módulo engancha el `View.dispatchTouchEvent` global y solo gira el '
            'interruptor `Settings.Secure` cuando la pulsación cae dentro\n'
            'de los límites en pantalla de la hora reconocido y apenas se mueve (toque) o es un segundo toque '
            'dentro de 350 ms (doble toque). El módulo **no consume**\n'
            'ningún evento de toque, de modo que deslizar hacia abajo el panel de notificaciones '
            'funciona como siempre.',
        ],
        'h_power': 'Nota sobre ahorro de energía',
        'power': [
            '**Segundos ACTIVADOS**: el módulo necesita un temporizador de 1 segundo para las '
            'actualizaciones (el coste inherente de mostrar segundos, unas 60 repintados del '
            'hora por minuto).',
            '**Segundos DESACTIVADOS**: el módulo **detiene ese temporizador de inmediato** '
            '(`stopTicker`)—— sin ningún despertar por segundo y sin lecturas entre procesos.\n'
            'Vuelve al ritmo original del sistema de "actualizar una vez por minuto", así que '
            'desactivar los segundos ahorra energía de verdad.',
            'El estado es un segundo toque almacena en **caché dentro del proceso** (`sSecondsEnabled`), así que el '
            'temporizador no lee `Settings.Secure` cada segundo (se evita un IPC por segundo).',
            'No es un segundo toque construye ningún formateador de tiempo: el módulo reutiliza el texto en crudo '
            'del sistema y solo añade `:ss`, así que no hay sobrecoste de formateo.',
            'El temporizador solo guarda una **referencia débil** a la vista y solo continúa '
            'mientras la vista esté `isAttachedToWindow()`; cuando la vista se destruye, sale automáticamente.',
        ],
        'h_build': 'Compilación',
        'build_intro': (
            'Requiere **JDK 17+** (se recomienda 17 o 21) y **Android SDK Platform 35** '
            '(Build-Tools 35 lo instala AGP automáticamente).'
        ),
        'h_backup': 'Copia de seguridad y desarrollo continuado (importante)',
        'backup_intro': (
            'En el directorio `ClockSeconds/`, **hay que conservar todos los archivos '
            'salvo `build/` y `.gradle/`** (la parte de código solo pesa unos 140 KB):'
        ),
        'th_backup_cat': 'Categoría',
        'th_backup_files': 'Archivo',
        'th_backup_note': 'Nota',
        'backup_rows': [
            ('Scripts de compilación',
             '`settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties`',
             'Versión, dependencias, parámetros del compilador'),
            ('Gradle Wrapper', '`gradlew`, `gradlew.bat`, `gradle/wrapper/*`', 'Fija Gradle 8.14.5'),
            ('Código fuente', '`app/src/main/java/**` (3 archivos `.java`)', 'El cuerpo de la lógica del módulo'),
            ('Recursos', '`app/src/main/res/**`',
             'Cadenas (chino por defecto + 21 idiomas values-xx), tema'),
            ('Metadatos de Xposed', '`app/src/main/resources/META-INF/xposed/*`',
             'Clase de entrada, ámbito, versión de API'),
            ('Manifiesto', '`app/src/main/AndroidManifest.xml`', 'Permisos, Activity'),
            ('Documentación', '`README*.md`', 'Documentación en 21 idiomas'),
            ('CI', '`.github/workflows/release.yml`', 'Etiqueta produce compilación firmada'),
        ],
        'h_trouble': 'Solución de problemas',
        'trouble_summary': 'Desplegar los pasos de solución de problemas',
        'trouble_intro': 'Consulta el logcat:',
        'trouble': [
            ('b', 'Las **compilaciones Release** eliminan todos los registros de diagnóstico '
                  'mediante la puerta `BuildConfig.DEBUG` (más pequeño y limpio); en logcat no '
                  'aparece nada de este módulo.'),
            ('b', 'Para investigar, instala temporalmente la **compilación debug** (`assembleDebug`, '
                  'firmada con la misma clave de producción, así que puede instalarse encima de la '
                  'Release directamente) y filtra por TAG:'),
            ('code', ['adb logcat -s ColorOSClockSeconds']),
            ('b', 'Si no aparecen segundos en absoluto: confirma primero que LSPosed tiene marcado '
                  'el ámbito SystemUI y se ha reiniciado; luego comprueba si el interruptor quedó '
                  'persistido como desactivado——\n'
                  'se puede leer / forzar por adb:'),
            ('code', [
                'adb shell settings get secure coloros_clock_seconds_enabled   # 1=activado 0=desactivado',
                'adb shell settings put  secure coloros_clock_seconds_enabled 1',
            ]),
            ('b', 'Si no surte efecto en un dispositivo que no sea ColorOS: instala la compilación '
                  'debug y mira las líneas `candidate:` / `CONFIG CLOCK:` del registro\n'
                  'para ver si el nombre de recurso / nombre de clase de la hora de la barra de '
                  'estado de tu dispositivo está cubierto por las reglas de reconocimiento; '
                  'si no lo está, basta con añadir una línea de regla.'),
        ],
        'h_feedback': 'Comentarios',
        'feedback': '¿Tienes preguntas, sugerencias o comentarios? Deja un mensaje en [GitHub Issues](https://github.com/kelebao/ClockSeconds/issues) y te responderé lo antes posible.',
        'h_license': 'License',
    },
}
