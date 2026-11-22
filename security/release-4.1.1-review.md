# Revue de sécurité et confidentialité — Notestr Android 4.1.1 — 22 novembre 2026

## Paquet candidat

- APK : `Notestr-Android-4.1.1.apk`, versionName `4.1.1`, versionCode `88`, compile/target SDK `36`, minSdk `26`.
- SHA-256 : `c514e35c6d21b98c730287c121fbbaa5d0a39e96aff0de77d065d592a3b26ab0`.
- `assembleRelease` et `lintVitalRelease` réussis. Signature APK v2, alignement ZIP, `debuggable=false` et certificat identique à la 4.1 publique contrôlés.
- Tests unitaires/instrumentés non relancés pour ce candidat. L’utilisateur confirme avoir testé l’APK 4.1.1 avant publication ; les scénarios précis ne sont pas consignés.

## Dépendances et composants

- Gradle et son wrapper JAR restent sur la version corrigée `8.14.7`, déjà vérifiée avec les empreintes officielles lors de la revue 4.1.
- DOMPurify embarqué reste en version corrigée `3.4.17`. AGP, Kotlin, Coroutines, AndroidX/Compose et ProseMirror restent sur les versions compatibles de la revue précédente ; aucune modification de ces dépendances n’a été apportée pour 4.1.1.
- Le SDK Nostr Rust demeure un binaire natif précompilé. `cargo-audit` est absent ; aucune revue Rust exhaustive n’est revendiquée.

## Sources et confidentialité

- Le code de version et la mise en couleur des boutons Réglages sont inclus.
- Scan ciblé de 260 fichiers source et 506 entrées APK pour les chemins personnels, `nsec`, jetons d’accès, caches, configurations locales et marqueurs personnels connus : aucun marqueur recherché détecté. Il ne s’agit pas d’une garantie exhaustive.
- L’APK ne contient pas `local.properties`, fichier de signature ni cache utilisateur.

## Publication

L’utilisateur a confirmé le 22 novembre 2026 avoir testé les candidats Android et Linux 4.1.1. Aucun push, tag distant, release ni téléversement n’a encore été effectué au moment de cette revue.
