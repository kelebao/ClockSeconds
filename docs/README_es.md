<div align="center">

[简体中文](../README.md) · [English](README_EN.md) · [繁體中文](README_zh-TW.md) · [日本語](README_ja.md) · [한국어](README_ko.md) · [Türkçe](README_tr.md) · [Русский](README_ru.md) · [Tiếng Việt](README_vi.md) · [ไทย](README_th.md) · [Deutsch](README_de.md) · **Español** · [Français](README_fr.md) · [Українська](README_uk.md) · [Polski](README_pl.md) · [Português (Brasil)](README_pt-BR.md) · [فارسی](README_fa.md) · [العربية](README_ar.md) · [Bahasa Indonesia](README_id.md) · [Italiano](README_it.md) · [Ελληνικά](README_el.md) · [ქართული](README_ka.md)

</div>

<div align="center">

# Segundos en la hora（Show Seconds in Time）

Muestra los **segundos** en la hora de la barra de estado, alterna con un solo toque y mantén la **decena de la hora en rojo**.<br>Sin clases privadas de fabricante. Verificado en dispositivo real con ColorOS 15; también cubre Xiaomi, OnePlus, Honor, vivo y otros fabricantes importantes.

[![version](https://img.shields.io/badge/version-1.0.4-blue.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/releases/latest)
![android](https://img.shields.io/badge/Android-10%2B-brightgreen.svg?style=flat-square)
[![lsposed](https://img.shields.io/badge/LSPosed-module-orange.svg?style=flat-square)](https://github.com/Xposed-Modules-Repo/io.github.kelebao.colorosclockseconds/releases)
![coloros](https://img.shields.io/badge/ColorOS%2015-verified-success.svg?style=flat-square)
[![stars](https://img.shields.io/github/stars/kelebao/ClockSeconds.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/stargazers)
[![license](https://img.shields.io/badge/license-MIT-lightgrey.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/blob/main/LICENSE)

</div>

---

## Funciones

| Función | Descripción |
|------|------|
| Segundos | El hora de la barra de estado muestra los segundos (`HH:mm:ss` / `h:mm:ss a`, sigue el ajuste de 12/24 horas del sistema) |
| Tocar para alternar | **Tocar** la hora de la barra de estado alterna la visualización de segundos; también se puede cambiar a **doble toque** en la página de ajustes |
| Decena de la hora en rojo | La **decena de la hora** se tiñe de rojo (el `1` del `13`, el `0` del `05`); los minutos y segundos mantienen el color del sistema. Las horas de un solo dígito (p. ej. `9:45`) no tienen decena y quedan intactas. Efectivo en todos los fabricantes |
| Idiomas de la interfaz | La interfaz de la app está traducida a **21 idiomas** (sigue automáticamente el idioma del sistema): 简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, ไทย, Deutsch, Español, Français, Українська, Polski, Português (Brasil), فارسی, العربية, Bahasa Indonesia, Italiano, Ελληνικά, ქართული |
| Persistencia | El estado es un segundo toque guarda en `Settings.Secure`, se comparte entre procesos y sobrevive a los reinicios de SystemUI |
| Compatibilidad | Verificado en ColorOS 15 / 16 / 17 (OPPO / OnePlus / realme); también funciona tal cual en otros fabricantes con root como Xiaomi / Honor / Redmi. Sin clases privadas de fabricante, así que no dispara el modo seguro de LSPosed. Mínimo Android 10 |

## Instalación

1. Descarga el APK más reciente de la página [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest)
2. Activa el módulo en **LSPosed**, ámbito: **Interfaz del sistema (`com.android.systemui`)**
3. Reinicia el proceso SystemUI o reinicia el dispositivo
4. Abre la página de configuración desde el **administrador de LSPosed** (no es un segundo toque crea icono en el lanzador)

> Requiere root (LSPosed). Para conceder `WRITE_SECURE_SETTINGS` y poder escribir en `Settings.Secure`:
> ```bash
> adb shell pm grant io.github.kelebao.colorosclockseconds android.permission.WRITE_SECURE_SETTINGS
> ```
> Sin ella todavía puedes alternar tocando la hora de la barra de estado (esa acción se ejecuta dentro del proceso SystemUI, que ya tiene el permiso de fábrica).

## Uso

- **Toca la hora de la barra de estado**: alterna la visualización de segundos (gesto predeterminado).
- El alternado es un segundo toque implementa con una prueba de acierto de toque global, así que **nunca entra en conflicto con "deslizar hacia abajo el panel de notificaciones"**—— un toque puro alterna, un deslizamiento sigue bajando el panel. Si lo prefieres, puedes cambiar el gesto a **doble toque** en la página de configuración.
- La página de configuración muestra el estado actual, el gesto elegido y un botón de actualización.

## Vista previa

<table><tr>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-off.jpg" alt="Segundos desactivados: `15:06` — la decena de la hora `1` sigue en rojo" width="100%">
<br>
Segundos desactivados: `15:06` — la decena de la hora `1` sigue en rojo
</td>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-on.jpg" alt="Segundos activados: `15:06:03` — minutos y segundos mantienen el color del sistema" width="100%">
<br>
Segundos activados: `15:06:03` — minutos y segundos mantienen el color del sistema
</td>
</tr></table>

## Cómo funciona

El módulo solo engancha **SystemUI**, con el enfoque legacy Xposed bien probado,
y **nunca toca ninguna clase privada de fabricante**, por lo que no rompe el estado interno del sistema ni dispara el modo seguro de LSPosed:

- **Segundos**: engancha el estable `TextView.setText` (after) para cachear el texto en crudo que el sistema pone en la hora de la barra de estado,
  luego engancha `TextView.onDraw` (before) para sobrescribir la visualización con `textoCrudo:ss` después de que el sistema lo haya fijado,
  impulsado por un `Handler` de 1 segundo. Los segundos añadidos heredan el color de texto propio de la hora (medido `#ccffffff`).
  Además, la decena de la hora se tiñe de rojo (un `ForegroundColorSpan` rojo sobre ese único carácter), tanto si los segundos se muestran como si no;
  los minutos y los segundos nunca se tiñen; las horas de un solo dígito no tienen decena y quedan intactas.
- **Alternar**: El panel de notificaciones de ColorOS intercepta los toques en la zona de la barra de estado, así que el `OnTouchListener` propio de la hora nunca recibe eventos.
  Por eso el módulo engancha el `View.dispatchTouchEvent` global y solo gira el interruptor `Settings.Secure` cuando la pulsación cae dentro
  de los límites en pantalla de la hora reconocido y apenas se mueve (toque) o es un segundo toque dentro de 350 ms (doble toque). El módulo **no consume**
  ningún evento de toque, de modo que deslizar hacia abajo el panel de notificaciones funciona como siempre.

## Nota sobre ahorro de energía

- **Segundos ACTIVADOS**: el módulo necesita un temporizador de 1 segundo para las actualizaciones (el coste inherente de mostrar segundos, unas 60 repintados del hora por minuto).
- **Segundos DESACTIVADOS**: el módulo **detiene ese temporizador de inmediato** (`stopTicker`)—— sin ningún despertar por segundo y sin lecturas entre procesos.
  Vuelve al ritmo original del sistema de "actualizar una vez por minuto", así que desactivar los segundos ahorra energía de verdad.
- El estado es un segundo toque almacena en **caché dentro del proceso** (`sSecondsEnabled`), así que el temporizador no lee `Settings.Secure` cada segundo (se evita un IPC por segundo).
- No es un segundo toque construye ningún formateador de tiempo: el módulo reutiliza el texto en crudo del sistema y solo añade `:ss`, así que no hay sobrecoste de formateo.
- El temporizador solo guarda una **referencia débil** a la vista y solo continúa mientras la vista esté `isAttachedToWindow()`; cuando la vista se destruye, sale automáticamente.

## Compilación

Requiere **JDK 17+** (se recomienda 17 o 21) y **Android SDK Platform 35** (Build-Tools 35 lo instala AGP automáticamente).

```bash
export JAVA_HOME=/path/to/jdk-17-or-21
./gradlew assembleDebug
```

## Copia de seguridad y desarrollo continuado (importante)

En el directorio `ClockSeconds/`, **hay que conservar todos los archivos salvo `build/` y `.gradle/`** (la parte de código solo pesa unos 140 KB):

| Categoría | Archivo | Nota |
|------|------|------|
| Scripts de compilación | `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties` | Versión, dependencias, parámetros del compilador |
| Gradle Wrapper | `gradlew`, `gradlew.bat`, `gradle/wrapper/*` | Fija Gradle 8.14.5 |
| Código fuente | `app/src/main/java/**` (3 archivos `.java`) | El cuerpo de la lógica del módulo |
| Recursos | `app/src/main/res/**` | Cadenas (chino por defecto + 21 idiomas values-xx), tema |
| Metadatos de Xposed | `app/src/main/resources/META-INF/xposed/*` | Clase de entrada, ámbito, versión de API |
| Manifiesto | `app/src/main/AndroidManifest.xml` | Permisos, Activity |
| Documentación | `README*.md` | Documentación en 21 idiomas |
| CI | `.github/workflows/release.yml` | Etiqueta produce compilación firmada |

## Solución de problemas

<details>
<summary>Desplegar los pasos de solución de problemas</summary>

Consulta el logcat:

- Las **compilaciones Release** eliminan todos los registros de diagnóstico mediante la puerta `BuildConfig.DEBUG` (más pequeño y limpio); en logcat no aparece nada de este módulo.
- Para investigar, instala temporalmente la **compilación debug** (`assembleDebug`, firmada con la misma clave de producción, así que puede instalarse encima de la Release directamente) y filtra por TAG:

  ```bash
  adb logcat -s ColorOSClockSeconds
  ```

- Si no aparecen segundos en absoluto: confirma primero que LSPosed tiene marcado el ámbito SystemUI y se ha reiniciado; luego comprueba si el interruptor quedó persistido como desactivado——
  se puede leer / forzar por adb:

  ```bash
  adb shell settings get secure coloros_clock_seconds_enabled   # 1=activado 0=desactivado
  adb shell settings put  secure coloros_clock_seconds_enabled 1
  ```

- Si no surte efecto en un dispositivo que no sea ColorOS: instala la compilación debug y mira las líneas `candidate:` / `CONFIG CLOCK:` del registro
  para ver si el nombre de recurso / nombre de clase de la hora de la barra de estado de tu dispositivo está cubierto por las reglas de reconocimiento; si no lo está, basta con añadir una línea de regla.

</details>

## Comentarios

¿Tienes preguntas, sugerencias o comentarios? Deja un mensaje en [GitHub Issues](https://github.com/kelebao/ClockSeconds/issues) y te responderé lo antes posible.

## License

[MIT](LICENSE) © kelebao
