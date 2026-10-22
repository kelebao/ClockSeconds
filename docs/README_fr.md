<div align="center">

[简体中文](../README.md) · [English](README_EN.md) · [繁體中文](README_zh-TW.md) · [日本語](README_ja.md) · [한국어](README_ko.md) · [Türkçe](README_tr.md) · [Русский](README_ru.md) · [Tiếng Việt](README_vi.md) · [ไทย](README_th.md) · [Deutsch](README_de.md) · [Español](README_es.md) · **Français** · [Українська](README_uk.md) · [Polski](README_pl.md) · [Português (Brasil)](README_pt-BR.md) · [فارسی](README_fa.md) · [العربية](README_ar.md) · [Bahasa Indonesia](README_id.md) · [Italiano](README_it.md) · [Ελληνικά](README_el.md) · [ქართული](README_ka.md)

</div>

<div align="center">

# Secondes à l'heure（Show Seconds in Time）

Affichez les **secondes** sur l'heure de la barre d'état, basculez d'un simple appui et gardez la **dizaine de l'heure en rouge**.<br>Aucune classe privée de fabricant. Vérifié sur appareil réel sous ColorOS 15 ; couvre aussi Xiaomi, OnePlus, Honor, vivo, Samsung et les autres grands fabricants.

[![version](https://img.shields.io/badge/version-1.0.7-blue.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/releases/latest)
![android](https://img.shields.io/badge/Android-10%2B-brightgreen.svg?style=flat-square)
[![lsposed](https://img.shields.io/badge/LSPosed-module-orange.svg?style=flat-square)](https://github.com/Xposed-Modules-Repo/io.github.kelebao.colorosclockseconds/releases)
![coloros](https://img.shields.io/badge/ColorOS%2015-verified-success.svg?style=flat-square)
[![stars](https://img.shields.io/github/stars/kelebao/ClockSeconds.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/stargazers)
[![license](https://img.shields.io/badge/license-MIT-lightgrey.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/blob/main/LICENSE)

</div>

---

## Fonctionnalités

| Fonctionnalité | Description |
|------|------|
| Secondes | L'heure de la barre d'état affiche les secondes (`HH:mm:ss` / `h:mm:ss a`, suit le réglage 12/24 h du système) |
| Appui pour basculer | Un **appui** sur l'heure de la barre d'état active/désactive les secondes ; le geste **double appui** est aussi disponible dans la page de réglages |
| Couleur de la dizaine | La couleur de la **dizaine de l'heure** est **personnalisable** (palette de 8 couleurs) ou **désactivable** pour revenir à la couleur du système ; les heures à un seul chiffre (ex. `9:45`) n'ont pas de dizaine et restent intactes. Efficace chez tous les fabricants |
| Aperçu par appui long | Un **appui long** sur l'heure de la barre d'état ouvre une **fenêtre flottante en verre dépoli** affichant l'**heure réseau** en temps réel ; déplaçable, redimensionnable via ◣ (en bas à gauche), un appui sur l'heure fait apparaître **fermer** (✕ en haut à droite) / **redimensionner** ; la taille du texte s'adapte à la fenêtre |
| Heure réseau | La page de réglages compare l'horloge à l'**heure réseau standard** en temps réel ; si l'écart est grand, appuyez sur **Synchroniser** pour activer le réglage automatique de l'heure |
| Sans adb | Les réglages sont écrits dans `Settings.Secure` ; sans le droit `WRITE_SECURE_SETTINGS`, le module bascule automatiquement sur un diffusé traité par SystemUI — **aucune autorisation adb nécessaire** |
| Langues de l'interface | L'interface de l'application est traduite en **21 langues** (suit automatiquement la langue du système) : 简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, ไทย, Deutsch, Español, Français, Українська, Polski, Português (Brasil), فارسی, العربية, Bahasa Indonesia, Italiano, Ελληνικά, ქართული |
| Persistance | L'état est stocké dans `Settings.Secure`, partagé entre les processus et conservé après un redémarrage de SystemUI |
| Compatibilité | Vérifié sur ColorOS 15 / 16 / 17 (OPPO / OnePlus / realme) ; fonctionne aussi tel quel sur d'autres fabricants rootés comme Xiaomi / Honor / Redmi / Samsung. Aucune classe privée de fabricant, donc le mode sans échec de LSPosed n'est pas déclenché. Android 10 minimum |

## Installation

1. Téléchargez le dernier APK depuis la page [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest)
2. Activez le module dans **LSPosed**, portée : **Interface système (`com.android.systemui`)**
3. Redémarrez le processus SystemUI ou redémarrez l'appareil
4. Ouvrez la page de réglages depuis le **gestionnaire LSPosed** (aucune icône n'est créée sur le bureau)

> Nécessite root (LSPosed). Pour accorder `WRITE_SECURE_SETTINGS` afin d'écrire dans `Settings.Secure` :
> ```bash
> adb shell pm grant io.github.kelebao.colorosclockseconds android.permission.WRITE_SECURE_SETTINGS
> ```
> Sans cette permission, vous pouvez toujours basculer en appuyant sur l'heure de la barre d'état (cette action s'exécute dans le processus SystemUI, qui en a déjà le droit).

## Utilisation

- **Appuyez sur l'heure de la barre d'état** : active/désactive les secondes (geste par défaut).
- La bascule repose sur un test de touche globale, elle n'**entre donc jamais en conflit avec le « défilement du panneau de notifications »**—— un appui pur bascule, un balayage continue de dérouler le panneau. Si vous préférez, vous pouvez passer au geste **double appui** dans la page de réglages.
- La page de réglages affiche l'état actuel, le geste choisi et un bouton d'actualisation.

## Aperçu

<table><tr>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-off.jpg" alt="Secondes désactivées : `15:06` — le chiffre des dizaines de l'heure `1` reste rouge" width="100%">
<br>
Secondes désactivées : `15:06` — le chiffre des dizaines de l'heure `1` reste rouge
</td>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-on.jpg" alt="Secondes activées : `15:06:03` — minutes et secondes gardent la couleur système" width="100%">
<br>
Secondes activées : `15:06:03` — minutes et secondes gardent la couleur système
</td>
</tr></table>

## Comment ça marche

Le module n'accroche que **SystemUI**, en utilisant l'approche legacy Xposed bien rodée,
et **ne touche jamais aucune classe privée de fabricant** : il ne casse donc jamais l'état interne du système ni ne déclenche le mode sans échec de LSPosed :

- **Secondes** : accrochez le stable `TextView.setText` (after) pour mettre en cache le texte brut que le système pose sur l'heure de la barre d'état,
  puis accrochez `TextView.onDraw` (before) pour réécrire l'affichage en `texteBrut:ss` une fois que le système a posé le texte,
  piloté par un `Handler` d'une seconde. Les secondes ajoutées héritent de la couleur du texte de l'heure (mesurée `#ccffffff`).
  La dizaine de l'heure est en plus colorée en rouge (un `ForegroundColorSpan` rouge sur ce seul caractère), que les secondes soient affichées ou non ;
  les minutes et les secondes ne sont jamais rouges ; les heures à un seul chiffre n'ont pas de dizaine et restent intactes.
- **Bascule** : le panneau de notifications de ColorOS intercepte les touchers dans la zone de la barre d'état, si bien que l'`OnTouchListener` de l'heure elle-même ne reçoit jamais les événements.
  Le module accroche donc le `View.dispatchTouchEvent` global et ne fait basculer l'interrupteur `Settings.Secure` que lorsque l'appui tombe dans
  les limites à l'écran de l'heure reconnue, avec un déplacement quasi nul (appui) ou un second appui dans les 350 ms (double appui). Le module ne **consomme**
  aucun événement tactile : le défilement du panneau de notifications fonctionne donc comme d'habitude.

## Note sur les économies d'énergie

- **Secondes ACTIVÉES** : le module a besoin d'un minuteur d'une seconde pour piloter les rafraîchissements (le coût inhérent à l'affichage des secondes, environ 60 redessins de l'heure par minute).
- **Secondes DÉSACTIVÉES** : le module **arrête immédiatement ce minuteur** (`stopTicker`)—— plus aucun réveil chaque seconde, plus aucune lecture inter-processus.
  Il revient au rythme d'origine du système « un rafraîchissement par minute », donc désactiver les secondes économise vraiment de l'énergie.
- L'état est mis en **cache dans le processus** (`sSecondsEnabled`) : le minuteur ne lit pas `Settings.Secure` chaque seconde (un IPC par seconde est évité).
- Aucun formateur d'heure n'est construit : le module réutilise le texte brut du système et se contente d'ajouter `:ss`, donc aucun surcoût de formatage.
- Le minuteur ne détient qu'une **référence faible** sur la vue et ne continue que tant que la vue est `isAttachedToWindow()` ; une fois la vue détruite, il sort automatiquement.

## Compilation

Nécessite **JDK 17+** (17 ou 21 recommandés) et **Android SDK Platform 35** (Build-Tools 35 est installé automatiquement par AGP).

```bash
export JAVA_HOME=/path/to/jdk-17-or-21
./gradlew assembleDebug
```

## Sauvegarde et développement continu (important)

Dans le répertoire `ClockSeconds/`, **conservez tous les fichiers sauf `build/` et `.gradle/`** (la partie source ne pèse qu'environ 140 Ko) :

| Catégorie | Fichier | Remarque |
|------|------|------|
| Scripts de compilation | `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties` | Version, dépendances, paramètres du compilateur |
| Gradle Wrapper | `gradlew`, `gradlew.bat`, `gradle/wrapper/*` | Fixe Gradle 8.14.5 |
| Code source | `app/src/main/java/**` (3 fichiers `.java`) | Le corps de la logique du module |
| Ressources | `app/src/main/res/**` | Chaînes (chinois par défaut + 21 langues values-xx), thème |
| Métadonnées Xposed | `app/src/main/resources/META-INF/xposed/*` | Classe d'entrée, portée, version d'API |
| Manifeste | `app/src/main/AndroidManifest.xml` | Permissions, Activity |
| Documentation | `README*.md` | Documentation en 21 langues |
| CI | `.github/workflows/release.yml` | Une étiquette produit une compilation signée |

## Dépannage

<details>
<summary>Déplier les étapes de dépannage</summary>

Consultez le logcat :

- Les **compilations Release** retirent tous les journaux de diagnostic via la porte `BuildConfig.DEBUG` (plus léger, plus propre) ; rien de ce module n'apparaît dans le logcat.
- Pour investiguer, installez temporairement la **compilation debug** (`assembleDebug`, signée avec la même clé de production, donc installable par-dessus la Release) puis filtrez par TAG :

  ```bash
  adb logcat -s ColorOSClockSeconds
  ```

- Si aucune seconde ne s'affiche : vérifiez d'abord que LSPosed a bien la portée SystemUI cochée et un redémarrage effectué ; puis vérifiez si l'interrupteur a été persisté comme désactivé——
  à lire / forcer via adb :

  ```bash
  adb shell settings get secure coloros_clock_seconds_enabled   # 1=activé 0=désactivé
  adb shell settings put  secure coloros_clock_seconds_enabled 1
  ```

- Si rien ne fonctionne sur un appareil non ColorOS : installez la compilation debug et regardez les lignes `candidate:` / `CONFIG CLOCK:` du journal
  pour voir si le nom de ressource / le nom de classe de l'heure de la barre d'état de votre appareil est couvert par les règles de reconnaissance ; sinon, une simple ligne de règle en plus suffit.

</details>

## Retours

Des questions, suggestions ou remarques ? Laissez un message sur [GitHub Issues](https://github.com/kelebao/ClockSeconds/issues), je réponds dès que possible.

## License

[MIT](LICENSE) © kelebao
