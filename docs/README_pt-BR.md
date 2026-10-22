<div align="center">

[简体中文](../README.md) · [English](README_EN.md) · [繁體中文](README_zh-TW.md) · [日本語](README_ja.md) · [한국어](README_ko.md) · [Türkçe](README_tr.md) · [Русский](README_ru.md) · [Tiếng Việt](README_vi.md) · [ไทย](README_th.md) · [Deutsch](README_de.md) · [Español](README_es.md) · [Français](README_fr.md) · [Українська](README_uk.md) · [Polski](README_pl.md) · **Português (Brasil)** · [فارسی](README_fa.md) · [العربية](README_ar.md) · [Bahasa Indonesia](README_id.md) · [Italiano](README_it.md) · [Ελληνικά](README_el.md) · [ქართული](README_ka.md)

</div>

<div align="center">

# Segundos na hora（Show Seconds in Time）

Mostre os **segundos** na hora da barra de status, alterne com um único toque e mantenha a **dezena da hora em vermelho**.<br>Sem classes privadas de fabricante. Verificado em dispositivo real com ColorOS 15; também cobre Xiaomi, OnePlus, Honor, vivo e outros grandes fabricantes.

[![version](https://img.shields.io/badge/version-1.0.4-blue.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/releases/latest)
![android](https://img.shields.io/badge/Android-10%2B-brightgreen.svg?style=flat-square)
[![lsposed](https://img.shields.io/badge/LSPosed-module-orange.svg?style=flat-square)](https://github.com/Xposed-Modules-Repo/io.github.kelebao.colorosclockseconds/releases)
![coloros](https://img.shields.io/badge/ColorOS%2015-verified-success.svg?style=flat-square)
[![stars](https://img.shields.io/github/stars/kelebao/ClockSeconds.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/stargazers)
[![license](https://img.shields.io/badge/license-MIT-lightgrey.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/blob/main/LICENSE)

</div>

---

## Recursos

| Recurso | Descrição |
|------|------|
| Segundos | A hora da barra de status mostra os segundos (`HH:mm:ss` / `h:mm:ss a`, segue a configuração de 12/24 horas do sistema) |
| Toque para alternar | **Tocar** na hora da barra de status alterna a exibição dos segundos; também é possível usar **toque duplo** na página de configurações |
| Dezena da hora em vermelho | A **dezena da hora** é pintada de vermelho (o `1` em `13`, o `0` em `05`); minutos e segundos mantêm a cor do sistema. Horas de um único dígito (ex. `9:45`) não têm dezena e ficam intactas. Efetivo em todos os fabricantes |
| Idiomas da interface | A interface do app está traduzida em **21 idiomas** (segue o idioma do sistema automaticamente): 简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, ไทย, Deutsch, Español, Français, Українська, Polski, Português (Brasil), فارسی, العربية, Bahasa Indonesia, Italiano, Ελληνικά, ქართული |
| Persistência | O estado é salvo em `Settings.Secure`, compartilhado entre processos e mantido após reinícios do SystemUI |
| Compatibilidade | Verificado no ColorOS 15 / 16 / 17 (OPPO / OnePlus / realme); também funciona direto em outros fabricantes com root como Xiaomi / Honor / Redmi. Sem classes privadas de fabricante, então o modo de segurança do LSPosed não é acionado. Android 10 no mínimo |

## Instalação

1. Baixe o APK mais recente na página [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest)
2. Ative o módulo no **LSPosed**, escopo: **Interface do sistema (`com.android.systemui`)**
3. Reinicie o processo SystemUI ou reinicie o dispositivo
4. Abra a página de configurações pelo **gerenciador do LSPosed** (nenhum ícone é criado na tela inicial)

> Requer root (LSPosed). Para conceder `WRITE_SECURE_SETTINGS` e poder escrever em `Settings.Secure`:
> ```bash
> adb shell pm grant io.github.kelebao.colorosclockseconds android.permission.WRITE_SECURE_SETTINGS
> ```
> Sem ela, você ainda pode alternar tocando na hora da barra de status (essa ação roda dentro do processo SystemUI, que já tem a permissão por natureza).

## Uso

- **Toque na hora da barra de status**: alterna a exibição dos segundos (gesto padrão).
- A alternância é implementada com um teste de acerto de toque global, então ela **nunca entra em conflito com "puxar para baixo o painel de notificações"**—— um toque puro alterna, um arrasto continua puxando o painel. Se preferir, você pode trocar o gesto para **toque duplo** na página de configurações.
- A página de configurações mostra o estado atual, o gesto escolhido e um botão de atualizar.

## Prévia

<table><tr>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-off.jpg" alt="Segundos desativados: `15:06` — o dígito da dezena da hora `1` continua vermelho" width="100%">
<br>
Segundos desativados: `15:06` — o dígito da dezena da hora `1` continua vermelho
</td>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-on.jpg" alt="Segundos ativados: `15:06:03` — minutos e segundos mantêm a cor do sistema" width="100%">
<br>
Segundos ativados: `15:06:03` — minutos e segundos mantêm a cor do sistema
</td>
</tr></table>

## Como funciona

O módulo só hooka o **SystemUI**, usando a abordagem legacy Xposed bem testada,
e **nunca toca em nenhuma classe privada de fabricante**, então não quebra o estado interno do sistema nem aciona o modo de segurança do LSPosed:

- **Segundos**: hook do estável `TextView.setText` (after) para cachear o texto bruto que o sistema coloca na hora da barra de status,
  depois hook do `TextView.onDraw` (before) para sobrescrever a exibição com `textoBruto:ss` depois que o sistema tiver definido o texto,
  movido por um `Handler` de 1 segundo. Os segundos acrescentados herdam a cor do próprio texto da hora (medido `#ccffffff`).
  Além disso, a dezena da hora é pintada de vermelho (um `ForegroundColorSpan` vermelho nesse único caractere), estejam os segundos sendo exibidos ou não;
  minutos e segundos nunca ficam vermelhos; horas de um único dígito não têm dezena e ficam intactas.
- **Alternar**: o painel de notificações da ColorOS intercepta toques na área da barra de status, então o `OnTouchListener` do próprio hora nunca recebe eventos.
  O módulo por isso hooka o `View.dispatchTouchEvent` global e só inverte o interruptor `Settings.Secure` quando o toque cai dentro
  dos limites de tela da hora reconhecido e quase não se move (toque) ou é um segundo toque dentro de 350 ms (toque duplo). O módulo **não consome**
  nenhum evento de toque, então puxar para baixo o painel de notificações funciona como sempre.

## Nota sobre economia de energia

- **Segundos LIGADOS**: o módulo precisa de um timer de 1 segundo para mover as atualizações (o custo inerente de mostrar segundos, cerca de 60 redesenhos do hora por minuto).
- **Segundos DESLIGADOS**: o módulo **para esse timer imediatamente** (`stopTicker`)—— nenhum despertar por segundo e nenhuma leitura entre processos.
  Ele volta ao ritmo original do sistema de "atualizar uma vez por minuto", então desligar os segundos economiza energia de verdade.
- O estado é guardado em **cache dentro do processo** (`sSecondsEnabled`), então o timer não lê `Settings.Secure` a cada segundo (evitando um IPC por segundo).
- Nenhum formatador de tempo é construído: o módulo reutiliza o texto bruto do sistema e apenas acrescenta `:ss`, então não há overhead de formatação.
- O timer mantém apenas uma **referência fraca** à view e só continua enquanto a view estiver `isAttachedToWindow()`; quando a view é destruída, ele sai automaticamente.

## Build

Requer **JDK 17+** (17 ou 21 recomendados) e **Android SDK Platform 35** (o Build-Tools 35 é instalado automaticamente pelo AGP).

```bash
export JAVA_HOME=/path/to/jdk-17-or-21
./gradlew assembleDebug
```

## Backup e desenvolvimento contínuo (importante)

No diretório `ClockSeconds/`, **todos os arquivos exceto `build/` e `.gradle/` devem ser mantidos** (a parte de código tem apenas cerca de 140 KB):

| Categoria | Arquivo | Nota |
|------|------|------|
| Scripts de build | `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties` | Versão, dependências, parâmetros do compilador |
| Gradle Wrapper | `gradlew`, `gradlew.bat`, `gradle/wrapper/*` | Fixa o Gradle 8.14.5 |
| Código-fonte | `app/src/main/java/**` (3 arquivos `.java`) | O corpo da lógica do módulo |
| Recursos | `app/src/main/res/**` | Strings (chinês padrão + 21 idiomas values-xx), tema |
| Metadados do Xposed | `app/src/main/resources/META-INF/xposed/*` | Classe de entrada, escopo, versão da API |
| Manifest | `app/src/main/AndroidManifest.xml` | Permissões, Activity |
| Documentação | `README*.md` | Documentação em 21 idiomas |
| CI | `.github/workflows/release.yml` | Tag gera build assinado |

## Solução de problemas

<details>
<summary>Expandir as etapas de solução de problemas</summary>

Verifique o logcat:

- Os **builds Release** removem todos os logs de diagnóstico pelo portão `BuildConfig.DEBUG` (menor, mais limpo); nada deste módulo aparece no logcat.
- Para investigar, instale temporariamente o **build debug** (`assembleDebug`, assinado com a mesma chave de produção, então pode ser instalado por cima do Release) e filtre pela TAG:

  ```bash
  adb logcat -s ColorOSClockSeconds
  ```

- Se os segundos não aparecem de forma alguma: primeiro confirme se o LSPosed está com o escopo SystemUI marcado e reiniciado; depois verifique se o interruptor ficou salvo como desligado—
  dá para ler / forçar pelo adb:

  ```bash
  adb shell settings get secure coloros_clock_seconds_enabled   # 1=ligado 0=desligado
  adb shell settings put  secure coloros_clock_seconds_enabled 1
  ```

- Se não funcionar em um dispositivo que não seja ColorOS: instale o build debug e olhe as linhas `candidate:` / `CONFIG CLOCK:` no log
  para ver se o nome do recurso / nome da classe da hora da barra de status do seu dispositivo está coberto pelas regras de reconhecimento; se não estiver, uma linha de regra extra basta.

</details>

## Feedback

Tem dúvidas, sugestões ou comentários? Deixe uma mensagem em [GitHub Issues](https://github.com/kelebao/ClockSeconds/issues) — respondo o quanto antes.

## License

[MIT](LICENSE) © kelebao
