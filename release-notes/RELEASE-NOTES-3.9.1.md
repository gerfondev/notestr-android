# Notestr 3.9.1 — Android et Linux

## Nouveautés et corrections

- Ajout du verrouillage/déverrouillage synchronisé d’une note, compatible entre Android et Linux. Une note verrouillée reste lisible et ne peut pas être modifiée ou supprimée depuis une version compatible.
- Correction de bugs mineurs.
- Mises à jour de sécurité et de maintenance : Kotlin 2.4.21, AndroidX Activity 1.13.0 et Fragment 1.9.1 sur Android ; aiohttp 3.14.5, cryptography 50.0.2 et websockets 17.2 sur Linux.
- Correction de l’export PDF Linux : les images de la note sont intégrées au document après déchiffrement local. Les images manquantes interrompent l’export avec un message explicite.
- Numéros de version Android et Linux alignés sur 3.9.1. Les nouveautés d’images, de zoom et la licence GPLv3 de la version 3.9 sont conservées.

Android versionCode 46. APK de mise à jour, à installer par-dessus la version existante sans désinstaller l’application. Linux x86_64 : AppImage, wheel Python et archive source. Voir `SECURITY-REVIEW.md` pour les contrôles et leurs limites.
