# Notestr Android 3.7

## Nouveautés et corrections

- **Cache des notes** : les notes en cache s’affichent avant la réponse des relais et restent consultables pendant la synchronisation. Le cache disque reste chiffré ; les résultats déchiffrés en mémoire sont effacés au verrouillage.
- **Liens corrigés** : les liens HTTP/HTTPS de l’éditeur s’ouvrent dans le navigateur. Une confirmation protège les modifications non publiées lors du passage au navigateur. Curseur main au survol avec une souris.
- **Numéro de version** visible dans les paramètres.
- **Français ou anglais** au choix dans les paramètres. Le changement est immédiat et mémorisé ; le contenu des notes reste inchangé.

## Sécurité et confidentialité

Contrôle renouvelé des dépendances Android, JavaScript, Rust natif et outils de compilation : aucune nouvelle alerte retournée par OSV pour les versions inventoriées. Le SDK Nostr corrigé et DOMPurify 3.4.16 sont conservés. TOAST UI reste archivé, avec une migration à prévoir. La version du WebView dépend des mises à jour système Android.

14 tests unitaires et 28 tests instrumentés réussis sur l’APK final. APK non débogable, signature et certificat conservés, alignement 16 Ko vérifié. Contrôles ciblés des sources, images et du contenu décompressé sans marqueur personnel recherché détecté ; ces vérifications ne constituent pas un audit exhaustif. Les anciennes métadonnées d’auteur déjà publiques restent dans l’historique, qui n’est pas réécrit.

## Installation

Installer **Notestr-Android.apk** par-dessus la version précédente, sans désinstaller l’application. Version 3.7, versionCode 26. Un fichier SHA-256 accompagne l’APK.
