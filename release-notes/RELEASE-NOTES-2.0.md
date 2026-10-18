# Notestr Android 2.0

## Sauvegarde de la note lors d’une modification

- Lors de la publication d’une note modifiée, sa version précédente est sauvegardée automatiquement.
- Une seule sauvegarde chiffrée est conservée par note, en local et sur les relais qui l’acceptent. La sauvegarde est envoyée avant la note modifiée.
- L’action **Version précédente** permet de charger cette sauvegarde après confirmation. Appuyer ensuite sur **Publier** pour confirmer la restauration ; la version remplacée devient la nouvelle sauvegarde.
- Format de sauvegarde partagé avec Notestr Linux. Le cache est conservé en cas de réponse vide des relais et les refus de sauvegarde sont signalés.

## Menus à icônes

- Les actions **Actualiser, Réglages, Verrouiller, Retour, Version précédente, Supprimer et Publier** utilisent des icônes.
- La création d’une note est représentée par une icône **+**.
- Un appui long sur les icônes de menu affiche leur fonction ; leurs descriptions restent disponibles pour les lecteurs d’écran.
- Cibles tactiles de 48 dp et version précédente intégrée à la barre de l’éditeur pour laisser davantage de place au texte.
- Confirmations explicites conservées pour supprimer une note ou charger sa sauvegarde.

## Sécurité et vérifications

- Correctifs de sécurité du SDK Nostr et de ses composants natifs, DOMPurify 3.4.16, JNA et outils de compilation inclus ; dépendances réexaminées avant publication.
- Sources, paquets, métadonnées et contenu de l’APK vérifiés. Résultats, limites et réserve sur des métadonnées de deux commits historiques dans SECURITY-REVIEW.md.
- Interface et documentation en français.

APK unique pour Android 8 ou plus récent. Installer **par-dessus la version actuelle, sans désinstaller**, pour conserver le coffre et les réglages. VersionCode 20, compatible avec la mise à jour depuis le candidat à icônes.

[Télécharger Notestr-Android.apk](https://github.com/gerfondev/notestr-android/releases/download/v2.0/Notestr-Android.apk)

SHA-256 : `e4f8717281f4acb6c303e6120aff6c658b7e1f4eaa9b32745f8af0800c12cfe5`.
