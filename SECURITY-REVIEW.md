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
