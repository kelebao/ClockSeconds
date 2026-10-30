<div align="center">

[简体中文](../README.md) · [English](README_EN.md) · [繁體中文](README_zh-TW.md) · [日本語](README_ja.md) · [한국어](README_ko.md) · [Türkçe](README_tr.md) · [Русский](README_ru.md) · [Tiếng Việt](README_vi.md) · [ไทย](README_th.md) · [Deutsch](README_de.md) · [Español](README_es.md) · **Français** · [Українська](README_uk.md) · [Polski](README_pl.md) · [Português (Brasil)](README_pt-BR.md) · [فارسی](README_fa.md) · [العربية](README_ar.md) · [Bahasa Indonesia](README_id.md) · [Italiano](README_it.md) · [Ελληνικά](README_el.md) · [ქართული](README_ka.md)

</div>

<div align="center">

# Secondes dans la barre d'état (Show Seconds in Time)

Affiche les **secondes** dans l'horloge de la barre d'état, bascule au toucher. On peut donner **une couleur à chaque chiffre** ou appliquer un **dégradé** sur toute la chaîne.<br>**Appui long** : fenêtre translucide affichant l'**heure réseau** (déplacement, taille, dégradé) ; les **tâches personnelles** activent les secondes à l'heure voulue.<br>Le chemin principal n'utilise que des API publiques d'AOSP (les classes privées OPPO / OnePlus / realme sont optionnelles et ignorées si absentes). Testé sur ColorOS 15 / 16.

[![version](https://img.shields.io/badge/version-1.2.0-blue.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/releases/latest)
![android](https://img.shields.io/badge/Android-10%2B-brightgreen.svg?style=flat-square)
[![lsposed](https://img.shields.io/badge/LSPosed-module-orange.svg?style=flat-square)](https://github.com/Xposed-Modules-Repo/io.github.kelebao.colorosclockseconds/releases)
![coloros](https://img.shields.io/badge/ColorOS%2015%2F16-verified-success.svg?style=flat-square)
[![stars](https://img.shields.io/github/stars/kelebao/ClockSeconds.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/stargazers)
[![license](https://img.shields.io/badge/license-MIT-lightgrey.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/blob/main/LICENSE)

</div>

---

## Fonctions

| Fonction | Détail |
|------|------|
| Secondes dans la barre | `HH:mm:ss` / `h:mm:ss a`, respecte le format 12/24 h et le fuseau horaire. Un **espace de largeur nulle** est ajouté à la fin pour que les thèmes ne retirent pas les couleurs ; en cas de perte, redessin automatique (pause de 5 minutes). |
| Bascule au toucher | **Toucher** l'heure active ou coupe les secondes (option **double toucher**) ; l'**appui long** est réservé à la fenêtre d'aperçu. Pendant une tâche, un message indique qu'elle contrôle. |
| Couleur par chiffre / dégradé | Six chiffres **chacun sa couleur** (10 préréglages + curseurs HSV + couleur système), deux-points centrés optiquement. Ou un **dégradé** du début à la fin (6 jeux, extrémités libres, assombri sur fond clair). La carte d'aperçu **se dessine elle-même** et correspond au pixel près à la barre. |
| Aperçu par appui long | Fenêtre translucide avec l'**heure réseau** ; glisser pour déplacer, coin ◣ pour la taille, position et taille **mémorisées** ; dégradé et chiffres de largeur fixe. |
| Tâches personnelles | Fenêtres **chaque heure** ou **chaque jour** : activer les secondes N minutes avant pendant M minutes puis restaurer. Changement d'heure ou de fuseau recalculé aussitôt, plus un contrôle toutes les 60 secondes. |
| Comparaison heure réseau | Affiche l'écart avec l'**heure réseau** ; un appui sur **Synchroniser** active l'heure automatique du système. |
| Aucun adb nécessaire | L'écriture passe par `Settings.Secure` ; sans `WRITE_SECURE_SETTINGS` un broadcast fait écrire la SystemUI : **aucune fonction ne demande adb**. |
| Tuile rapide | Ajoutez la tuile « Afficher les secondes » pour basculer sans ouvrir l'application. |
| Nom de ressource de l'horloge | Si l'horloge n'est pas reconnue, touchez **Détection auto** ou saisissez le nom (ex. `clock`). |
| 21 langues d'interface | Suit la langue du système : 简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, ไทย, Deutsch, Español, Français, Українська, Polski, Português (Brasil), فارسی, العربية, Bahasa Indonesia, Italiano, Ελληνικά, ქართული ; les autres utilisent l'anglais. |
| Stockage / portée | L'état est dans `Settings.Secure`, partagé entre processus et conservé après redémarrage. **Testé sur ColorOS 15 / 16 (Android 15 / 16)** ; le cœur n'utilise que des API publiques, donc d'autres marques devraient fonctionner, mais seuls OPPO / OnePlus ont été testés. Minimum Android 10. |

## Modes de couleur

Deux façons de colorer, à basculer dans la carte **Couleurs des chiffres** : **par chiffre** ou **dégradé** sur toute la chaîne.

<table><tr>
<td align="center" width="50%">
<img src="screenshots/colors-digit.png" alt="Par chiffre : une couleur par chiffre, deux-points centrés optiquement" width="100%">
<br>
Par chiffre : une couleur par chiffre, deux-points centrés optiquement
</td>
<td align="center" width="50%">
<img src="screenshots/colors-gradient.png" alt="Dégradé : du début à la fin, 6 jeux ou extrémités personnalisées" width="100%">
<br>
Dégradé : du début à la fin, 6 jeux ou extrémités personnalisées
</td>
</tr></table>

## Installation

1. Téléchargez l'APK depuis [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest)
2. Activez le module dans **LSPosed** et cochez la portée **Interface système** (`com.android.systemui`)
3. Redémarrez le processus SystemUI ou l'appareil
4. L'écran de réglages s'ouvre depuis le **gestionnaire LSPosed** (pas d'icône dans le lanceur)

> Root (LSPosed) requis. **Les droits adb ne sont généralement pas nécessaires** : écriture via `Settings.Secure`, et sans `WRITE_SECURE_SETTINGS` un broadcast fait écrire la SystemUI. Pour que l'écran de réglages écrive **directement** :
> ```bash
> adb shell pm grant io.github.kelebao.colorosclockseconds android.permission.WRITE_SECURE_SETTINGS
> ```
> Sans autorisation tout fonctionne : toucher, tâches et fenêtre tournent dans la SystemUI.

## Utilisation

- **Toucher l'heure** : secondes on/off (par défaut) ; réglable en **double toucher**.
- **Appui long** : fenêtre translucide avec l'**heure réseau**, glisser pour déplacer ; toucher l'heure affiche **fermer** (haut droite) et **taille** (bas gauche).
- La carte **Couleurs des chiffres** a deux pages et l'onglet sert d'interrupteur.
- La carte **Tâches personnelles** planifie des fenêtres comme « secondes 2 minutes avant chaque heure ».
- Le basculement utilise un test tactile global et **n'entre pas en conflit avec le tiroir** de notifications.

## Aperçu

<table><tr>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-off.jpg" alt="Sans secondes : `15:06`, la dizaine d'heures garde sa couleur" width="100%">
<br>
Sans secondes : `15:06`, la dizaine d'heures garde sa couleur
</td>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-on.jpg" alt="Avec secondes : `15:06:03`, minutes et secondes en couleur système" width="100%">
<br>
Avec secondes : `15:06:03`, minutes et secondes en couleur système
</td>
</tr></table>

## Fonctionnement

Le module ne cible que la **SystemUI** et utilise l'API legacy Xposed, stable.
**Le cœur n'utilise que des API publiques d'AOSP ; les classes constructeur sont optionnelles et ignorées si absentes**, donc pas de casse du système ni de mode sans échec LSPosed :

- **Secondes** : `TextView.setText` (after) mémorise le texte système, puis `TextView.onDraw` (before) écrit `original:ss`, rafraîchi chaque seconde via `Handler`.
- **Couleur par chiffre** : un `ForegroundColorSpan` par caractère (les spans étrangers sont retirés) ; les deux-points sont remontés de 0.11em car les mises en page en plusieurs segments posent le glyphe plus bas.
- **Dégradé** : `CharacterStyle` appelle `setShader` dans `updateDrawState` ; l'aperçu **se dessine lui-même** (`GradientTimeView`) car ColorOS 15 quantifie le shader par segment de texte.
- **Basculement** : le panneau intercepte les touchers sur la barre, le module enganche donc `View.dispatchTouchEvent` globalement et ne bascule que si le toucher est dans l'horloge sans glissement ; l'événement **n'est pas consommé**.
- **Tâches** : « l'heure locale est-elle dans la fenêtre », minuteries exactes aux bornes plus contrôle toutes les 60 secondes.

## Consommation

- **Secondes on** : minuterie chaque seconde (coût inévitable). Mesuré sur SystemUI par cœur : 7.0% off, 7.25% on (60 s).
- **Secondes off** : la minuterie **s'arrête** (`stopTicker`), aucun réveil par seconde.
- La **protection des couleurs** ralentit 2s → 4s → … → 30s et s'arrête après 5 minutes sans perte.
- L'état est **mis en cache dans le processus** ; les tâches utilisent `Handler.postDelayed` : **ni WakeLock ni AlarmManager**, pas de réveil du sommeil profond.

## Compiler

Il faut **JDK 17+** (17 ou 21) et **Android SDK Platform 35** (AGP télécharge Build-Tools 35).

```bash
export JAVA_HOME=/path/to/jdk-17-or-21
./gradlew assembleDebug
```

## Sauvegarde et reprise du développement (important)

Dans le dossier `ClockSeconds/` gardez **tout sauf `build/` et `.gradle/`** (le code fait ~300 Ko) :

| Catégorie | Fichiers | Remarque |
|------|------|------|
| Fichiers de build | `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties`, `keystore.properties` | Versions, dépendances, paramètres, signature |
| Gradle Wrapper | `gradlew`, `gradlew.bat`, `gradle/wrapper/*` | Fixe Gradle 8.14.5 |
| Code | `app/src/main/java/**` (20 `.java`) | Logique du module, réglages, classes de dessin |
| Ressources | `app/src/main/res/**` | Textes (22 langues), thèmes |
| Métadonnées Xposed | `app/src/main/resources/META-INF/xposed/*` | Classe d'entrée, portée, niveau d'API |
| Manifest | `app/src/main/AndroidManifest.xml` | Permissions, activités |
| Outils | `tools/**` | Générateur de README, contrôle i18n, script de release |
| Documents | `README*.md`, `docs/**`, `CHANGELOG.md` | Description, captures, journal des versions |
| CI | `.github/workflows/*.yml` | Build signé sur étiquette |

## Dépannage

<details>
<summary>Voir les étapes</summary>

Regardez logcat :

- Les **builds Release** masquent tous les journaux de diagnostic derrière `BuildConfig.DEBUG`.
- Pour déboguer, installez un **build debug** (`assembleDebug`, même clé) puis filtrez :

  ```bash
  adb logcat -s ColorOSClockSeconds
  ```

- Aucune seconde : vérifiez la portée « Interface système » dans LSPosed et redémarrez ; lisez ou écrivez l'interrupteur :

  ```bash
  adb shell settings get secure coloros_clock_seconds_enabled   # 1=on 0=off
  adb shell settings put  secure coloros_clock_seconds_enabled 1
  ```

- Les couleurs ou secondes reviennent seules : le système ou le thème reconstruit le texte et le module redessine ; sinon cherchez `color-guard stopped` / `span-theft` dans le log debug.

</details>

## Retours

Questions et suggestions via [GitHub Issues](https://github.com/kelebao/ClockSeconds/issues).

## License

[MIT](LICENSE) © kelebao
