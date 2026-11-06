# Revue sécurité et confidentialité — Notestr Android 4.0.1 — 6 novembre 2026

## Paquet Android

- Candidat : `Notestr-Android-4.0.1.apk`, versionName `4.0.1`, versionCode `78`, minSdk 26, target/compileSdk 36.
- SHA-256 : `85f8d64315312d2bea43b9210bd4fcf5f26913d4f26837c5f4f34438c7162265`.
- Signature APK v2 valide ; certificat Android Debug conservé pour rester compatible avec les installations existantes. L’APK est non débogable et aligné ZIP.
- APK : 506 entrées. `aapt` confirme le nom de paquet, la version et les permissions attendues.
- `./gradlew clean testReleaseUnitTest assembleRelease` réussi : 25 tests unitaires, `lintVitalRelease`, compilation et empaquetage réussis. Les avertissements de compilation restants sont deux appels Kotlin redondants et une API tooltip dépréciée ; aucun n’a bloqué le build.
- La compilation a vérifié les empreintes SHA-256 de l’AAR Nostr et de JNA. L’empreinte de la ressource DOMPurify locale est contrôlée par `app/src/main/assets/editor/SHA256SUMS`.
- Pas d’appareil ni d’émulateur accessible pendant cette vérification : aucun lancement réel de l’APK final n’a été effectué. La recherche et le lancement biométrique ont été essayés sur l’appareil avant cette mise à jour de sécurité ; la validation de ce paquet stable précis reste nécessaire avant publication.

## Dépendances et mises à jour de sécurité

- DOMPurify embarqué mis à jour de 3.4.16 vers 3.4.17. L’avis GitHub `GHSA-cg2g-xh64-v4q7` qualifie de critique une vulnérabilité de contournement de `SAFE_FOR_TEMPLATES`, affectant les versions 3.4.4 à 3.4.16 et corrigée en 3.4.17. La version 3.4.17 a été obtenue depuis le registre npm officiel ; son intégrité SHA-512 publiée (`B/pSlYVHQ58kqISD/yDjuJ1yfzI34m1gF4lAod2bwSRhiN4K5Cl3H8Af7hqB2PmTL911tdEudnnXn5FnhRoz0w==`) a été recalculée sur l’archive téléchargée. SHA-256 du fichier embarqué : `cd37898f9782adf54820c413191b5691803f0f137c799d6edcfd38523b4c2c31`.
- ProseMirror `1.42.6` reste supérieur à la version corrigée `1.42.3` de l’avis `GHSA-c8x8-7fp4-3x9w`. Le dépôt TOAST UI Editor intégré est archivé ; le bundle reste reconstruit avec les composants corrigés documentés dans `NOTESTR-PATCHES.md`.
- Graphe runtime Gradle release résolu et examiné. Gradle `8.14.6` inclut les correctifs aux avis d’octobre 2026 documentés par Gradle ; AGP `8.13.2`, Kotlin `2.4.21`, Coroutines `1.11.0`, AndroidX Biometric `1.1.0`, Activity `1.13.0`, Lifecycle `2.10.0`, AppCompat `1.8.0`, Compose UI `1.11.3` et Material 3 `1.4.0` sont conservés sur la base compatible avec SDK 36. Les surcharges de sécurité déjà présentes pour Netty, BouncyCastle, Commons Compress, jose4j et JDOM2 restent actives.
- Les versions AndroidX/Compose qui demanderaient une migration majeure de l’outillage/SDK n’ont pas été montées sans avis de sécurité visant les versions embarquées. Le binaire Nostr Rust demeure une compilation locale personnalisée ; Cargo et `cargo-audit` ne sont pas installés dans cet environnement, donc aucun audit Rust par cet outil n’est revendiqué. Les empreintes Nostr SDK et JNA ont été validées par les tâches Gradle `verifyNostrSdk` et `verifyJna`.

Sources primaires consultées : [avis critique DOMPurify](https://github.com/advisories/GHSA-cg2g-xh64-v4q7), [releases DOMPurify](https://github.com/cure53/DOMPurify/releases), [avis ProseMirror](https://github.com/ProseMirror/prosemirror-view/security/advisories/GHSA-c8x8-7fp4-3x9w), [avis Gradle](https://github.com/gradle/gradle/security/advisories), [compatibilité AGP](https://developer.android.com/build/releases/about-agp), [releases AndroidX](https://developer.android.com/jetpack/androidx/versions), [releases Kotlin](https://github.com/JetBrains/kotlin/releases), [releases Coroutines](https://github.com/Kotlin/kotlinx.coroutines/releases).

## Sources, paquet et données personnelles

- Scan ciblé du code suivi et des fichiers de publication : chemins personnels locaux, clés Nostr privées complètes, clés PEM privées, jetons GitHub et jetons Bearer. Aucun marqueur de ces catégories n’a été trouvé. Aucun npub complet n’a été détecté dans les sources contrôlées ou l’APK.
- Même scan effectué sur les 506 entrées extraites logiquement de l’APK : aucun chemin local, clé privée complète ou jeton recherché n’a été détecté. Les marqueurs ressemblant à des adresses dans le binaire sont dans les ressources et dépendances tierces (éditeurs, SDK natif) ou les données de test synthétiques ; ils ne correspondent pas à l’identité du propriétaire. Les correspondances dans l’image PNG du dépôt proviennent des octets de pixels et non de métadonnées texte ; aucun chunk de texte PNG n’a été trouvé.
- Les chemins absolus du poste ont été retirés des six anciens rapports JSON de dépendances suivis par Git ; les données de versions et d’avis restent intactes.
- Les rapports de dépendances vérifient des composants tiers et contiennent leurs métadonnées d’origine ; les identités auteur/committer de l’historique Git antérieur sont conservées et ne sont pas réécrites. Elles ne sont pas intégrées à l’APK.
- Contrôle ciblé, non exhaustif : il ne permet pas d’affirmer qu’aucune donnée personnelle inconnue n’existe. Il n’y a pas eu d’audit spécialisé de toutes les chaînes de dépendances native/Rust.

Le candidat local et ses empreintes sont prêts pour essai. Aucun push, tag distant, release ou téléversement n’a été effectué dans cette vérification.
