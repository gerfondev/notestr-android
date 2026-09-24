# Notestr Android 1.2.5

## Nouveautés

- Sauvegarde automatique de la version précédente lors de la publication d’une note modifiée : une seule sauvegarde chiffrée par note, conservée en local et envoyée aux relais avant la mise à jour.
- Nouvelle action **Version précédente** dans l’éditeur : charger la sauvegarde après confirmation, puis **Publier** pour valider la restauration. La version remplacée devient alors la sauvegarde.
- Sauvegardes compatibles avec Notestr Linux, avec le même compte et un relais commun acceptant les sauvegardes. Fonctionnement prévu avec clé locale et Amber.

## Fiabilité et sécurité

- Une mise à jour n’est envoyée qu’aux relais ayant accepté sa sauvegarde ; un refus est signalé.
- Cache local fusionné, vérifié et écrit atomiquement ; une réponse vide des relais ne fait plus perdre les événements conservés.
- Gestion cohérente des identifiants historiques, des dates identiques et de la suppression de la note et de sa sauvegarde.
- Mise à jour du SDK Nostr et reconstruction de ses bibliothèques natives avec les dépendances de sécurité corrigées.
- Mise à jour de DOMPurify vers 3.4.16, de JNA, des coroutines, d’AppCompat, de Gradle et de dépendances transitives des outils de compilation.
- Contrôles renouvelés des sources, dépendances, métadonnées, historique Git et contenu de l’APK. Résultats et limites dans SECURITY-REVIEW.md.

APK unique pour Android 8 ou plus récent. Installer la mise à jour **par-dessus la version actuelle, sans désinstaller l’application**, pour conserver le coffre et les réglages. VersionCode 18, y compris pour remplacer le candidat de test.

Le candidat de sauvegarde a été testé sur smartphone. Les essais automatisés utilisent des clés synthétiques et un relais local ; Amber réel et tous les modèles de téléphone ne sont pas couverts.

[Télécharger Notestr-Android.apk](https://github.com/gerfondev/notestr-android/releases/download/v1.2.5/Notestr-Android.apk)

SHA-256 : `d1242c65ed785f54203f3444110839ed9e08055aafbd19b8ead1e9ef74e20182`.
