# Notestr Android

> **Version 3.9.5** — revue de sécurité des dépendances et de l’outillage Android ; versionCode 71. [Notes de version 3.9.5](RELEASE-NOTES-3.9.5.md).

Application Android native de notes privées chiffrées sur Nostr, compatible avec Notestr Linux. Langues disponibles : français, anglais.

## Télécharger et installer — aucune compilation nécessaire

**[Télécharger Notestr Android — dernière version — APK complet](https://github.com/gerfondev/notestr-android/releases/latest/download/Notestr-Android-3.9.5.apk)**

[Voir la dernière version](https://github.com/gerfondev/notestr-android/releases/latest) · Version actuelle : **3.9.5** (versionCode 71).

Ouvrir ce lien depuis le téléphone, télécharger le fichier APK puis l’ouvrir pour installer l’application. Autoriser l’installation depuis cette source lorsque Android le demande. Aucun outil de développement ni compilation n’est nécessaire.

Pour mettre à jour une installation existante, installer cet APK par-dessus sans désinstaller Notestr, afin de conserver le coffre et les réglages.

SHA-256 : voir `SHA256SUMS-3.9.5-Android.txt` dans les fichiers de la release.

## Version 3.9.5

- Revue de sécurité des dépendances et de l’outillage Android.
- Conservation des versions Compose/Lifecycle compatibles avec la base Android qualifiée.
- Version Android alignée sur Linux : 3.9.5 (versionCode 71). [Notes de version](RELEASE-NOTES-3.9.5.md).

Les notes des versions précédentes sont conservées dans [`release-notes/`](release-notes/).

## Fonctions

- lecture et déchiffrement des événements `kind 33457` de l’utilisateur ;
- chiffrement NIP-44 v2 de soi vers soi avec `rust-nostr` ;
- création et modification en conservant le tag `d` de six caractères alphanumériques minuscules ;
- suppression par événement `kind 5` selon NIP-09 ;
- relais configurables, avec `wss://relay.decentralia.fr` par défaut ;
- éditeur Markdown visuel local et mode source ;
- retours simples rendus explicites à la publication pour préserver la mise en forme ;
- verrouillage par mot de passe et changement de mot de passe ;
- clé privée protégée par le mot de passe puis par Android Keystore ;
- connexion à Amber selon NIP-55 : la clé privée reste dans le signer ;
- cache local limité aux événements Nostr signés : le contenu des notes y reste chiffré NIP-44 ;
- verrouillage automatique après trois minutes en arrière-plan et captures d’écran bloquées.

Les images Markdown `![description](https://…)` sont affichées dans le mode Visuel et adaptées à la largeur de l’écran. Le chargement se fait directement depuis leur serveur HTTPS. Les URL HTTP et les fichiers locaux ne sont pas chargés. Une URL seule reste un lien : utiliser la syntaxe Markdown image. L’envoi de pièces jointes n’est pas pris en charge.

Cette application lit les documents personnels kind `33457`, pas les articles kind `30023` ni leurs tags `image`.

Le mode Visuel utilise une hauteur liée à la zone Android disponible. Son chargement affiche une progression ; en cas d’échec, un diagnostic et un bouton Réessayer apparaissent. Le retour au Markdown conserve la source même si le moteur visuel n’a pas démarré.

## Titres et mise en forme

Le bouton H ouvre un menu visible sur mobile : En-tête 1 à En-tête 6, puis Paragraphe. Les titres de la liste Android retirent les marqueurs usuels de mise en forme.

À la publication, une première ligne simple entièrement en gras/italique (par exemple `***Test version Android***`) est convertie en vrai titre H1 (`# Test version Android`). Cette conversion remplace le gras/italique de cette première ligne par le style H1. Le corps de la note et les premières lignes complexes restent inchangés par cette conversion. Les sauts de ligne continuent d’être normalisés comme auparavant.

Pour appliquer cette conversion au titre d’une note déjà publiée, ouvrir cette note dans Android puis la republier. Aucune ancienne note n’est republiée automatiquement.

## Installer l’APK fourni

Utiliser le lien **Télécharger Notestr Android** en haut de ce README pour obtenir l’APK prêt à installer sur Android 8 ou plus récent. Le code source et les instructions de compilation ci-dessous sont destinés aux personnes qui souhaitent développer l’application.

Cet APK est signé avec une clé de débogage locale. Une publication dans un magasin d’applications exige une clé de signature de production conservée hors du dépôt.

## Construire avec Android Studio

1. Installer Android Studio avec le SDK Android 36 et Java 17.
2. Ouvrir le dossier `~/Documents/Dev/Android/notestr-android`.
3. Laisser Gradle synchroniser les dépendances.
4. Utiliser **Build > Build APK(s)**.

Le résultat est créé dans `app/build/outputs/apk/debug/app-debug.apk`.

## Construire en ligne de commande

Avec `JAVA_HOME` et `ANDROID_HOME` configurés :

```sh
cd ~/Documents/Dev/Android/notestr-android
./gradlew testDebugUnitTest assembleDebug
```

## Première ouverture

1. Choisir **Clé locale** ou **Amber**.
2. Avec une clé locale, saisir la clé `nsec` du compte Nostr qui contient vos notes. Avec Amber, installer et déverrouiller Amber, puis sélectionner le compte voulu.
3. Choisir un mot de passe d’au moins huit caractères.
4. Vérifier la liste des relais.
5. Créer le coffre ou se connecter avec Amber, puis attendre la première synchronisation.

Lors de la connexion Amber, autoriser et mémoriser les quatre opérations demandées : signature des événements `33457` et `5`, chiffrement NIP-44 et déchiffrement NIP-44. Notestr conserve seulement la clé publique et le nom technique du signer dans son coffre local. La clé privée reste dans Amber.

Après une mise à jour depuis une version déjà configurée, ouvrir **Réglages > Changer de compte ou utiliser Amber** pour revenir à l’écran de choix. Cette opération efface uniquement le coffre et le cache locaux ; elle ne supprime aucune note des relais.

Le mot de passe n’est envoyé à aucun service. En mode local, sa perte rend la clé stockée illisible ; conserver une sauvegarde sûre de la clé `nsec` en dehors de l’application. En mode Amber, il est possible de réinstaller Notestr puis de reconnecter le même compte Amber.

## Déverrouillage biométrique

1. Enregistrer une empreinte ou un visage compatible avec la biométrie forte dans les réglages Android.
2. Ouvrir Notestr avec son mot de passe habituel.
3. Aller dans **Réglages > Activer la biométrie**, puis confirmer dans le dialogue Android.
4. Aux ouvertures suivantes, utiliser **Déverrouiller par biométrie**. Le bouton de déverrouillage par mot de passe reste disponible.

La biométrie est facultative, désactivée par défaut et peut être désactivée dans les réglages. L’application ne reçoit aucune empreinte ni image du visage : Android autorise une opération cryptographique dans Keystore. Seuls les capteurs acceptés par Android pour la biométrie forte sont utilisés.

Le changement de mot de passe ou la reconfiguration du compte supprime l’accès biométrique précédent. Un changement des empreintes enregistrées peut également invalider la clé biométrique : utiliser alors le mot de passe et réactiver l’option. L’absence de capteur compatible ou l’annulation du dialogue n’empêche pas le déverrouillage par mot de passe.

Avec Amber, la biométrie ouvre le coffre local de Notestr ; elle ne remplace pas les autorisations d’Amber. Le parcours de connexion Amber reste celui de la version précédente.

## Confidentialité du dépôt

Le code, les ressources de l’éditeur et la configuration par défaut ne contiennent ni clé privée ni note personnelle. Les secrets, réglages et événements mis en cache sont créés dans l’espace privé de l’application au moment de son utilisation et ne font pas partie du dépôt.

Avant toute publication du code, vérifier malgré tout les fichiers suivis et ne jamais ajouter `local.properties`, un fichier de signature, une clé `nsec`, un export de données ou le contenu du répertoire privé de l’application.

## Dépendances principales

- Jetpack Compose / Material 3 ;
- `org.rust-nostr:nostr-sdk:0.44.8` pour les clés, signatures, événements, relais et NIP-44 ;
- TOAST UI Editor et DOMPurify, chargés uniquement depuis les ressources locales. Leurs licences sont fournies dans `app/src/main/assets/editor/`.

Le SDK Nostr fournit une implémentation du chiffrement NIP-44 v2. Son interface Kotlin est encore annoncée comme alpha par le projet, même si le cœur cryptographique Rust est partagé avec les autres liaisons officielles.

## Modifications hors connexion

Le bouton Publier enregistre une modification chiffrée sur cet appareil avant de tenter sa synchronisation. Les modifications en attente persistent au redémarrage. La reprise nécessite une application ouverte et déverrouillée ; les conflits conservent les deux versions. Voir [OFFLINE.md](OFFLINE.md) pour le fonctionnement, les limites et le protocole de test.
