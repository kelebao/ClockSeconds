# -*- coding: utf-8 -*-
"""Slot translations for README generation: Português (Brasil) (pt-BR)."""

TEXTS = {
    'pt-BR': {
        'title': 'Segundos no relógio da barra de status (Show Seconds in Time)',
        'tagline': (
            'Mostra os **segundos** no relógio da barra de status, alternando com um toque. Você pode dar '
            '**uma cor a cada dígito** ou aplicar um **gradiente** na sequência toda.<br>'
            '**Toque longo** abre uma janela translúcida com o **horário da rede** (arrastar, tamanho, gradiente); '
            'as **tarefas próprias** ligam os segundos na hora escolhida.<br>'
            'O caminho principal usa apenas APIs públicas do AOSP (classes fechadas da OPPO / OnePlus / realme são '
            'opcionais e ignoradas quando não existem). Testado no ColorOS 15 / 16.'
        ),
        'h_features': 'Recursos',
        'th_feature': 'Recurso',
        'th_desc': 'Detalhe',
        'rows': [
            ('Segundos na barra',
             '`HH:mm:ss` / `h:mm:ss a`, respeita o formato 12/24 h e o fuso horário. Um **espaço de largura zero** '
             'é adicionado no fim para os temas não removerem as cores; se as cores caírem, o texto é redesenhado '
             '(pausa de 5 minutos).'),
            ('Alternar com um toque',
             '**Tocar** no horário liga ou desliga os segundos (pode escolher **toque duplo**); o **toque longo** '
             'é da janela de prévia. Dentro de uma tarefa aparece o aviso de que ela está no controle.'),
            ('Cor por dígito / gradiente',
             'Seis dígitos **cada um com sua cor** (10 predefinições + barras HSV + cor do sistema) e os dois-pontos '
             'centralizados opticamente. Ou um **gradiente** da cor inicial à final (6 conjuntos, pontas editáveis, '
             'escurecido em fundo claro). A prévia **se desenha sozinha**, então bate com a barra pixel a pixel.'),
            ('Prévia no toque longo',
             'Janela translúcida com o **horário da rede**; arraste para mover, canto ◣ para o tamanho, posição e '
             'tamanho **ficam guardados**; aceita gradiente e dígitos de largura fixa.'),
            ('Tarefas próprias',
             'Janelas **a cada hora** ou **a cada dia**: ligar os segundos N minutos antes por M minutos e depois '
             'restaurar. Mudança de hora ou fuso é recalculada na hora, com revisão a cada 60 segundos.'),
            ('Comparação com o horário da rede',
             'Mostra a diferença para o **horário da rede**; um toque em **Sincronizar** liga o ajuste automático '
             'do sistema.'),
            ('Sem precisar de adb',
             'A escrita vai para `Settings.Secure`; sem `WRITE_SECURE_SETTINGS` um broadcast faz a SystemUI '
             'escrever — **nenhum recurso exige adb**.'),
            ('Bloco rápido',
             'Adicione o bloco “Mostrar segundos” para alternar sem abrir o aplicativo.'),
            ('Nome do recurso do relógio',
             'Se o relógio não for detectado, toque em **Detectar automaticamente** ou informe o nome do recurso '
             '(por exemplo `clock`).'),
            ('21 idiomas de interface',
             'Segue o idioma do sistema: 简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, '
             'ไทย, Deutsch, Español, Français, Українська, Polski, Português (Brasil), فارسی, العربية, '
             'Bahasa Indonesia, Italiano, Ελληνικά, ქართული; os demais usam inglês.'),
            ('Armazenamento / alcance',
             'O estado fica em `Settings.Secure`, é compartilhado entre processos e sobrevive a reinicializações. '
             '**Testado no ColorOS 15 / 16 (Android 15 / 16)**; o núcleo usa apenas APIs públicas, então outras '
             'marcas devem funcionar, mas só OPPO / OnePlus foram testadas. Mínimo Android 10.'),
        ],
        'h_colors': 'Modos de cor',
        'colors_intro': (
            'Duas formas de colorir, alternadas no cartão **Cores dos dígitos**: **por dígito** '
            'ou **gradiente** na sequência toda.'
        ),
        'cap_digit': 'Por dígito: cada número com sua cor, dois-pontos centralizados opticamente',
        'cap_grad': 'Gradiente: da cor inicial à final, 6 conjuntos ou pontas próprias',
        'h_install': 'Instalação',
        'install': [
            'Baixe o APK em [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest)',
            'Ative o módulo no **LSPosed** e marque o escopo **Interface do sistema** (`com.android.systemui`)',
            'Reinicie o processo da SystemUI ou o aparelho',
            'A tela de ajustes abre pelo **gerenciador do LSPosed** (não cria ícone no launcher)',
        ],
        'note1': (
            'Precisa de root (LSPosed). **Normalmente não é preciso permissão adb**: a escrita usa '
            '`Settings.Secure` e, sem `WRITE_SECURE_SETTINGS`, um broadcast faz a SystemUI escrever. Para a tela '
            'de ajustes escrever **direto**:'
        ),
        'note2': 'Sem conceder nada continua funcionando: toques, tarefas e janela rodam dentro da SystemUI.',
        'h_usage': 'Uso',
        'usage': [
            '**Toque no horário**: segundos on/off (padrão); nos ajustes dá para usar **toque duplo**.',
            '**Toque longo**: janela translúcida com o **horário da rede**, arraste para mover; tocar no horário '
            'mostra **fechar** (canto superior direito) e **tamanho** (inferior esquerdo).',
            'O cartão **Cores dos dígitos** tem duas páginas e a própria aba é o interruptor.',
            'O cartão **Tarefas próprias** agenda janelas como “segundos 2 minutos antes de cada hora”.',
            'A troca usa uma verificação global do toque e **não conflita com o painel** de notificações.',
        ],
        'h_demo': 'Prévia',
        'cap_off': 'Sem segundos: `15:06`, a dezena da hora mantém a cor',
        'cap_on': 'Com segundos: `15:06:03`, minutos e segundos na cor do sistema',
        'h_how': 'Como funciona',
        'how_intro': [
            'O módulo só faz hook na **SystemUI** e usa a API legacy estável do Xposed.',
            '**O núcleo usa apenas APIs públicas do AOSP; classes de fabricante são opcionais e ignoradas quando '
            'não existem**, então não quebra o sistema nem dispara o modo de segurança do LSPosed:',
        ],
        'how': [
            '**Segundos**: `TextView.setText` (after) guarda o texto do sistema e `TextView.onDraw` (before) '
            'escreve `original:ss`, atualizando a cada segundo via `Handler`.',
            '**Cor por dígito**: um `ForegroundColorSpan` por caractere (spans estranhos são removidos); os '
            'dois-pontos sobem 0.11em porque layouts com vários trechos colocam o glifo mais baixo.',
            '**Gradiente**: `CharacterStyle` chama `setShader` em `updateDrawState`; a prévia **se desenha '
            'sozinha** (`GradientTimeView`) porque o ColorOS 15 quantiza o shader por trecho de texto.',
            '**Alternar**: o painel intercepta os toques na barra, então o módulo faz hook global em '
            '`View.dispatchTouchEvent` e só alterna quando o toque cai dentro do relógio sem arrastar; o evento '
            '**não é consumido**.',
            '**Tarefas**: checar “o horário local está na janela” com timers exatos nas bordas e revisão a cada '
            '60 segundos.',
        ],
        'h_power': 'Consumo',
        'power': [
            '**Com segundos**: timer a cada segundo (custo inevitável). Medido na SystemUI por núcleo: 7.0% '
            'desligado, 7.25% ligado (60 s).',
            '**Sem segundos**: o timer **para na hora** (`stopTicker`), sem despertar a cada segundo.',
            'A **proteção de cor** desacelera 2s → 4s → … → 30s e encerra após 5 minutos sem perdas.',
            'O estado é **cacheado no processo**; as tarefas usam `Handler.postDelayed`: **sem WakeLock nem '
            'AlarmManager**, não acordam o sono profundo.',
        ],
        'h_build': 'Compilar',
        'build_intro': 'É preciso **JDK 17+** (17 ou 21) e **Android SDK Platform 35** (o AGP baixa o Build-Tools 35).',
        'h_backup': 'Backup e continuidade do desenvolvimento (importante)',
        'backup_intro': 'Na pasta `ClockSeconds/` guarde **tudo menos `build/` e `.gradle/`** (o código tem ~300 KB):',
        'th_backup_cat': 'Categoria',
        'th_backup_files': 'Arquivos',
        'th_backup_note': 'Observação',
        'backup_rows': [
            ('Arquivos de build', '`settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, '
             '`gradle.properties`, `keystore.properties`', 'Versões, dependências, parâmetros, assinatura'),
            ('Gradle Wrapper', '`gradlew`, `gradlew.bat`, `gradle/wrapper/*`', 'Fixa o Gradle 8.14.5'),
            ('Código', '`app/src/main/java/**` (20 `.java`)', 'Lógica do módulo, ajustes, classes de desenho'),
            ('Recursos', '`app/src/main/res/**`', 'Textos (22 idiomas), temas'),
            ('Metadados Xposed', '`app/src/main/resources/META-INF/xposed/*`', 'Classe de entrada, escopo, nível de API'),
            ('Manifest', '`app/src/main/AndroidManifest.xml`', 'Permissões, atividades'),
            ('Ferramentas', '`tools/**`', 'Gerador do README, checagem i18n, script de release'),
            ('Documentos', '`README*.md`, `docs/**`, `CHANGELOG.md`', 'Descrição, capturas, histórico de versões'),
            ('CI', '`.github/workflows/*.yml`', 'Build assinado na tag'),
        ],
        'h_trouble': 'Solução de problemas',
        'trouble_summary': 'Ver passos',
        'trouble_intro': 'Veja o logcat:',
        'trouble': [
            ('b', '**Builds release** escondem todos os logs de diagnóstico atrás de `BuildConfig.DEBUG`.'),
            ('b', 'Para depurar instale um **build debug** (`assembleDebug`, mesma chave) e filtre:'),
            ('code', ['adb logcat -s ColorOSClockSeconds']),
            ('b', 'Sem segundos: confira o escopo “Interface do sistema” no LSPosed e reinicie; depois leia ou '
                  'grave a chave:'),
            ('code', [
                'adb shell settings get secure coloros_clock_seconds_enabled   # 1=on 0=off',
                'adb shell settings put  secure coloros_clock_seconds_enabled 1',
            ]),
            ('b', 'Cores ou segundos voltam sozinhos: o sistema ou o tema reconstrói o texto e o módulo redesenha; '
                  'se não voltar, procure `color-guard stopped` / `span-theft` no log debug.'),
        ],
        'h_feedback': 'Sugestões',
        'feedback': 'Dúvidas e propostas em [GitHub Issues](https://github.com/kelebao/ClockSeconds/issues).',
        'h_license': 'License',
    },
}
