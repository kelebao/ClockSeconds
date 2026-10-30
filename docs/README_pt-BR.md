<div align="center">

[简体中文](../README.md) · [English](README_EN.md) · [繁體中文](README_zh-TW.md) · [日本語](README_ja.md) · [한국어](README_ko.md) · [Türkçe](README_tr.md) · [Русский](README_ru.md) · [Tiếng Việt](README_vi.md) · [ไทย](README_th.md) · [Deutsch](README_de.md) · [Español](README_es.md) · [Français](README_fr.md) · [Українська](README_uk.md) · [Polski](README_pl.md) · **Português (Brasil)** · [فارسی](README_fa.md) · [العربية](README_ar.md) · [Bahasa Indonesia](README_id.md) · [Italiano](README_it.md) · [Ελληνικά](README_el.md) · [ქართული](README_ka.md)

</div>

<div align="center">

# Segundos no relógio da barra de status (Show Seconds in Time)

Mostra os **segundos** no relógio da barra de status, alternando com um toque. Você pode dar **uma cor a cada dígito** ou aplicar um **gradiente** na sequência toda.<br>**Toque longo** abre uma janela translúcida com o **horário da rede** (arrastar, tamanho, gradiente); as **tarefas próprias** ligam os segundos na hora escolhida.<br>O caminho principal usa apenas APIs públicas do AOSP (classes fechadas da OPPO / OnePlus / realme são opcionais e ignoradas quando não existem). Testado no ColorOS 15 / 16.

[![version](https://img.shields.io/badge/version-1.2.0-blue.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/releases/latest)
![android](https://img.shields.io/badge/Android-10%2B-brightgreen.svg?style=flat-square)
[![lsposed](https://img.shields.io/badge/LSPosed-module-orange.svg?style=flat-square)](https://github.com/Xposed-Modules-Repo/io.github.kelebao.colorosclockseconds/releases)
![coloros](https://img.shields.io/badge/ColorOS%2015%2F16-verified-success.svg?style=flat-square)
[![stars](https://img.shields.io/github/stars/kelebao/ClockSeconds.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/stargazers)
[![license](https://img.shields.io/badge/license-MIT-lightgrey.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/blob/main/LICENSE)

</div>

---

## Recursos

| Recurso | Detalhe |
|------|------|
| Segundos na barra | `HH:mm:ss` / `h:mm:ss a`, respeita o formato 12/24 h e o fuso horário. Um **espaço de largura zero** é adicionado no fim para os temas não removerem as cores; se as cores caírem, o texto é redesenhado (pausa de 5 minutos). |
| Alternar com um toque | **Tocar** no horário liga ou desliga os segundos (pode escolher **toque duplo**); o **toque longo** é da janela de prévia. Dentro de uma tarefa aparece o aviso de que ela está no controle. |
| Cor por dígito / gradiente | Seis dígitos **cada um com sua cor** (10 predefinições + barras HSV + cor do sistema) e os dois-pontos centralizados opticamente. Ou um **gradiente** da cor inicial à final (6 conjuntos, pontas editáveis, escurecido em fundo claro). A prévia **se desenha sozinha**, então bate com a barra pixel a pixel. |
| Prévia no toque longo | Janela translúcida com o **horário da rede**; arraste para mover, canto ◣ para o tamanho, posição e tamanho **ficam guardados**; aceita gradiente e dígitos de largura fixa. |
| Tarefas próprias | Janelas **a cada hora** ou **a cada dia**: ligar os segundos N minutos antes por M minutos e depois restaurar. Mudança de hora ou fuso é recalculada na hora, com revisão a cada 60 segundos. |
| Comparação com o horário da rede | Mostra a diferença para o **horário da rede**; um toque em **Sincronizar** liga o ajuste automático do sistema. |
| Sem precisar de adb | A escrita vai para `Settings.Secure`; sem `WRITE_SECURE_SETTINGS` um broadcast faz a SystemUI escrever — **nenhum recurso exige adb**. |
| Bloco rápido | Adicione o bloco “Mostrar segundos” para alternar sem abrir o aplicativo. |
| Nome do recurso do relógio | Se o relógio não for detectado, toque em **Detectar automaticamente** ou informe o nome do recurso (por exemplo `clock`). |
| 21 idiomas de interface | Segue o idioma do sistema: 简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, ไทย, Deutsch, Español, Français, Українська, Polski, Português (Brasil), فارسی, العربية, Bahasa Indonesia, Italiano, Ελληνικά, ქართული; os demais usam inglês. |
| Armazenamento / alcance | O estado fica em `Settings.Secure`, é compartilhado entre processos e sobrevive a reinicializações. **Testado no ColorOS 15 / 16 (Android 15 / 16)**; o núcleo usa apenas APIs públicas, então outras marcas devem funcionar, mas só OPPO / OnePlus foram testadas. Mínimo Android 10. |

## Modos de cor

Duas formas de colorir, alternadas no cartão **Cores dos dígitos**: **por dígito** ou **gradiente** na sequência toda.

<table><tr>
<td align="center" width="50%">
<img src="screenshots/colors-digit.png" alt="Por dígito: cada número com sua cor, dois-pontos centralizados opticamente" width="100%">
<br>
Por dígito: cada número com sua cor, dois-pontos centralizados opticamente
</td>
<td align="center" width="50%">
<img src="screenshots/colors-gradient.png" alt="Gradiente: da cor inicial à final, 6 conjuntos ou pontas próprias" width="100%">
<br>
Gradiente: da cor inicial à final, 6 conjuntos ou pontas próprias
</td>
</tr></table>

## Instalação

1. Baixe o APK em [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest)
2. Ative o módulo no **LSPosed** e marque o escopo **Interface do sistema** (`com.android.systemui`)
3. Reinicie o processo da SystemUI ou o aparelho
4. A tela de ajustes abre pelo **gerenciador do LSPosed** (não cria ícone no launcher)

> Precisa de root (LSPosed). **Normalmente não é preciso permissão adb**: a escrita usa `Settings.Secure` e, sem `WRITE_SECURE_SETTINGS`, um broadcast faz a SystemUI escrever. Para a tela de ajustes escrever **direto**:
> ```bash
> adb shell pm grant io.github.kelebao.colorosclockseconds android.permission.WRITE_SECURE_SETTINGS
> ```
> Sem conceder nada continua funcionando: toques, tarefas e janela rodam dentro da SystemUI.

## Uso

- **Toque no horário**: segundos on/off (padrão); nos ajustes dá para usar **toque duplo**.
- **Toque longo**: janela translúcida com o **horário da rede**, arraste para mover; tocar no horário mostra **fechar** (canto superior direito) e **tamanho** (inferior esquerdo).
- O cartão **Cores dos dígitos** tem duas páginas e a própria aba é o interruptor.
- O cartão **Tarefas próprias** agenda janelas como “segundos 2 minutos antes de cada hora”.
- A troca usa uma verificação global do toque e **não conflita com o painel** de notificações.

## Prévia

<table><tr>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-off.jpg" alt="Sem segundos: `15:06`, a dezena da hora mantém a cor" width="100%">
<br>
Sem segundos: `15:06`, a dezena da hora mantém a cor
</td>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-on.jpg" alt="Com segundos: `15:06:03`, minutos e segundos na cor do sistema" width="100%">
<br>
Com segundos: `15:06:03`, minutos e segundos na cor do sistema
</td>
</tr></table>

## Como funciona

O módulo só faz hook na **SystemUI** e usa a API legacy estável do Xposed.
**O núcleo usa apenas APIs públicas do AOSP; classes de fabricante são opcionais e ignoradas quando não existem**, então não quebra o sistema nem dispara o modo de segurança do LSPosed:

- **Segundos**: `TextView.setText` (after) guarda o texto do sistema e `TextView.onDraw` (before) escreve `original:ss`, atualizando a cada segundo via `Handler`.
- **Cor por dígito**: um `ForegroundColorSpan` por caractere (spans estranhos são removidos); os dois-pontos sobem 0.11em porque layouts com vários trechos colocam o glifo mais baixo.
- **Gradiente**: `CharacterStyle` chama `setShader` em `updateDrawState`; a prévia **se desenha sozinha** (`GradientTimeView`) porque o ColorOS 15 quantiza o shader por trecho de texto.
- **Alternar**: o painel intercepta os toques na barra, então o módulo faz hook global em `View.dispatchTouchEvent` e só alterna quando o toque cai dentro do relógio sem arrastar; o evento **não é consumido**.
- **Tarefas**: checar “o horário local está na janela” com timers exatos nas bordas e revisão a cada 60 segundos.

## Consumo

- **Com segundos**: timer a cada segundo (custo inevitável). Medido na SystemUI por núcleo: 7.0% desligado, 7.25% ligado (60 s).
- **Sem segundos**: o timer **para na hora** (`stopTicker`), sem despertar a cada segundo.
- A **proteção de cor** desacelera 2s → 4s → … → 30s e encerra após 5 minutos sem perdas.
- O estado é **cacheado no processo**; as tarefas usam `Handler.postDelayed`: **sem WakeLock nem AlarmManager**, não acordam o sono profundo.

## Compilar

É preciso **JDK 17+** (17 ou 21) e **Android SDK Platform 35** (o AGP baixa o Build-Tools 35).

```bash
export JAVA_HOME=/path/to/jdk-17-or-21
./gradlew assembleDebug
```

## Backup e continuidade do desenvolvimento (importante)

Na pasta `ClockSeconds/` guarde **tudo menos `build/` e `.gradle/`** (o código tem ~300 KB):

| Categoria | Arquivos | Observação |
|------|------|------|
| Arquivos de build | `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties`, `keystore.properties` | Versões, dependências, parâmetros, assinatura |
| Gradle Wrapper | `gradlew`, `gradlew.bat`, `gradle/wrapper/*` | Fixa o Gradle 8.14.5 |
| Código | `app/src/main/java/**` (20 `.java`) | Lógica do módulo, ajustes, classes de desenho |
| Recursos | `app/src/main/res/**` | Textos (22 idiomas), temas |
| Metadados Xposed | `app/src/main/resources/META-INF/xposed/*` | Classe de entrada, escopo, nível de API |
| Manifest | `app/src/main/AndroidManifest.xml` | Permissões, atividades |
| Ferramentas | `tools/**` | Gerador do README, checagem i18n, script de release |
| Documentos | `README*.md`, `docs/**`, `CHANGELOG.md` | Descrição, capturas, histórico de versões |
| CI | `.github/workflows/*.yml` | Build assinado na tag |

## Solução de problemas

<details>
<summary>Ver passos</summary>

Veja o logcat:

- **Builds release** escondem todos os logs de diagnóstico atrás de `BuildConfig.DEBUG`.
- Para depurar instale um **build debug** (`assembleDebug`, mesma chave) e filtre:

  ```bash
  adb logcat -s ColorOSClockSeconds
  ```

- Sem segundos: confira o escopo “Interface do sistema” no LSPosed e reinicie; depois leia ou grave a chave:

  ```bash
  adb shell settings get secure coloros_clock_seconds_enabled   # 1=on 0=off
  adb shell settings put  secure coloros_clock_seconds_enabled 1
  ```

- Cores ou segundos voltam sozinhos: o sistema ou o tema reconstrói o texto e o módulo redesenha; se não voltar, procure `color-guard stopped` / `span-theft` no log debug.

</details>

## Sugestões

Dúvidas e propostas em [GitHub Issues](https://github.com/kelebao/ClockSeconds/issues).

## License

[MIT](LICENSE) © kelebao
