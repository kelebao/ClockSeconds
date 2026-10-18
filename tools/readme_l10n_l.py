# -*- coding: utf-8 -*-
"""Slot translations for README generation: Français (fr)."""

TEXTS = {
    'fr': {
        'title': "Secondes sur l'horloge (Clock Seconds)",
        'tagline': (
            "Affichez les **secondes** sur l'horloge de la barre d'état, basculez d'un simple "
            "appui et gardez la **dizaine de l'heure en rouge**.<br>"
            "Aucune classe privée de fabricant. Vérifié sur appareil réel sous ColorOS 15 ; "
            "couvre aussi Xiaomi, OnePlus, Honor, vivo et les autres grands fabricants."
        ),
        'h_features': 'Fonctionnalités',
        'th_feature': 'Fonctionnalité',
        'th_desc': 'Description',
        'rows': [
            ('Secondes',
             "L'horloge de la barre d'état affiche les secondes (`HH:mm:ss` / `h:mm:ss a`, suit le réglage 12/24 h du système)"),
            ('Appui pour basculer',
             "Un **appui** sur l'horloge de la barre d'état active/désactive les secondes ; "
             "le geste **appui long** est aussi disponible dans la page de réglages"),
            ("Chiffre des dizaines en rouge",
             "La **dizaine de l'heure** est colorée en rouge (le `1` de `13`, le `0` de `05`) ; "
             "les minutes et les secondes gardent la couleur du système. Les heures à un seul "
             "chiffre (ex. `9:45`) n'ont pas de dizaine et restent intactes. Efficace chez tous les fabricants"),
            ("Langues de l'interface",
             "L'interface de l'application est traduite en **21 langues** (suit automatiquement la langue du système) : "
             "简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, ไทย, "
             "Deutsch, Español, Français, Українська, Polski, Português (Brasil), "
             "فارسی, العربية, Bahasa Indonesia, Italiano, Ελληνικά, ქართული"),
            ('Persistance',
             "L'état est stocké dans `Settings.Secure`, partagé entre les processus et conservé après un redémarrage de SystemUI"),
            ('Compatibilité',
             "Vérifié sur ColorOS 15 / 16 / 17 (OPPO / OnePlus / realme) ; fonctionne aussi tel quel "
             "sur d'autres fabricants rootés comme Xiaomi / Honor / Redmi. Aucune classe privée de "
             "fabricant, donc le mode sans échec de LSPosed n'est pas déclenché. Android 10 minimum"),
        ],
        'h_install': 'Installation',
        'install': [
            "Téléchargez le dernier APK depuis la page [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest)",
            "Activez le module dans **LSPosed**, portée : **Interface système (`com.android.systemui`)**",
            "Redémarrez le processus SystemUI ou redémarrez l'appareil",
            "Ouvrez la page de réglages depuis le **gestionnaire LSPosed** (aucune icône n'est créée sur le bureau)",
        ],
        'note1': (
            "Nécessite root (LSPosed). Pour accorder `WRITE_SECURE_SETTINGS` afin d'écrire "
            "dans `Settings.Secure` :"
        ),
        'note2': (
            "Sans cette permission, vous pouvez toujours basculer en appuyant sur l'horloge de la "
            "barre d'état (cette action s'exécute dans le processus SystemUI, qui en a déjà le droit)."
        ),
        'h_usage': 'Utilisation',
        'usage': [
            "**Appuyez sur l'horloge de la barre d'état** : active/désactive les secondes (geste par défaut).",
            "La bascule repose sur un test de touche globale, elle n'**entre donc jamais en conflit "
            "avec le « défilement du panneau de notifications »**—— un appui pur bascule, un "
            "balayage continue de dérouler le panneau. Si vous préférez, vous pouvez passer au "
            "geste **appui long** dans la page de réglages.",
            "La page de réglages affiche l'état actuel, le geste choisi et un bouton d'actualisation.",
        ],
        'h_demo': 'Aperçu',
        'cap_off': "Secondes désactivées : `15:06` — le chiffre des dizaines de l'heure `1` reste rouge",
        'cap_on': 'Secondes activées : `15:06:03` — minutes et secondes gardent la couleur système',
        'h_how': 'Comment ça marche',
        'how_intro': [
            "Le module n'accroche que **SystemUI**, en utilisant l'approche legacy Xposed bien rodée,",
            "et **ne touche jamais aucune classe privée de fabricant** : il ne casse donc jamais "
            "l'état interne du système ni ne déclenche le mode sans échec de LSPosed :",
        ],
        'how': [
            "**Secondes** : accrochez le stable `TextView.setText` (after) pour mettre en cache le "
            "texte brut que le système pose sur l'horloge de la barre d'état,\n"
            "puis accrochez `TextView.onDraw` (before) pour réécrire l'affichage en "
            "`texteBrut:ss` une fois que le système a posé le texte,\n"
            "piloté par un `Handler` d'une seconde. Les secondes ajoutées héritent de la couleur "
            "du texte de l'horloge (mesurée `#ccffffff`).\n"
            "La dizaine de l'heure est en plus colorée en rouge (un `ForegroundColorSpan` rouge sur "
            "ce seul caractère), que les secondes soient affichées ou non ;\n"
            "les minutes et les secondes ne sont jamais rouges ; les heures à un seul chiffre "
            "n'ont pas de dizaine et restent intactes.",
            "**Bascule** : le panneau de notifications de ColorOS intercepte les touchers dans la "
            "zone de la barre d'état, si bien que l'`OnTouchListener` de l'horloge elle-même ne "
            "reçoit jamais les événements.\n"
            "Le module accroche donc le `View.dispatchTouchEvent` global et ne fait basculer "
            "l'interrupteur `Settings.Secure` que lorsque l'appui tombe dans\n"
            "les limites à l'écran de l'horloge reconnue, avec un déplacement quasi nul (appui) "
            "ou un maintien de 500 ms (appui long). Le module ne **consomme**\n"
            "aucun événement tactile : le défilement du panneau de notifications fonctionne donc comme d'habitude.",
        ],
        'h_power': 'Note sur les économies d\'énergie',
        'power': [
            "**Secondes ACTIVÉES** : le module a besoin d'un minuteur d'une seconde pour piloter "
            "les rafraîchissements (le coût inhérent à l'affichage des secondes, environ 60 "
            "redessins de l'horloge par minute).",
            "**Secondes DÉSACTIVÉES** : le module **arrête immédiatement ce minuteur** "
            "(`stopTicker`)—— plus aucun réveil chaque seconde, plus aucune lecture inter-processus.\n"
            "Il revient au rythme d'origine du système « un rafraîchissement par minute », donc "
            "désactiver les secondes économise vraiment de l'énergie.",
            "L'état est mis en **cache dans le processus** (`sSecondsEnabled`) : le minuteur ne lit "
            "pas `Settings.Secure` chaque seconde (un IPC par seconde est évité).",
            "Aucun formateur d'heure n'est construit : le module réutilise le texte brut du système "
            "et se contente d'ajouter `:ss`, donc aucun surcoût de formatage.",
            "Le minuteur ne détient qu'une **référence faible** sur la vue et ne continue que "
            "tant que la vue est `isAttachedToWindow()` ; une fois la vue détruite, il sort automatiquement.",
        ],
        'h_build': 'Compilation',
        'build_intro': (
            "Nécessite **JDK 17+** (17 ou 21 recommandés) et **Android SDK Platform 35** "
            "(Build-Tools 35 est installé automatiquement par AGP)."
        ),
        'h_backup': 'Sauvegarde et développement continu (important)',
        'backup_intro': (
            "Dans le répertoire `ClockSeconds/`, **conservez tous les fichiers sauf "
            "`build/` et `.gradle/`** (la partie source ne pèse qu'environ 140 Ko) :"
        ),
        'th_backup_cat': 'Catégorie',
        'th_backup_files': 'Fichier',
        'th_backup_note': 'Remarque',
        'backup_rows': [
            ('Scripts de compilation',
             '`settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties`',
             'Version, dépendances, paramètres du compilateur'),
            ('Gradle Wrapper', '`gradlew`, `gradlew.bat`, `gradle/wrapper/*`', 'Fixe Gradle 8.14.5'),
            ('Code source', '`app/src/main/java/**` (3 fichiers `.java`)', 'Le corps de la logique du module'),
            ('Ressources', '`app/src/main/res/**`',
             'Chaînes (chinois par défaut + 21 langues values-xx), thème'),
            ('Métadonnées Xposed', '`app/src/main/resources/META-INF/xposed/*`',
             "Classe d'entrée, portée, version d'API"),
            ('Manifeste', '`app/src/main/AndroidManifest.xml`', 'Permissions, Activity'),
            ('Documentation', '`README*.md`', 'Documentation en 21 langues'),
            ('CI', '`.github/workflows/release.yml`', 'Une étiquette produit une compilation signée'),
        ],
        'h_trouble': 'Dépannage',
        'trouble_summary': 'Déplier les étapes de dépannage',
        'trouble_intro': 'Consultez le logcat :',
        'trouble': [
            ('b', 'Les **compilations Release** retirent tous les journaux de diagnostic via la '
                  'porte `BuildConfig.DEBUG` (plus léger, plus propre) ; rien de ce module '
                  "n'apparaît dans le logcat."),
            ('b', 'Pour investiguer, installez temporairement la **compilation debug** '
                  '(`assembleDebug`, signée avec la même clé de production, donc installable '
                  'par-dessus la Release) puis filtrez par TAG :'),
            ('code', ['adb logcat -s ColorOSClockSeconds']),
            ('b', "Si aucune seconde ne s'affiche : vérifiez d'abord que LSPosed a bien la portée "
                  'SystemUI cochée et un redémarrage effectué ; puis vérifiez si l\'interrupteur a '
                  'été persisté comme désactivé——\n'
                  'à lire / forcer via adb :'),
            ('code', [
                'adb shell settings get secure coloros_clock_seconds_enabled   # 1=activé 0=désactivé',
                'adb shell settings put  secure coloros_clock_seconds_enabled 1',
            ]),
            ('b', "Si rien ne fonctionne sur un appareil non ColorOS : installez la compilation "
                  'debug et regardez les lignes `candidate:` / `CONFIG CLOCK:` du journal\n'
                  "pour voir si le nom de ressource / le nom de classe de l'horloge de la barre "
                  "d'état de votre appareil est couvert par les règles de reconnaissance ; "
                  "sinon, une simple ligne de règle en plus suffit."),
        ],
        'h_license': 'License',
    },
}
