# Revue de sécurité et confidentialité — Notestr Android 4.1 — 14 novembre 2026

## Paquet candidat

- APK `Notestr-Android-4.1.apk`, versionName `4.1`, versionCode `85`, compile/target SDK `36`, minSdk `26`.
- SHA-256 : `8de3c415a8b0e5cd835e5bb9db9277739575b2269ce9eb68d7111a47540100bc`.
- Signature APK v2 valide, alignement ZIP vérifié, `debuggable=false`. Certificat conservé pour permettre une mise à jour sans désinstallation.
- `./gradlew clean testReleaseUnitTest assembleRelease lintVitalRelease` réussi avec 25 tests unitaires release. Aucune exécution sur téléphone/émulateur dans cet environnement ; l’APK final doit être testé avant publication.

## Dépendances et outils

- Wrapper Gradle et JAR mis à jour de 8.14.6 vers 8.14.7. Les avis Gradle publiés en octobre 2026 et le nouvel avis du 10 novembre sont corrigés à partir de 8.14.7. Archive `gradle-8.14.7-bin.zip` vérifiée contre le SHA-256 officiel `903bfc4bc7622323124054c10f509246212a1902948bdaf26c379cca8735e856` ; JAR du wrapper vérifié contre `ad16bca46cb71bb5887c546860d93992aeee35c1fdaf4a242b0a612375d937e0`.
- DOMPurify embarqué `3.4.17` ; ProseMirror `1.42.6`. Les empreintes de DOMPurify, du SDK natif Nostr et de JNA sont contrôlées par le projet.
- AGP `8.13.2`, Kotlin `2.4.21`, Coroutines `1.11.0` et bibliothèques AndroidX/Compose restent sur les versions compatibles retenues lors de la revue précédente. Aucun avis affectant ces versions n’a été identifié dans les sources consultées. Le SDK Nostr Rust est un binaire précompilé localement ; `cargo-audit` n’est pas installé, donc aucun audit Rust exhaustif n’est revendiqué.

## Confidentialité

- Scan ciblé de 256 fichiers source et 506 entrées APK : aucun chemin local personnel complet, nsec complète, npub fourni, jeton GitHub/Bearer ou bloc de clé PEM trouvé.
- Les identités auteur/committer présentes dans l’historique local sont des identités génériques du projet. Les sources ne sont pas poussées avec ce candidat ; aucun historique distant n’est réécrit.
- Contrôle ciblé, non exhaustif : il ne peut pas garantir l’absence de donnée personnelle inconnue ni remplacer un audit natif/Rust complet.

## Publication et vérification publique

- L’utilisateur a autorisé explicitement la publication le 15 novembre 2026.
- GitHub : release [v4.1](https://github.com/gerfondev/notestr-android/releases/tag/v4.1), commit `1bbdf1d`, tag `v4.1`.
- APK et fichier SHA-256 téléchargés depuis les URLs publiques après publication. L’APK public est identique au candidat contrôlé : SHA-256 `8de3c415a8b0e5cd835e5bb9db9277739575b2269ce9eb68d7111a47540100bc`.
- Le commit et le tag ont été ajoutés sans réécriture de l’historique existant.
