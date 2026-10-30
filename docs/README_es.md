<div align="center">

[简体中文](../README.md) · [English](README_EN.md) · [繁體中文](README_zh-TW.md) · [日本語](README_ja.md) · [한국어](README_ko.md) · [Türkçe](README_tr.md) · [Русский](README_ru.md) · [Tiếng Việt](README_vi.md) · [ไทย](README_th.md) · [Deutsch](README_de.md) · **Español** · [Français](README_fr.md) · [Українська](README_uk.md) · [Polski](README_pl.md) · [Português (Brasil)](README_pt-BR.md) · [فارسی](README_fa.md) · [العربية](README_ar.md) · [Bahasa Indonesia](README_id.md) · [Italiano](README_it.md) · [Ελληνικά](README_el.md) · [ქართული](README_ka.md)

</div>

<div align="center">

# Segundos en la barra de estado (Show Seconds in Time)

Muestra los **segundos** en el reloj de la barra de estado, se alterna con un toque. Puedes dar **un color a cada dígito** o aplicar un **degradado** a toda la cadena.<br>**Mantén pulsado** para abrir una ventana translúcida con la **hora de red** (arrastrar, tamaño, degradado); las **tareas propias** activan los segundos a la hora indicada.<br>El camino principal solo usa API públicas de AOSP (las clases privadas de OPPO / OnePlus / realme son opcionales y se omiten si no existen). Probado en ColorOS 15 / 16.

[![version](https://img.shields.io/badge/version-1.2.0-blue.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/releases/latest)
![android](https://img.shields.io/badge/Android-10%2B-brightgreen.svg?style=flat-square)
[![lsposed](https://img.shields.io/badge/LSPosed-module-orange.svg?style=flat-square)](https://github.com/Xposed-Modules-Repo/io.github.kelebao.colorosclockseconds/releases)
![coloros](https://img.shields.io/badge/ColorOS%2015%2F16-verified-success.svg?style=flat-square)
[![stars](https://img.shields.io/github/stars/kelebao/ClockSeconds.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/stargazers)
[![license](https://img.shields.io/badge/license-MIT-lightgrey.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/blob/main/LICENSE)

</div>

---

## Funciones

| Función | Detalle |
|------|------|
| Segundos en la barra | `HH:mm:ss` / `h:mm:ss a`, respeta el formato de 12/24 h y la zona horaria. Al final se añade un **espacio de ancho cero** para que los temas no borren los colores; si se pierden, se redibuja solo (pausa de 5 minutos). |
| Alternar con un toque | **Tocar** la hora activa o desactiva los segundos (se puede elegir **doble toque**); **mantener pulsado** queda para la ventana de vista previa. Dentro de una tarea avisa de que la controla la tarea. |
| Color por dígito / degradado | Seis dígitos **cada uno con su color** (10 preajustes + barras HSV + color del sistema) y los dos puntos centrados ópticamente. O un **degradado** de color inicial a final (6 juegos, extremos editables, se oscurece sobre fondo claro). La tarjeta de vista previa **se dibuja sola**, así que coincide píxel a píxel con la barra. |
| Vista previa al mantener pulsado | Ventana translúcida con la **hora de red**; arrastra para mover, esquina ◣ para el tamaño, posición y tamaño **se recuerdan**; admite degradado y cifras de ancho fijo. |
| Tareas propias | Ventanas **cada hora** o **cada día**: activar los segundos N minutos antes durante M minutos y luego restaurar. Si cambias la hora o la zona se recalcula al instante, con repaso cada 60 segundos. |
| Comparación con la hora de red | Muestra la desviación frente a la **hora de red**; un toque en **Sincronizar** activa el ajuste automático del sistema. |
| Sin necesidad de adb | La configuración se escribe en `Settings.Secure`; sin `WRITE_SECURE_SETTINGS` se envía un broadcast y la SystemUI escribe por ti: **ninguna función requiere adb**. |
| Baldosa rápida | Añade la baldosa «Mostrar segundos» para alternar sin abrir la aplicación. |
| Nombre del recurso del reloj | Si no se detecta, pulsa **Detección automática** o escribe el nombre del recurso (por ejemplo `clock`). |
| 21 idiomas de interfaz | Sigue el idioma del sistema: 简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, ไทย, Deutsch, Español, Français, Українська, Polski, Português (Brasil), فارسی, العربية, Bahasa Indonesia, Italiano, Ελληνικά, ქართული; el resto usa inglés. |
| Almacenamiento / alcance | El estado se guarda en `Settings.Secure`, se comparte entre procesos y persiste tras reiniciar. **Probado en ColorOS 15 / 16 (Android 15 / 16)**; el núcleo usa solo API públicas, así que otros fabricantes deberían funcionar, pero solo se ha probado en OPPO / OnePlus. Mínimo Android 10. |

## Modos de color

Dos formas de colorear, se cambian en la tarjeta **Colores de dígitos**: **por dígito** o **degradado** en toda la cadena.

<table><tr>
<td align="center" width="50%">
<img src="screenshots/colors-digit.png" alt="Por dígito: cada cifra con su color, puntos suspensivos centrados ópticamente" width="100%">
<br>
Por dígito: cada cifra con su color, puntos suspensivos centrados ópticamente
</td>
<td align="center" width="50%">
<img src="screenshots/colors-gradient.png" alt="Degradado: de color inicial a final, 6 juegos o extremos propios" width="100%">
<br>
Degradado: de color inicial a final, 6 juegos o extremos propios
</td>
</tr></table>

## Instalación

1. Descarga el APK desde [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest)
2. Activa el módulo en **LSPosed** y marca el ámbito **Interfaz del sistema** (`com.android.systemui`)
3. Reinicia el proceso de SystemUI o el dispositivo
4. Los ajustes se abren desde el **gestor de LSPosed** (no se crea icono en el lanzador)

> Requiere root (LSPosed). **Normalmente no hacen falta permisos adb**: se escribe en `Settings.Secure` y, sin `WRITE_SECURE_SETTINGS`, mediante broadcast que resuelve la SystemUI. Para que los ajustes escriban **directamente**, concede:
> ```bash
> adb shell pm grant io.github.kelebao.colorosclockseconds android.permission.WRITE_SECURE_SETTINGS
> ```
> Sin conceder nada funciona igual: toques, tareas y ventana corren dentro de la SystemUI.

## Uso

- **Toca la hora**: segundos on/off (por defecto); en ajustes puedes poner **doble toque**.
- **Mantén pulsado**: ventana translúcida con la **hora de red**, arrastra para mover; al tocar la hora aparecen **cerrar** (arriba derecha) y **tamaño** (abajo izquierda).
- La tarjeta **Colores de dígitos** tiene dos páginas y la pestaña es el interruptor.
- La tarjeta **Tareas propias** planifica ventanas como «segundos 2 minutos antes de cada hora».
- El cambio usa una comprobación global del toque y **no interfiere con bajar el panel**.

## Vista previa

<table><tr>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-off.jpg" alt="Sin segundos: `15:06`, la decena de hora conserva su color" width="100%">
<br>
Sin segundos: `15:06`, la decena de hora conserva su color
</td>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-on.jpg" alt="Con segundos: `15:06:03`, minutos y segundos con el color del sistema" width="100%">
<br>
Con segundos: `15:06:03`, minutos y segundos con el color del sistema
</td>
</tr></table>

## Cómo funciona

El módulo solo engancha la **SystemUI** y usa la API legacy estable de Xposed.
**El núcleo usa solo API públicas de AOSP; las clases de fabricante son opcionales y se omiten si no existen**, por lo que no rompe el sistema ni dispara el modo seguro de LSPosed:

- **Segundos**: `TextView.setText` (after) guarda el texto del sistema y `TextView.onDraw` (before) escribe `original:ss`, refresco cada segundo con `Handler`.
- **Color por dígito**: un `ForegroundColorSpan` por carácter (se quitan spans ajenos); los dos puntos se suben 0.11em porque los diseños con varios tramos dejan el glifo más bajo.
- **Degradado**: `CharacterStyle` llama a `setShader` en `updateDrawState`; la vista previa **se dibuja sola** (`GradientTimeView`) porque ColorOS 15 cuantiza el shader por tramo de texto.
- **Alternar**: el panel intercepta los toques en la barra, así que el módulo engancha `View.dispatchTouchEvent` global y solo cambia si el toque cae dentro del reloj sin arrastrar; el evento **no se consume**.
- **Tareas**: comprobar «¿la hora local cae en la ventana?» con temporizadores exactos en los bordes y repaso cada 60 segundos.

## Consumo

- **Con segundos**: temporizador cada segundo (coste inevitable). Medido en SystemUI por núcleo: 7.0% apagado, 7.25% encendido (60 s).
- **Sin segundos**: el temporizador **se detiene** (`stopTicker`), no hay despertares por segundo.
- **Protección de color** retrocede 2s → 4s → … → 30s y termina tras 5 minutos sin pérdidas.
- El estado se **cachea en el proceso**; las tareas usan `Handler.postDelayed`: **sin WakeLock ni AlarmManager**, no despiertan el sueño profundo.

## Compilar

Hace falta **JDK 17+** (17 o 21) y **Android SDK Platform 35** (AGP descarga Build-Tools 35).

```bash
export JAVA_HOME=/path/to/jdk-17-or-21
./gradlew assembleDebug
```

## Copia y continuación del desarrollo (importante)

En la carpeta `ClockSeconds/` guarda **todo menos `build/` y `.gradle/`** (el código son ~300 KB):

| Categoría | Archivos | Nota |
|------|------|------|
| Archivos de compilación | `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties`, `keystore.properties` | Versiones, dependencias, parámetros, firma |
| Gradle Wrapper | `gradlew`, `gradlew.bat`, `gradle/wrapper/*` | Fija Gradle 8.14.5 |
| Código | `app/src/main/java/**` (20 `.java`) | Lógica del módulo, ajustes, clases de dibujo |
| Recursos | `app/src/main/res/**` | Textos (22 idiomas), temas |
| Metadatos Xposed | `app/src/main/resources/META-INF/xposed/*` | Clase de entrada, ámbito, nivel de API |
| Manifest | `app/src/main/AndroidManifest.xml` | Permisos, actividades |
| Herramientas | `tools/**` | Generador de README, comprobación i18n, script de release |
| Documentos | `README*.md`, `docs/**`, `CHANGELOG.md` | Descripción, capturas, registro de cambios |
| CI | `.github/workflows/*.yml` | Compilación firmada con etiqueta |

## Solución de problemas

<details>
<summary>Ver pasos</summary>

Mira logcat:

- Las **compilaciones Release** ocultan todos los registros de diagnóstico tras `BuildConfig.DEBUG`.
- Para depurar instala una **compilación debug** (`assembleDebug`, misma clave) y filtra:

  ```bash
  adb logcat -s ColorOSClockSeconds
  ```

- No aparecen segundos: revisa el ámbito «Interfaz del sistema» en LSPosed y reinicia; luego lee o escribe el interruptor:

  ```bash
  adb shell settings get secure coloros_clock_seconds_enabled   # 1=on 0=off
  adb shell settings put  secure coloros_clock_seconds_enabled 1
  ```

- Los colores o los segundos vuelven solos: el sistema o el tema reconstruye el texto y el módulo redibuja; si no se recupera busca `color-guard stopped` / `span-theft` en el log debug.

</details>

## Sugerencias

Dudas y propuestas en [GitHub Issues](https://github.com/kelebao/ClockSeconds/issues).

## License

[MIT](LICENSE) © kelebao
