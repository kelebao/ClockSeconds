# -*- coding: utf-8 -*-
"""Slot translations for README generation: Português (Brasil) (pt-BR)."""

TEXTS = {
    'pt-BR': {
        'title': 'Segundos na hora（Show Seconds in Time）',
        'tagline': (
            'Mostre os **segundos** na hora da barra de status, alterne com um único toque '
            'e mantenha a **dezena da hora em vermelho**.<br>'
            'O caminho principal usa apenas APIs públicas do AOSP (a compatibilidade com classes privadas do fabricante é opcional, só para OPPO/OnePlus/realme e ignorada quando ausente). Verificado em dispositivo real com ColorOS 15; '
            'também cobre Xiaomi, OnePlus, Honor, vivo, Samsung e outros grandes fabricantes.'
        ),
        'h_features': 'Recursos',
        'th_feature': 'Recurso',
        'th_desc': 'Descrição',
        'rows': [
            ('Segundos',
             'A hora da barra de status mostra os segundos (`HH:mm:ss` / `h:mm:ss a`, segue a configuração de 12/24 horas do sistema)'),
            ('Toque para alternar',
             '**Tocar** na hora da barra de status alterna a exibição dos segundos; também é possível usar **toque duplo** na página de configurações'),
            ('Cor da dezena',
             'A cor da **dezena da hora** é **personalizável** (paleta de 8 cores) ou '
             '**desativável** para voltar à cor do sistema; horas de um único dígito (ex. `9:45`) '
             'não têm dezena e ficam intactas. Efetivo em todos os fabricantes'),
            ('Prévia ao pressionar',
             '**Pressionar** a hora da barra de status abre uma **janela flutuante de vidro '
             'fosco** com a **hora da rede** em tempo real; arrastável, redimensionável pelo ◣ '
             '(canto inferior esquerdo), toque na hora para ver **fechar** (✕ canto superior) / '
             '**redimensionar**; o tamanho do texto se adapta à janela'),
            ('Hora da rede',
             'A página de configurações compara seu relógio com a **hora padrão da rede** em '
             'tempo real; se a diferença for grande, toque em **Sincronizar** para ligar o '
             'horário automático do sistema'),
            ('Sem adb',
             'As configurações vão para `Settings.Secure`; sem a permissão '
             '`WRITE_SECURE_SETTINGS` o módulo usa um broadcast tratado pelo SystemUI — '
             '**não precisa de adb**'),
            ('Idiomas da interface',
             'A interface do app está traduzida em **21 idiomas** (segue o idioma do sistema automaticamente): '
             '简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, ไทย, '
             'Deutsch, Español, Français, Українська, Polski, Português (Brasil), '
             'فارسی, العربية, Bahasa Indonesia, Italiano, Ελληνικά, ქართული'),
            ('Persistência',
             'O estado é salvo em `Settings.Secure`, compartilhado entre processos e mantido após reinícios do SystemUI'),
            ('Compatibilidade',
             'Verificado no ColorOS 15 / 16 / 17 (OPPO / OnePlus / realme); também funciona direto '
             'em outros fabricantes com root como Xiaomi / Honor / Redmi / Samsung. O caminho principal usa apenas APIs públicas do '
             'AOSP, então o modo de segurança do LSPosed não é acionado. Android 10 no mínimo'),            ('Tile rápido',
             'Adicione um tile "Mostrar segundos" no painel, alterne com um toque'),
            ('Flutuante aprimorado',
             'Posição e tamanho do flutuante são **lembrados**; **toque duplo** na hora também alterna'),
            ('Detecção configurável',
             'Se o relógio não for detectado em outros aparelhos, toque em **Detecção automática** nas Configurações ou informe o nome do recurso (ex. clock) manualmente'),

        ],
        'h_install': 'Instalação',
        'install': [
            'Baixe o APK mais recente na página [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest)',
            'Ative o módulo no **LSPosed**, escopo: **Interface do sistema (`com.android.systemui`)**',
            'Reinicie o processo SystemUI ou reinicie o dispositivo',
            'Abra a página de configurações pelo **gerenciador do LSPosed** (nenhum ícone é criado na tela inicial)',
        ],
        'note1': (
            'Requer root (LSPosed). Para conceder `WRITE_SECURE_SETTINGS` e poder escrever '
            'em `Settings.Secure`:'
        ),
        'note2': (
            'Sem ela, você ainda pode alternar tocando na hora da barra de status '
            '(essa ação roda dentro do processo SystemUI, que já tem a permissão por natureza).'
        ),
        'h_usage': 'Uso',
        'usage': [
            '**Toque na hora da barra de status**: alterna a exibição dos segundos (gesto padrão).',
            'A alternância é implementada com um teste de acerto de toque global, então ela '
            '**nunca entra em conflito com "puxar para baixo o painel de notificações"**—— um toque '
            'puro alterna, um arrasto continua puxando o painel. Se preferir, você pode trocar o '
            'gesto para **toque duplo** na página de configurações.',
            'A página de configurações mostra o estado atual, o gesto escolhido e um botão de atualizar.',
        ],
        'h_demo': 'Prévia',
        'cap_off': 'Segundos desativados: `15:06` — o dígito da dezena da hora `1` continua vermelho',
        'cap_on': 'Segundos ativados: `15:06:03` — minutos e segundos mantêm a cor do sistema',
        'h_how': 'Como funciona',
        'how_intro': [
            'O módulo só hooka o **SystemUI**, usando a abordagem legacy Xposed bem testada,',
            'e **o caminho principal usa apenas APIs públicas do AOSP, e a compatibilidade com classes privadas do fabricante é opcional e ignorada quando ausente**, então não quebra o estado '
            'interno do sistema nem aciona o modo de segurança do LSPosed:',
        ],
        'how': [
            '**Segundos**: hook do estável `TextView.setText` (after) para cachear o texto bruto '
            'que o sistema coloca na hora da barra de status,\n'
            'depois hook do `TextView.onDraw` (before) para sobrescrever a exibição com '
            '`textoBruto:ss` depois que o sistema tiver definido o texto,\n'
            'movido por um `Handler` de 1 segundo. Os segundos acrescentados herdam a cor do '
            'próprio texto da hora (medido `#ccffffff`).\n'
            'Além disso, a dezena da hora é pintada de vermelho (um `ForegroundColorSpan` vermelho '
            'nesse único caractere), estejam os segundos sendo exibidos ou não;\n'
            'minutos e segundos nunca ficam vermelhos; horas de um único dígito não têm dezena '
            'e ficam intactas.',
            '**Alternar**: o painel de notificações da ColorOS intercepta toques na área da barra '
            'de status, então o `OnTouchListener` do próprio hora nunca recebe eventos.\n'
            'O módulo por isso hooka o `View.dispatchTouchEvent` global e só inverte o interruptor '
            '`Settings.Secure` quando o toque cai dentro\n'
            'dos limites de tela da hora reconhecido e quase não se move (toque) ou é um segundo toque '
            'dentro de 350 ms (toque duplo). O módulo **não consome**\n'
            'nenhum evento de toque, então puxar para baixo o painel de notificações funciona como sempre.',
        ],
        'h_power': 'Nota sobre economia de energia',
        'power': [
            '**Segundos LIGADOS**: o módulo precisa de um timer de 1 segundo para mover as '
            'atualizações (o custo inerente de mostrar segundos, cerca de 60 redesenhos do '
            'hora por minuto).',
            '**Segundos DESLIGADOS**: o módulo **para esse timer imediatamente** (`stopTicker`)—— '
            'nenhum despertar por segundo e nenhuma leitura entre processos.\n'
            'Ele volta ao ritmo original do sistema de "atualizar uma vez por minuto", então '
            'desligar os segundos economiza energia de verdade.',
            'O estado é guardado em **cache dentro do processo** (`sSecondsEnabled`), então o '
            'timer não lê `Settings.Secure` a cada segundo (evitando um IPC por segundo).',
            'Nenhum formatador de tempo é construído: o módulo reutiliza o texto bruto do sistema '
            'e apenas acrescenta `:ss`, então não há overhead de formatação.',
            'O timer mantém apenas uma **referência fraca** à view e só continua enquanto a view '
            'estiver `isAttachedToWindow()`; quando a view é destruída, ele sai automaticamente.',
        ],
        'h_build': 'Build',
        'build_intro': (
            'Requer **JDK 17+** (17 ou 21 recomendados) e **Android SDK Platform 35** '
            '(o Build-Tools 35 é instalado automaticamente pelo AGP).'
        ),
        'h_backup': 'Backup e desenvolvimento contínuo (importante)',
        'backup_intro': (
            'No diretório `ClockSeconds/`, **todos os arquivos exceto `build/` e '
            '`.gradle/` devem ser mantidos** (a parte de código tem apenas cerca de 140 KB):'
        ),
        'th_backup_cat': 'Categoria',
        'th_backup_files': 'Arquivo',
        'th_backup_note': 'Nota',
        'backup_rows': [
            ('Scripts de build',
             '`settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties`',
             'Versão, dependências, parâmetros do compilador'),
            ('Gradle Wrapper', '`gradlew`, `gradlew.bat`, `gradle/wrapper/*`', 'Fixa o Gradle 8.14.5'),
            ('Código-fonte', '`app/src/main/java/**` (3 arquivos `.java`)', 'O corpo da lógica do módulo'),
            ('Recursos', '`app/src/main/res/**`',
             'Strings (chinês padrão + 21 idiomas values-xx), tema'),
            ('Metadados do Xposed', '`app/src/main/resources/META-INF/xposed/*`',
             'Classe de entrada, escopo, versão da API'),
            ('Manifest', '`app/src/main/AndroidManifest.xml`', 'Permissões, Activity'),
            ('Documentação', '`README*.md`', 'Documentação em 21 idiomas'),
            ('CI', '`.github/workflows/release.yml`', 'Tag gera build assinado'),
        ],
        'h_trouble': 'Solução de problemas',
        'trouble_summary': 'Expandir as etapas de solução de problemas',
        'trouble_intro': 'Verifique o logcat:',
        'trouble': [
            ('b', 'Os **builds Release** removem todos os logs de diagnóstico pelo portão '
                  '`BuildConfig.DEBUG` (menor, mais limpo); nada deste módulo aparece no logcat.'),
            ('b', 'Para investigar, instale temporariamente o **build debug** (`assembleDebug`, '
                  'assinado com a mesma chave de produção, então pode ser instalado por cima '
                  'do Release) e filtre pela TAG:'),
            ('code', ['adb logcat -s ColorOSClockSeconds']),
            ('b', 'Se os segundos não aparecem de forma alguma: primeiro confirme se o LSPosed '
                  'está com o escopo SystemUI marcado e reiniciado; depois verifique se o '
                  'interruptor ficou salvo como desligado—\n'
                  'dá para ler / forçar pelo adb:'),
            ('code', [
                'adb shell settings get secure coloros_clock_seconds_enabled   # 1=ligado 0=desligado',
                'adb shell settings put  secure coloros_clock_seconds_enabled 1',
            ]),
            ('b', 'Se não funcionar em um dispositivo que não seja ColorOS: instale o build '
                  'debug e olhe as linhas `candidate:` / `CONFIG CLOCK:` no log\n'
                  'para ver se o nome do recurso / nome da classe da hora da barra de status '
                  'do seu dispositivo está coberto pelas regras de reconhecimento; se não estiver, '
                  'uma linha de regra extra basta.'),
        ],
        'h_feedback': 'Feedback',
        'feedback': 'Tem dúvidas, sugestões ou comentários? Deixe uma mensagem em [GitHub Issues](https://github.com/kelebao/ClockSeconds/issues) — respondo o quanto antes.',
        'h_license': 'License',
    },
}
