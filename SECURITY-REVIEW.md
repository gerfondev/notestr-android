# Publication 2.0 — contrôle du 24 septembre 2026

Publication 2.0 explicitement autorisée par l’utilisateur après remise du candidat à icônes. VersionCode 20. Contrôle des dépendances renouvelé : 658 requêtes OSV sans avis retourné ; métadonnées Maven, Rust et JavaScript vérifiées sans erreur de collecte. Les versions corrigées et exceptions de maintenance restent celles de 1.2.5 : SDK reconstruit, JNA aux métadonnées nettoyées, DOMPurify 3.4.16 et outils de compilation corrigés. Aucune correction supplémentaire identifiée par ce contrôle. TOAST UI reste archivé ; les limites de l’inventaire JavaScript et des versions d’outillage conservées sont documentées ci-dessous.

Confidentialité : les deux nouveaux commits déjà présents sur GitHub avant cette intervention contiennent une adresse e-mail d’auteur. Cette donnée historique n’est pas reproduite dans le rapport et aucun historique distant n’est réécrit. Le nouveau commit et le tag utilisent l’identité neutre du projet. Sources distribuées, archives imbriquées, images et APK inspectés : aucun marqueur personnel recherché détecté dans ces fichiers, y compris l’adresse identifiée dans les commits historiques. L’archive complète d’exemples du SDK demeure exclue du dépôt et de l’APK.

La présentation actuelle ne contient plus de référence à l’autre application ; la normalisation des titres garde son comportement avec un nom de fonction générique. Notes de version centrées sur les sauvegardes lors de la publication d’une modification et les actions à icônes.

Validation finale : 13 tests JVM et 17 tests instrumentés sur l’APK release 2.0 réussis. Sauvegardes, refus de relais, restauration via icônes, format Linux, NIP-44, Amber simulé, coffre, clavier et Retour vérifiés. Les essais réels sur téléphone et Amber ne couvrent pas tous les cas ; TalkBack n’a pas été testé manuellement.

APK non débogable, versionCode 20, certificat existant et signature v2 vérifiés. 489 entrées décompressées contrôlées sans secret ou chemin personnel correspondant aux motifs recherchés. Empreintes du SDK natif, JNA et DOMPurify conformes ; alignement 16 Ko des bibliothèques 64 bits vérifié. SHA-256 : `e4f8717281f4acb6c303e6120aff6c658b7e1f4eaa9b32745f8af0800c12cfe5`. Rapports `security/release-2.0-*.json`. Les résultats ci-dessous concernent les versions précédentes et ne constituent pas une garantie exhaustive de sécurité ou de confidentialité.

---

# Candidat local à icônes — 1.2.5-test.19, 24 septembre 2026

Publication interdite pour ce changement. Actions de menu remplacées par des icônes vectorielles locales avec description accessible, infobulle et cible tactile de 48 dp ; confirmations conservées. Aucune dépendance ajoutée. Versions, correctifs et exceptions documentés pour 1.2.5 conservés.

Avant compilation : contrôle OSV renouvelé sur 658 coordonnées, aucun avis retourné ; versions officielles et maintenance reconsultées. Trois formats de version Cargo ont nécessité de conserver le résultat du contrôle complet effectué plus tôt le même jour ; détail explicite dans `security/icons-test-19-dependencies.json`. TOAST UI archivé et limites de l’inventaire JavaScript restent documentés ci-dessous. Ressources graphiques vectorielles sans métadonnées personnelles, licence Material incluse.

Validation terminée : 13 tests JVM et 3 tests UI sur l’APK release réussis. Restauration/publication par les icônes, clavier et appui tactile sur Retour sous la barre système vérifiés. Les noms des icônes restent accessibles aux lecteurs d’écran ; l’usage réel de TalkBack et le rendu sur smartphone ne sont pas validés manuellement.

APK non débogable, versionCode 19, certificat inchangé ; 489 entrées décompressées inspectées sans secret ou chemin personnel correspondant aux motifs recherchés. Bibliothèques natives et DOMPurify identiques aux fichiers vérifiés de 1.2.5 ; signature et empreinte contrôlées. SHA-256 : `c09322ae18b0d323530a269dac2c2c94d60fe3e5cd9ca4b373a674b578edc537`. Résultats dans `security/icons-test-19-*.json`. Ce contrôle ciblé ne garantit pas l’absence de vulnérabilité inconnue ou donnée encodée. La copie de publication reste inchangée.

---

# Publication 1.2.5 — contrôle du 24 septembre 2026

L’utilisateur a testé le candidat 1.2.4-test.17 sur smartphone, indiqué qu’il fonctionne et autorisé explicitement la publication sous 1.2.5. Numéro technique 18 pour remplacer le candidat local. La version finale ajoute DOMPurify 3.4.16, dont l’intégrité npm est vérifiée et les tests de filtrage sont rejoués.

Audit renouvelé : 658 coordonnées interrogées auprès d’OSV, sans alerte retournée ; métadonnées Maven, registre Cargo officiel et npm consultés pour les dépendances directes, transitives et le graphe natif verrouillé. Détails dans `security/release-1.2.5-dependencies.json`. La composition JavaScript minifiée reste une reconstruction du graphe amont, pas une preuve d’identité de chaque module ; le verrou Cargo comprend aussi des composants optionnels. Le SDK natif corrigé, son empreinte et les 522 vérifications d’interface sont conservés du candidat testé.

DOMPurify 3.4.16 remplace 3.4.15. SDK Nostr 0.45.1 reconstruit avec rustls 0.23.45 et les autres corrections détaillées ci-dessous ; JNA 5.19.1, coroutines 1.11.0, AppCompat 1.8.0 et Gradle 8.14.5. Correctifs de sécurité transitifs de l’outillage conservés. Aucune mise à jour majeure supplémentaire n’est présentée comme nécessaire en l’absence d’avis identifié ; les exceptions de versions AGP/AndroidX/Rust restent celles du contrôle précédent.

Maintenance : SDK Nostr et DOMPurify actifs ; TOAST UI Editor archivé, conservé pour sa compatibilité Markdown, avec ancien filtre embarqué retiré et filtre maintenu externe. Sa migration reste une action de maintenance à prévoir. Le fournisseur qualifie encore les bindings du SDK d’API en évolution ; la version publiée sans suffixe de préversion et son interface vérifiée sont épinglées.

Confidentialité avant compilation : sources applicatives, AAR et archives imbriquées inspectés ; aucun marqueur personnel ciblé détecté. L’archive source amont contient des clés dans ses exemples : elle est exclue des fichiers publiés, téléchargée à la demande et vérifiée par empreinte pour reconstruire le SDK. Les clés de nos tests sont les scalaires synthétiques publics 1 et 2, identifiés comme tels. L’historique existant et ses auteurs/committers ont été vérifiés : identité de projet uniquement, aucun marqueur ciblé détecté. Aucun cache, journal local, base privée ou clé de signature ne sera publié.

JNA : quatre noms de version ELF de base de l’AAR officiel contenaient un chemin personnel du fournisseur. Une copie locale vérifiée remplace ces seules métadonnées par un chemin neutre et actualise leur hash ELF. Sections exécutables et classes JVM inchangées, transformations reproductibles et empreintes dans `vendor/jna`. Les essais sont renouvelés après cette correction.

Contrôles finaux réussis : 13 tests JVM, 17 tests Android sur l’APK release et 5 tests isolés d’éditeur en debug. Filtrage HTML 3.4.16 vérifié sur les chemins personnalisés et par défaut. Lecture Linux de la fixture Android finale confirmée. Rapport `security/release-1.2.5-tests.json`.

APK final non débogable, versionCode 18, signature v2 valide et certificat des versions précédentes conservé. 480 entrées APK inspectées : aucun marqueur de secret ou chemin personnel recherché détecté ; bibliothèques natives comparées aux empreintes contrôlées, alignement 16 Ko des bibliothèques 64 bits vérifié. SHA-256 : `d1242c65ed785f54203f3444110839ed9e08055aafbd19b8ead1e9ef74e20182`. Rapport `security/release-1.2.5-apk.json`.

Fichiers de publication, archives décompressées et métadonnées d’images examinés ; historique existant inspecté sans correspondance aux motifs recherchés. Identités auteur/committer de projet uniquement. Rapports `security/release-1.2.5-source-privacy.json` et `security/release-1.2.5-history-privacy.json`. Les modifications de la version finale n’ont pas encore été testées séparément sur le téléphone ; elles sont couvertes par les essais automatisés renouvelés, dans les limites indiquées. L’absence d’alerte dans les bases interrogées et de correspondance aux motifs de confidentialité n’est pas une garantie exhaustive. Les sections suivantes conservent les résultats et limites des contrôles antérieurs.

---

# Contrôle du 24 septembre 2026 — SDK corrigé, candidat Android local

Statut : candidat local 1.2.4-test.17 contrôlé et prêt pour le test smartphone ; aucune publication autorisée.

Ce contrôle remplace le statut bloquant du 23 septembre, conservé ci-dessous comme historique. Le registre officiel a changé de groupe : `org.nostrdevkit:nostr-sdk:0.45.1`. L’ancienne recherche dans `org.rust-nostr` ne permettait pas de conclure sur la dernière version du SDK.

- SDK 0.45.1 reconstruit depuis le commit amont identifié dans `vendor/nostr-sdk/provenance.json`, avec verrou Cargo mis à jour et patch bip39 épinglé par révision. AAR local `0.45.1-notestr.1`, distinct de l’artefact officiel. Classes JVM officielles conservées ; 522 sommes de contrôle d’API UniFFI et contrat 30 concordants.
- Correctifs natifs : nostr 0.45.5, nostr-sdk 0.45.4, rustls 0.23.45, rustls-webpki 0.103.15, lru 0.18.5, anyhow 1.0.104, bytes 1.12.1 ; anciens modules relay-pool/relay-builder retirés du graphe. Cargo.lock complet conservé.
- JNA 5.19.1, coroutines 1.11.0, AppCompat 1.8.0 ; Gradle 8.14.5. Correctifs transitifs des outils de compilation : Netty 4.1.138.Final, Bouncy Castle 1.86, commons-compress 1.28.0, jose4j 0.9.7, JDOM 2.0.6.1. Ces substitutions sont validées par compilation et inventaire résolu.
- NDK stable 30.0.16248370 ; Rust 1.95.0 conservé pour reproduire la chaîne explicitement utilisée par l’amont. Rust stable actuel 1.98.1 recensé ; avis officiels examinés : avis Windows/Cygwin sans exposition dans les cibles Linux/Android utilisées, anciens avis généraux antérieurs au compilateur retenu. Ce choix reste une exception de version, pas une affirmation de dernière version.
- AGP 8.13.2, Kotlin 2.4.20, SDK Android 36, JDK Temurin 17.0.20.1+1 conservés. Les versions AndroidX antérieurement validées restent conservées hors mises à jour explicites ; les migrations majeures d’outillage ne sont pas incluses.
- OSV : **658 requêtes, aucune correspondance** sur les versions Maven résolues, le verrou Cargo complet et l’inventaire JavaScript. Rapport `security/sdk-osv-2026-09-24.json`. Cela ne garantit pas l’absence de vulnérabilité inconnue ou non répertoriée. Le verrou Cargo inclut également des composants optionnels non nécessairement liés.
- Métadonnées Maven officielles consultées pour les 276 coordonnées résolues ; signflinger, zipflinger et core-proto vérifiés sur Google Maven après correction du registre de recherche (leurs dernières entrées incluent des préversions non adoptées). TOAST UI reste archivé ; DOMPurify 3.4.15 et les mesures de filtrage documentées précédemment sont conservés.
- Contrôle ciblé sources et métadonnées images : `security/source-review-2026-09-24.json`. Aucun nouveau commit ; aucun historique local à publier. Les tests utilisent exclusivement des clés synthétiques identifiées et un relais loopback.

Validation terminée : 13 tests JVM, 15 tests instrumentés sur l’APK release et 5 tests isolés d’éditeur en debug réussis. APK non débogable, versionCode 17, 478 entrées décompressées inspectées sans marqueur ciblé détecté ; signature v2 et certificat existant vérifiés, bibliothèques natives conformes aux empreintes contrôlées. SHA-256 APK : `e31831f5d2c0810a3ad6d54b49e91d319280b43d5be76ab6208f163f6734396b`. Rapports dans `security/apk-check-2026-09-24.json` et VALIDATION.md. Le test sur smartphone et avec Amber réel restera à effectuer par l’utilisateur. Aucun push, tag distant ou transfert vers la copie de publication.

---

# Contrôle du 23 septembre 2026 — sauvegarde Linux / Android, non livrée

**Statut : code fonctionnel validé sur émulateur, livraison smartphone suspendue aux corrections du SDK natif. Aucune publication GitHub, aucun tag, aucun push, aucune modification de la copie `publication/notestr-android`.**

Ce contrôle complète et corrige la portée du rapport du 21 septembre ci-dessous : l’absence d’alerte Maven ne couvrait pas les dépendances Cargo embarquées. Les avis ci-dessous concernent des versions identifiées dans le SDK déjà utilisé ; ils ne sont pas introduits par la sauvegarde.

## Versions et sources examinées

- SDK Android `org.rust-nostr:nostr-sdk:0.44.8`, dernière version stable de ces coordonnées dans Maven Central au moment du contrôle. `0.45.0-alpha.7` est une préversion et n’a pas été adoptée. Le dépôt FFI expose un tag `v0.45.0`, mais aucun artefact Android stable `0.45.0` n’apparaît dans le registre consulté ; absence de release GitHub à cette adresse. Le tag exact `v0.44.8` reste absent de la liste consultée.
- Inventaire de 113 coordonnées Maven (111 modules du précédent inventaire runtime, Kotlin Gradle Plugin 2.4.20 et AGP 8.13.2) : métadonnées officielles Google Maven/Maven Central consultées. Requête OSV de 115 coordonnées en incluant DOMPurify 3.4.15 et TOAST UI Editor 3.2.2 : aucun avis retourné. Ce résultat ne remplace pas le contrôle natif.
- Extraction des chemins crate/version des quatre bibliothèques `libnostr_sdk_ffi.so` de l’AAR release : **94 couples crate/version** identifiés et interrogés dans OSV et crates.io. Empreintes de l’AAR et des bibliothèques dans `security/native-dependencies-2026-09-23.json`. Extraction reproductible avec `tools/audit-native-dependencies.py`. Ce n’est pas un SBOM complet : des composants sans chemins conservés peuvent manquer.
- Reconstruction candidate du graphe runtime JavaScript à partir du `package.json` de l’éditeur et du lock officiel : 11 dépendances ProseMirror et utilitaires interrogées dans OSV, aucun avis retourné. Versions stables npm également consultées. L’ancien DOMPurify intégré reste externalisé au profit de 3.4.15. Cette reconstruction ne prouve pas l’identité exacte de chaque module minifié ; TOAST UI reste archivé et sa migration reste nécessaire à terme.
- JDK effectivement exécuté : Temurin 17.0.20.1+1 ; endpoint Adoptium consulté et conservé dans l’inventaire. AGP 8.13.2, SDK de compilation 36 et AndroidX validés conservés ; les versions stables plus récentes recensées impliquent une migration coordonnée. BOM et autres outils ne sont pas annoncés comme tous à jour.
- Les résultats JSON, versions consultées et sources publiques sont conservés dans `security/dependency-review-2026-09-23.json`. Aucun identifiant personnel ou secret n’y figure.

## Correction de l’outil de compilation

Gradle **8.13 → 8.14.4**, avec SHA-256 officiel fixé dans le wrapper : `f1771298a70f6db5a29daf62378c4e18a17fc33c9ba6b14362e0cdf40610380d`.

Cette version corrige **GHSA-mqwm-5m85-gmcv** et **GHSA-w78c-w6vf-rw82**, concernant le repli vers un autre dépôt après certaines erreurs réseau. La branche 8 compatible avec AGP est conservée plutôt que de migrer simultanément vers Gradle 9. Les compilations de vérification et les 13 tests JVM passent avec 8.14.4 ; pas de réutilisation du build cache.

## Alertes natives ouvertes et exposition

Les versions sont celles extraites des binaires. Une preuve de correctif fournisseur rétroporté n’est pas disponible. Les minima corrigés ci-dessous proviennent des avis OSV/RustSec ; ils ne constituent pas une validation de compatibilité binaire.

| Composant embarqué | Avis RustSec | Correction annoncée / action |
| --- | --- | --- |
| anyhow 1.0.97 | RUSTSEC-2026-0190 | ≥ 1.0.103 ; vérifier les usages de `downcast_mut` après ajout de contexte |
| bytes 1.10.1 | RUSTSEC-2026-0007 | ≥ 1.11.1 ; débordement dans `BytesMut::reserve`, atteignabilité depuis le transport non exclue |
| lru 0.16.0 | RUSTSEC-2026-0002, RUSTSEC-2026-0253 | ≥ 0.18.2 pour couvrir les deux ; valider la migration de l’API et les types de cache |
| rand 0.8.5 et 0.9.0 | RUSTSEC-2026-0097 | ≥ 0.8.6 / ≥ 0.9.3 ; problème conditionné à un logger réentrant utilisant le RNG |
| rustls 0.23.25 | RUSTSEC-2026-0285 | ≥ 0.23.45 ; traitement des niveaux de chiffrement pendant le handshake TLS 1.3 |
| rustls-webpki 0.103.0 | RUSTSEC-2026-0049, -0098, -0099, -0104 | ≥ 0.103.13 pour couvrir les quatre ; contraintes de certificats et traitement des CRL |
| nostr-relay-builder 0.44.1 | RUSTSEC-2026-0237 | Composant non maintenu ; retirer la fonction serveur inutilisée ou migrer |
| nostr-relay-pool 0.44.3 | RUSTSEC-2026-0243 | Composant non maintenu séparément ; fonctions intégrées au SDK Rust 0.45.0 |

Analyse d’exposition :

- TLS/WebPKI est utilisé pour les connexions `wss://`, donc son code est sur le chemin réseau. L’avis rustls précise que le transcript reste authentifié et n’établit pas une possibilité de falsification du handshake par un attaquant réseau. Les avis WebPKI de contraintes de noms exigent des certificats signés incorrectement émis ; cette condition réduit l’exposition sans la supprimer. Les avis CRL exigent l’utilisation de CRL, non configurée directement par l’application ; la configuration complète du binaire fournisseur n’a pas été reconstruite.
- L’application ne configure ni logger Rust personnalisé utilisant le RNG, ni serveur de relais, ni caches avec destructeurs personnalisés. Les préconditions de certains avis ne sont donc pas établies dans les appels Kotlin. Cela ne suffit pas à prouver l’absence de chemins internes concernés, notamment pour `lru`, `bytes` et `anyhow`.
- Les avis spécifiques Nostr publiés par l’amont (validation des événements, NIP-44/NIP-04, etc.) ont été consultés. Les versions natives repérées `nostr 0.44.7` et `nostr-relay-pool 0.44.3` correspondent aux correctifs annoncés pour la release FFI 0.44.8. Cela ne corrige pas les avis transitifs listés plus haut.

**Exception limitée aux essais** : le SDK actuel a été conservé uniquement pour les tests en émulateur jetable, avec clé synthétique publique et relais loopback. Aucun compte réel ni relais public utilisé. **Aucune exception de livraison sur smartphone n’est accordée par ce rapport.**

Action nécessaire : produire un SDK Android avec dépendances natives corrigées et provenance vérifiable (reconstruction contrôlée avec vérification des bindings, ou migration vers une distribution stable corrigée), puis rejouer les essais sur les architectures distribuées. Un simple changement de version Gradle/Maven ne met pas à jour les crates à l’intérieur d’un AAR déjà compilé.

## Fonctionnalité et vérifications

Format partagé vérifié avec le code Linux local : kind 30078, adresse `notestr/previous/` + SHA-256 de l’identifiant, contenu NIP-44 de la précédente note repris sans déchiffrement/réchiffrement, même timestamp que la mise à jour, tag `e`. Le cache Android vérifie signatures et auteur, compacte les sauvegardes et utilise une écriture atomique. Chaque relais doit accepter la sauvegarde avant de recevoir la mise à jour.

Résultats : 13 tests JVM et 19 tests instrumentés Android réussis, puis lecture de la fixture produite par Android avec le code Linux. Voir VALIDATION.md pour le détail et les limites.

APK **interne de test uniquement**, toujours versionName 1.2.4 / versionCode 16, débogable, non livré. Signature v2 vérifiée ; certificat Android Debug habituel. SHA-256 : `fb703fa8462a7e0d860beb255844d7a0988c5e328a097ea9784fef3565f5cba8`. Cette empreinte ne désigne pas un nouvel APK de publication.

Contrôle ciblé des fichiers source hors caches/builds et des entrées décompressées de cet APK : aucun marqueur personnel, chemin personnel ou secret correspondant aux motifs recherchés. Les fixtures et clés de test synthétiques sont réservées à androidTest et absentes de l’APK applicatif. Résultats dans `security/test-artifact-check-2026-09-23.json`. Les images n’ont pas été modifiées. Aucun commit créé ; aucun nouvel historique destiné à publication. La vérification des métadonnées des images et de l’historique complet n’a pas été renouvelée ; elle restera nécessaire avant une livraison/publication.

## Limites et sources

Ce contrôle n’est pas exhaustif. Les graphes natifs et JavaScript sont partiellement reconstruits ; le graphe des plugins/outils de compilation n’est pas intégralement réinventorié ; l’inventaire Maven runtime reste celui de la version précédente, sans changement de dépendances applicatives. Le niveau Android/WebView du smartphone n’a pas été contrôlé. Aucune affirmation « sans vulnérabilité » n’est faite.

- [Registre Maven du SDK](https://repo.maven.apache.org/maven2/org/rust-nostr/nostr-sdk/maven-metadata.xml)
- [Tags du dépôt FFI](https://github.com/rust-nostr/nostr-sdk-ffi/tags)
- [Avis Gradle sur le repli entre dépôts](https://github.com/gradle/gradle/security/advisories/GHSA-mqwm-5m85-gmcv) et [hôte inconnu](https://github.com/gradle/gradle/security/advisories/GHSA-w78c-w6vf-rw82)
- [Avis rustls TLS 1.3](https://rustsec.org/advisories/RUSTSEC-2026-0285.html)
- [Avis WebPKI sur les contraintes DNS](https://rustsec.org/advisories/RUSTSEC-2026-0099.html)
- [Avis WebPKI sur les CRL](https://rustsec.org/advisories/RUSTSEC-2026-0104.html)
- [Avis de maintenance du pool Nostr](https://rustsec.org/advisories/RUSTSEC-2026-0243.html)
- [Avis du SDK Nostr](https://github.com/rust-nostr/nostr/security/advisories)
- [TOAST UI archivé](https://github.com/nhn/tui.editor), [DOMPurify](https://github.com/cure53/DOMPurify/releases)
- [OSV](https://osv.dev/), [crates.io](https://crates.io/), [Google Maven](https://maven.google.com/), [npm](https://www.npmjs.com/)

---

# Historique des contrôles antérieurs

Les conclusions ci-dessous sont conservées comme historique. Pour l’état actuel des alertes et de la livraison, utiliser le contrôle du 23 septembre ci-dessus.

# Complément pour la mise à jour du bouton Retour

Dépendances inchangées ; contrôle des versions de la même journée conservé et requête OSV renouvelée avant compilation : 114 coordonnées, aucune alerte retournée. Les exceptions et limites ci-dessous restent applicables.

APK 1.2.4 versionCode 16, non débogable, même certificat. Les sources livrables et l’APK sont à nouveau contrôlés pour les marqueurs personnels et secrets recherchés. Publication autorisée explicitement par l’utilisateur, par remplacement des fichiers de la release existante et alignement du tag sur le nouveau commit. Aucun commit de main n’est réécrit.

---

# Contrôle de sécurité de la version 1.2.4 — 21 septembre 2026

## Correctifs appliqués

- Kotlin Gradle Plugin et compilateur Compose passés de 2.2.21 à 2.4.20. Cette version est hors de la plage affectée par GHSA-r937-wjx7-w2jp / CVE-2026-53914 (cache de compilation). Compilation sans réutilisation du build cache.
- Suppression complète du module DOMPurify 2.3.3 intégré au bundle TOAST UI Editor. Les chemins de filtrage par défaut et personnalisés utilisent maintenant le DOMPurify 3.4.15 chargé séparément. Test Android des deux chemins, avec rejet de scripts, gestionnaires d’événements et liens javascript. Patch reproductible dans tools/externalize-editor-sanitizer.py ; empreinte du bundle original vérifiée.
- APK de publication non débogable, sans les dépendances réservées au débogage. Même certificat que les versions précédentes, dont l’intitulé reste Android Debug pour conserver la compatibilité de mise à jour. La clé privée reste locale et n’est pas publiée.
- Empreinte SHA-256 officielle de la distribution Gradle fixée dans le wrapper.

## Contrôle des dépendances

Versions stables vérifiées sur Google Maven, Maven Central et les pages officielles. DOMPurify 3.4.15, Nostr SDK 0.44.8, Biometric 1.1.0 et les bibliothèques de test déclarées sont à la dernière version stable identifiée dans leurs coordonnées respectives. Kotlin est à la version stable corrigée 2.4.20. JDK de compilation : Temurin 17.0.20.1.

L’inventaire des modules Maven réellement résolus pour releaseRuntimeClasspath figure dans security/runtime-dependencies-1.2.4.json. Requête OSV du 21 septembre 2026 : 114 coordonnées/version vérifiées (modules résolus, Kotlin Gradle Plugin et les deux bibliothèques JavaScript) ; aucune alerte retournée après les correctifs. L’ancienne copie de DOMPurify n’est plus présente dans l’artefact.

Des versions AndroidX plus récentes existent : Activity 1.13.0, Fragment 1.9.0, Lifecycle 2.11.0, BOM Compose 2026.09.00 ; le projet conserve ici les versions AndroidX validées avec SDK 36. Certaines versions récentes demandent une migration coordonnée de SDK/AGP. Gradle 8.13 et AGP 8.13.2 sont aussi conservés dans la plage compatible avec Kotlin 2.4.20. Ces exceptions sont explicites : toutes les dépendances ne sont pas annoncées comme étant les toutes dernières versions.

TOAST UI Editor 3.2.2 est la dernière release de son dépôt désormais archivé. Le remplacement du module vulnérable réduit le risque identifié, sans résoudre l’absence de maintenance globale de cet éditeur. Une migration ultérieure vers un éditeur maintenu reste à étudier.

## Confidentialité et artefact

Contrôle ciblé des fichiers prévus pour publication et des contenus décompressés de l’APK : marqueurs personnels connus, chemins de compte local, clés privées reconnaissables, secrets Nostr, jetons et caches locaux. Les identifiants auteur/committer Git utilisent l’identité de projet. Les exemples cryptographiques sont synthétiques. Aucun secret ni donnée personnelle du propriétaire détecté par ces recherches.

Le contrôle précédent du dépôt GitHub a couvert les 5 commits accessibles (HEAD f41d425b1d8c411b16aa40648a6b744055c4cc92), leurs métadonnées et 93 blobs. L’APK public 1.2.3 avait été téléchargé et son empreinte correspondait à la copie inspectée. Le nouvel historique ajoute uniquement les sources et documents de la présente version, sans journaux, résultats de tests, chemins de travail personnels ni clés de signature.

Des chemins de compilation des fournisseurs sont présents dans les bibliothèques natives ; ils ne désignent pas le propriétaire du projet. Les métadonnées de provenance de l’illustration originale sont conservées. Les identifiants publics du projet restent visibles.

APK : versionName 1.2.4, versionCode 16 (supérieur aux APK de test locaux), Android minimum 26. Débogage désactivé, sauvegarde Android et trafic HTTP en clair désactivés.

SHA-256 : `34834299085c421630f8ccce877984b82634083ffca447f5f7c1d85cce33386c`.

## Limites

Ce contrôle n’est pas un audit exhaustif ni une garantie d’absence de vulnérabilités. La requête OSV couvre les coordonnées inventoriées, pas une reconstruction complète des dépendances Cargo incorporées dans les binaires natifs du SDK Nostr. Leur provenance déclarée a été examinée ; aucun tag source v0.44.8 correspondant n’a été trouvé dans le dépôt FFI indiqué par le POM. Cette limite de traçabilité reste ouverte. L’inventaire transitoire des modules npm inclus dans TOAST UI, au-delà de DOMPurify, n’a pas été intégralement reconstitué. Les avis sur le système Android/WebView du téléphone dépendent de ses mises à jour. Les recherches de secrets ne détectent pas nécessairement des données encodées ou des identités inconnues.

Aucun compte utilisateur réel consulté. Les essais utilisent uniquement l’émulateur et des clés synthétiques. La publication GitHub de 1.2.4 est explicitement autorisée par l’utilisateur ; aucune release 1.2.5 n’est prévue.

## Sources

- https://developer.android.com/jetpack/androidx/versions
- https://kotlinlang.org/docs/gradle-configure-project.html
- https://github.com/advisories/GHSA-r937-wjx7-w2jp
- https://github.com/cure53/DOMPurify/releases
- https://github.com/nhn/tui.editor
- https://osv.dev/
