# Contrôles de sécurité — Notestr Android 3.9 (9 octobre 2026)

## Candidat local — affichage et restauration des notes verrouillées — 11 octobre 2026

Android 3.9-test.44 (versionCode 44), APK SHA-256 `e74a76fce55e5e8aaf4bfdc60e64bff051bda485027a948f7c1d91390e90c54d`. Le mode lecture seule ne demande plus `Editor.getMarkdown()` au lecteur Toast UI, qui ne fournit pas cette méthode ; le chemin de chargement des références d’images chiffrées reste actif. Le bouton et le ViewModel refusent le chargement d’une version antérieure pour une note verrouillée. Le retour d’une note depuis la corbeille reste autorisé.

14 tests JVM release réussis. Deux tests instrumentés ciblés réussis sur émulateur Android 11 : affichage/résolution d’image dans une note verrouillée et refus de restauration de version. APK de release local non débogable, signature v2 vérifiée, zipalign vérifié. Android 17/WebView 153.0.8010.36 non testé sur appareil physique.

Revue de dépendances réutilisée : contrôle 3.9 du 9 octobre (708 coordonnées Maven, crates.io, npm et PyPI). Aucun composant modifié pour ce correctif. Les 178 alertes de l’espace npm de développement/test archivé restent documentées et ne sont pas intégrées au runtime. Réserves de la revue précédente conservées ; aucune affirmation d’absence de vulnérabilité.

Scan ciblé des 83 fichiers source concernés et des 494 entrées APK : aucun chemin personnel connu, nom du propriétaire, nsec complet, en-tête de clé privée ou jeton GitHub détecté. Ce scan n’est pas exhaustif. Empreinte/signature et résultats du candidat dans `security/locked-note-fix-2026-10-11.json`. Candidat local uniquement : aucune publication, aucun push ni tag.

# Candidat local — notes verrouillées — 11 octobre 2026

Android 3.9-test.43 (versionCode 43), APK SHA-256 `bd3c15f3378d3005792ec128da6a7b5c76fbb3f1c65cb3ceee96c254a444419f`. Le lecteur visuel en lecture seule n’installe plus le gestionnaire d’événement d’édition. Les icônes verrouillé/déverrouillé sont alignées sur l’état de la note.

Tests unitaires release et assemblage release réussis ; test instrumenté ciblé d’une note verrouillée contenant une référence d’image chiffrée réussi sur émulateur Android 11. Dans la classe instrumentée complète, un échec existant subsiste sur l’ouverture d’un lien depuis la corbeille. WebView 153.0.8010.36 non testé sur appareil physique. Signature v2 vérifiée, APK non débogable ; certificat Debug local, donc incompatibilité possible d’installation par-dessus une build signée autrement.

Contrôle ciblé des sources et APK sans marqueur personnel recherché. Aucune dépendance modifiée ; la revue du 9 octobre et ses limites restent la revue de dépendances actuelle. Aucun claim d’audit exhaustif. Candidat local seulement, sans publication.

---

# Contrôles de sécurité — Notestr Android 3.9 (9 octobre 2026)

Android 3.9, versionCode 41. APK SHA-256 : `91426d31d2cf2ab6984ee3156128ed6fee2e8fa6e09c56ea59a58e2bdd00bcf7`. Signature APK v2 vérifiée ; le certificat est identique à celui de l’APK 3.8 (empreinte SHA-256 `8de9e60e3088c01bdf9c51695a1e06c7af0e89099e8d9800dc58c1bead3e63d8`). Le manifeste ne déclare pas l’application débogable.

Les dépendances Maven, Rust, npm et Python ont fait l’objet de 708 requêtes OSV. L’avis ProseMirror GHSA-c8x8-7fp4-3x9w est corrigé dans le bundle reconstruit avec `prosemirror-view` 1.42.6 (version corrigée 1.42.3 ou ultérieure), ainsi que des versions récentes compatibles des autres paquets ProseMirror. DOMPurify 3.4.16 est chargé séparément. L’audit npm de production ne signale aucune vulnérabilité ; l’audit complet du monorepo historique contient 178 alertes de dépendances d’outils/tests, qui ne sont pas embarquées et restent une réserve documentée.

La reconstruction conserve l’API TOAST UI 3.2.2 et ajoute le support des nœuds `linebreak` émis par ToastMark. `testReleaseUnitTest` et `assembleRelease` réussissent ; le test instrumenté d’ouverture, zoom, déplacement et fermeture de l’image réussit sur émulateur Android 11.

Le contrôle ciblé des sources, de l’APK, du wheel et de l’archive source n’a trouvé aucun chemin personnel connu, nom du propriétaire, adresse e-mail qui lui soit attribuable, clé `nsec` complète, marqueur de clé privée PEM ou jeton GitHub. Il ne constitue pas un audit exhaustif. Les identités déjà présentes dans l’historique Git antérieur sont conservées ; aucun historique distant n’est réécrit.

---

# Candidat local — verrouillage des notes et insertion d’image — 11 octobre 2026

Android 3.9-test.42 (versionCode 42), APK local SHA-256 `5fbc5be3b493c69a92451226a6ac8eb69b77e7282809a8233d71e62e30ff8cd0`. Le verrouillage synchronisé entre versions compatibles Android/Linux rend la note en lecture seule et bloque sa modification, publication et mise à la corbeille. L’icône d’insertion d’image est ajoutée après celle du tableau dans la barre Visuel.

Tests unitaires release et assemblage release réussis. Test instrumenté ciblé de l’icône, du sélecteur et de l’insertion au curseur réussi sur émulateur Android 11. Dans la suite instrumentée complète, 3 échecs restent : hiérarchie d’interface biométrique absente dans le test, biométrie forte non disponible sur l’émulateur, et ouverture de lien depuis l’affichage de la corbeille ; un test est ignoré. Les tests de verrouillage et de relais utilisent des données synthétiques. L’essai physique reste à faire.

APK signé v2 par le certificat Android Debug local (`8de9e60e3088c01bdf9c51695a1e06c7af0e89099e8d9800dc58c1bead3e63d8`), non débogable. Contrôle ciblé des sources et de l’APK : aucun marqueur personnel ou secret recherché. Aucune dépendance modifiée ; la revue du 9 octobre reste la revue actuelle. Ses limites et réserves précédentes restent applicables. Vérifications non exhaustives.

Candidat local uniquement ; aucun push ni tag distant. Détails dans `livrables/test-verrouillage-note-image-2026-10-11/LIRE-MOI.md`.

# Zoom et déplacement des images — candidat local Android du 9 octobre 2026

Android 3.8-test.40 (versionCode 40), APK SHA-256 `094d61314fda03b669b73ffb045f80e848975479b4156458bf0425949390534a`. La visionneuse d’image ajoute zoom +/−, réinitialisation, zoom focalisé au pincement et déplacement au doigt. Les limites gardent l’image dans le cadre, et la note reste inchangée.

17 tests JVM, compilation release et compilation Kotlin du test instrumenté réussis ; l’essai instrumenté n’a pas été exécuté faute d’appareil ou d’émulateur. Signature APK v2 vérifiée, certificat de test Android local, paquet non débogable et alignement vérifié. Les fichiers JavaScript/CSS embarqués sont identiques aux sources. Scan ciblé des 491 entrées APK sans marqueurs personnels recherchés ; il n’est pas exhaustif.

Aucune dépendance modifiée. Dernier audit large du 6 octobre, pas de nouvelle revue complète des registres le 9 octobre. La réserve GHSA-c8x8-7fp4-3x9w / CVE-2026-104847 dans ProseMirror embarqué via TOAST UI demeure ouverte ; les contrôles de collage locaux ne sont pas le correctif amont.

Candidat local uniquement, aucune publication. Rapport : `livrables/test-zoom-images-40-46/security/zoom-pan-images-2026-10-09.json`.

---

# Visionneuse d’images — candidat local Android du 9 octobre 2026

Android 3.8-test.39 (versionCode 39), APK local SHA-256 `2ec6ba663cc93a591827bc66fbb037d271762e84c806de89e77a97095ecca76f`. Un appui sur une image déjà déchiffrée ouvre une vue agrandie ; le bouton Retour Android, Échap ou le bouton de fermeture la referment. L’ouverture ne change pas le Markdown et ne déclenche pas de téléchargement.

17 tests JVM réussis, assemblage release et compilation du test instrumenté réussis. Le test instrumenté n’a pas été exécuté : aucun appareil/émulateur n’était connecté. L’APK est signé v2 par le certificat Android Debug local, non débogable, alignement vérifié ; le JavaScript/CSS embarqué est identique aux sources. Scan ciblé de 491 entrées APK : aucun chemin/nom du propriétaire, clé `nsec` formée ou en-tête de clé privée détecté. Ce contrôle n’est pas exhaustif.

Aucune dépendance modifiée. Dernier audit large du 6 octobre ; pas de nouvel audit complet de registres le 9 octobre. L’avis de gravité élevée GHSA-c8x8-7fp4-3x9w / CVE-2026-104847 dans ProseMirror empaqueté avec TOAST UI reste ouvert ; le filtrage local du collage n’est pas un correctif amont. Les autres limites 3.8 restent applicables. Le candidat est local, aucune publication.

Rapport de vérification ciblée : `livrables/test-plein-ecran-images-39-45/security/plein-ecran-images-2026-10-09.json`.

---

# Insertion d’image à la position choisie — candidat Android local du 9 octobre 2026

Android 3.8-test.38 (versionCode 38), APK SHA-256 `3021544b51a3ca03197ebdae951a1c1cf4988b4d61aa5cfdbb360d4d19b3e803`. 17 tests JVM et assemblage release réussis. Signature v2 valide, paquet non débogable, zipalign contrôlé ; le JavaScript embarqué est identique à la source. L’éditeur mémorise et restaure la sélection autour du sélecteur d’image, et accepte le Markdown complet renvoyé par le téléversement, en mode Visuel comme Markdown. Le bouton texte est remplacé par une icône accessible.

Scan ciblé des 491 entrées décompressées : aucun chemin personnel du propriétaire, courriel ciblé, motif complet de clé `nsec` ou en-tête de clé privée PEM détecté. Ce contrôle n’est pas exhaustif. Aucun téléphone ni émulateur n’a été utilisé. Pas de nouvel audit complet des dépendances le 9 octobre ; l’avis TOAST UI/ProseMirror et les réserves natives déjà documentées restent ouverts. Aucune publication.

Voir `security/image-position-candidate-2026-10-09.json` et `livrables/test-images-position-39/LIRE-MOI.md`.

# Aperçu immédiat des images — candidat Android local du 9 octobre 2026

Android 3.8-test.37 (versionCode 37), APK SHA-256 `d439a0da8e7426b2d63f218ee84ace19dc9d8b63ceb51ea825c7f43c5b510c47`. 17 tests JVM réussis, assemblage release, signature v2 vérifiée, non débogable et zipalign vérifié. Après un téléversement réussi, le blob chiffré est maintenant enregistré immédiatement dans le cache local, afin que le mode visuel puisse afficher l’image sans téléchargement Blossom supplémentaire. Aucun téléphone ni émulateur connecté ; validation physique à faire par l’utilisateur.

Les dépendances restent inchangées. Dernier audit général des registres le 5 octobre, sans nouvel audit complet le 9 octobre. Les contrôles ciblés portent sur l’APK décompressé et les sources ; ils ne sont pas exhaustifs. La réserve GHSA-c8x8-7fp4-3x9w / CVE-2026-104847 de l’éditeur intégré et les réserves natives de 3.8 restent ouvertes. Voir `security/images-candidate-2026-10-09.json`.

Paquet local uniquement ; aucune publication.

# Candidat Android initial d’insertion d’images — 3.8-test.36

Android 3.8-test.36 (versionCode 36), APK local signé v2, non débogable et aligné. SHA-256 `1558873d2edf8e37a861b5e7b6019bc5aa32813a268178620f1a348e7f437e8b`. 17 tests JVM réussis ; aucun téléphone ni émulateur connecté, le test physique est laissé à l’utilisateur. Les 15 bibliothèques natives de l’APK sont identiques à la version 3.8 déjà contrôlée. Les sources JavaScript embarquées ont été comparées aux sources.

Le traitement chiffre les images en AES-256-GCM avant envoi, enlève leurs métadonnées par conversion JPEG, limite le blob chiffré à 20 Mio et conserve seulement le blob chiffré en cache. Les serveurs sont lus dans les tags `server` du kind 10063 vérifié ; nostr.build n’est utilisé qu’en l’absence de serveur valide. Les anciennes et nouvelles versions de l’application ne partagent pas encore l’affichage de ce format.

Contrôle ciblé des 490 entrées APK décompressées : aucun marqueur de chemin du propriétaire, courriel, identifiant `nsec` ou clé privée connue. Les 11 images sources n’ont pas de métadonnée ; les images Android compilées ne contiennent que des champs de transparence (aucun EXIF). Dépendances inchangées ; dernier examen général le 5 octobre, pas de nouvel audit complet des registres le 9 octobre. Les réserves 3.8 restent ouvertes, notamment GHSA-c8x8-7fp4-3x9w / CVE-2026-104847 dans ProseMirror intégré à TOAST UI. Le collage de texte brut réduit le chemin décrit, sans remplacer le correctif amont ni prouver l’absence d’autres chemins. Voir `security/images-candidate-2026-10-09.json`.

Paquet local uniquement, sans publication.

# Publication 3.8 vérifiée — 5 octobre 2026

Android 229a579 (code 35), Linux 6eeec99, tags v3.8. Huit fichiers publics téléchargés anonymement et comparés aux fichiers contrôlés ; signature et certificat de l’APK public vérifiés. Rapport security/release-3.8-publication.json. Anciens auteurs publics conservés et réserves documentées maintenues. Toute publication suivante nécessite son propre accord.

---

# Version 3.8 — contrôle du 5 octobre 2026

Android 3.8 (code 35), Linux 3.8. Publication GitHub demandée explicitement pour les deux applications. Corbeille commune manuelle, modification chiffrée hors connexion, correction des blocages Android, corbeille visuelle et boutons clarifiés/alignés. Android : verrouillage après 180 000 ms en arrière-plan, horloge monotone et contrôle au retour, verrouillage manuel immédiat. Aucun vidage automatique de la corbeille. Sauvegardes préalables des sources effectuées.

## Dépendances et exposition

699 coordonnées directes/transitives et registres officiels reconsultés, bibliothèques JavaScript, Rust/JNA, outils de compilation et maintenance inclus : aucune alerte OSV applicative retournée ni erreur de récupération. Deux versions stables nouvelles intégrées sous Linux : aiohttp 3.14.4 et cachetools 7.2.1 ; empreintes des wheels vérifiées sur PyPI, nouveaux numéros interrogés dans OSV sans alerte retournée. Les versions natives et Android qualifiées sont conservées. Index Ubuntu rafraîchis : aucune nouvelle version candidate pour les 191 composants de référence. Chaîne Gradle/JDK/SDK conservée selon les exceptions de compatibilité antérieures ; absence d’alerte ne signifie pas absence de risque.

WebKitGTK/JavaScriptCore 2.54.0 embarqué : les neuf avis Ubuntu récemment indexés renvoient à WSA-2026-0006, déjà corrigé dans ce moteur ; les métadonnées Ubuntu 2.52.6 ne décrivent pas le binaire remplacé. Les autres réserves de l’analyse native étendue de 3.7.1 et les quatre avis additionnels d’octobre restent documentés dans release-3.8-native-exposure.json, sans les réduire aux seuls codecs. TOAST UI demeure archivé ; rendu assaini par DOMPurify et réseau de l’éditeur restreint. WebView Android fourni par le système. Les paquets ne sont pas déclarés exempts de vulnérabilité.

## Validation

14 tests JVM. Suite principale Android : 45 tests signalés (cas relais désactivé dans cette suite), une assertion de position du clavier en échec ; reprise isolée réussie. Suite sur relais local : 8 tests réussis, comprenant cette reprise et sauvegarde/corbeille/hors connexion ; 2 tests biométriques réussis. 48 cas Android distincts exercés, avec limite d’intermittence du test clavier conservée. Émulateur Android 11/API30, pas d’essai physique Android17. 92 tests Python réussis avec les dépendances actualisées, pip check sans incompatibilité. Essais GTK du moteur embarqué : enregistrement/reprise/conflits et copie, corbeille manuelle et alignement.

## Confidentialité et livraison

Contrôles ciblés des sources destinées à publication, ressources, métadonnées d’images, fichiers de revue et historiques. Comptes et notes synthétiques uniquement pour les tests, clés de test identifiées. Les anciens commits publics portant une identité d’auteur personnelle restent dans l’historique (3 Android, 2 Linux avant publication), sans réécriture. Les nouveaux commits utilisent une identité générique. Les contrôles ciblés ne garantissent pas l’absence de toute donnée inconnue ou encodée.

Paquets : examens du contenu décompressé, signature/certificat et alignement de l’APK, comparaison des bibliothèques natives avec les AAR contrôlés, comparaison de l’AppImage extraite avec l’AppDir et anonymisation des propriétaires de l’archive Python. Rapports datés, empreintes et résultats finaux dans security/release-3.8-*.json. Les fichiers de travail locaux, caches de compte, keystores et journaux bruts ne font pas partie des livrables. Notes utilisateur dans RELEASE-NOTES-3.8.md.

---

# Verrouillage Android après trois minutes — candidat local du 5 octobre 2026

Android 3.7.1-test.34 (code 34). Sauvegarde avant-verrouillage-2026-10-05. Le passage en arrière-plan déclenche un délai de 180 000 ms, annulé par un retour anticipé. Horloge monotone elapsedRealtime, incluant le sommeil ; vérification au retour avant reprise de l’interface pour couvrir un minuteur suspendu. Verrouillage manuel immédiat conservé, FLAG_SECURE inchangé, redémarrage du processus verrouillé. Une autorisation Amber reste possible pendant le délai ; à expiration la session et la demande en cours sont annulées. Le délai persiste lors du remplacement des ressources de session en arrière-plan. Le message avant ouverture d’un lien avec brouillon a été actualisé en français/anglais. Les brouillons non enregistrés restent perdus au verrouillage ; aucun nouvel enregistrement automatique ajouté. Linux inchangé.

14 tests JVM et 22 instrumentés réussis sur émulateur Android 11 : cycle réel arrêt/retour de l’activité, seuil juste avant/à 3 minutes, minuteur expiré en arrière-plan, arrêts répétés, verrouillage manuel, Amber, éditeur, corbeille et mode hors connexion. Seuils temporels testés avec horodatages monotones injectés, sans attente réelle de trois minutes. Téléphone Android 17 physique non testé.

699 coordonnées OSV et registres officiels, maintenance, outils et avis Rust reconsultés : aucune alerte retournée ni erreur ; métadonnées identiques au candidat 33, aucune dépendance modifiée. TOAST UI archivé et autres réserves de maintenance/exposition conservés ; WebView dépend des mises à jour système. Aucun paquet Linux recompilé. Contrôles ciblés de 1 598 entrées sources/archives et 490 entrées APK, images/métadonnées et historique : aucun marqueur personnel recherché non expliqué. Anciennes identités publiques conservées. APK non débogable, certificat/signature v2 historiques, alignement 16 Ko et correspondance des natifs avec les AAR contrôlés vérifiés. Contrôles non exhaustifs. Rapports et empreinte : security/background-lock-*.json. Aucun push, tag distant, release ni téléversement.

---

# Boutons de corbeille — candidats locaux du 5 octobre 2026

Android 3.7.1-test.33 (code 33) : accès à la corbeille par bouton texte « Corbeille », retour « Notes », distinct de l’icône de suppression dans la note. Linux 3.7.1.dev33 : marges et hauteur de rangée communes aux boutons Corbeille / Visuel / Markdown ; messages d’attente placés sous la rangée. Sauvegardes avant-boutons-corbeille-2026-10-05. Aucun changement du protocole, cache ou synchronisation.

Android : 15 des 16 tests instrumentés passent initialement ; assertion de position du clavier en échec, puis test isolé réussi sans modification du code. Émulateur Android 11 uniquement. Linux : contrôles GTK depuis l’AppImage finale extraite, alignement vertical et hauteur vérifiés avec/sans message d’attente ; corbeille, restauration et suppression manuelles validées. Cohérence de version vérifiée après correction du README de packaging. Pas de nouveau test JVM pour ce changement d’interface.

699 coordonnées OSV, registres officiels et maintenance reconsultés, aucune alerte applicative ni erreur, métadonnées identiques au candidat précédent. Index Ubuntu rafraîchis : aucune nouvelle version candidate. Neuf nouveaux avis Ubuntu concernent les métadonnées WebKit 2.52.6 : tous renvoient à WSA-2026-0006, corrigé dans le moteur 2.54.0 réellement embarqué depuis 3.7. Voir trash-buttons-native-exposure.json et les avis détaillés. Les autres réserves natives et l’état archivé de TOAST UI restent documentés, sans affirmation d’absence de vulnérabilité.

Sources/archives et paquets décompressés contrôlés par motifs ciblés, métadonnées d’images et anciens auteurs examinés : aucun marqueur personnel recherché non expliqué. Historiques publics conservés. APK non débogable, signature et certificat historiques, natifs comparés aux AAR et alignement vérifiés. AppImage extraite et comparée à l’AppDir. Contrôles non exhaustifs. Empreintes et résultats dans security/trash-buttons-*.json. Aucun push, tag distant, release ou téléversement ; copies de publication inchangées.

---

# Corbeille Android en mode visuel — candidat local du 5 octobre 2026

Android 3.7.1-test.32 (code 32), sauvegarde avant-corbeille-visuelle-2026-10-05. Remplacement du texte brut de la corbeille par le lecteur TOAST UI, avec le même assainissement DOMPurify et les restrictions CSP. Aucun champ éditable ni barre de mise en forme ; cases de tâches non modifiables, collage bloqué, source conservée exactement. Liens HTTP/HTTPS ouverts par le pont natif. Linux 3.7.1.dev31 inchangé. Le fonctionnement hors connexion du candidat 31 est conservé.

14 tests JVM et 16 tests instrumentés réussis sur émulateur Android 11, dont rendu visuel en lecture seule, clic réel sur lien, restauration/suppression manuelles et régressions éditeur/hors connexion. Test de routage du collage JavaScript réussi. Téléphone physique Android 17 non testé.

699 coordonnées OSV, registres officiels, maintenance et outils reconsultés : aucune alerte retournée ni erreur ; métadonnées identiques au contrôle du candidat 31. Aucune dépendance modifiée. TOAST UI archivé : exception de maintenance conservée, rendu assaini et réseau restreint ; WebView dépend des mises à jour système. Bibliothèques Rust/JNA inchangées, binaires APK comparés aux AAR contrôlés. Réserves natives Linux précédentes inchangées, aucun nouveau paquet Linux livré.

Contrôles ciblés de 1 597 entrées sources/archives et 490 entrées APK décompressées : aucun marqueur personnel recherché détecté ; métadonnées d’images examinées. Anciens auteurs publics conservés, sans réécriture. APK non débogable, signature v2/certificat historiques et alignement 16 Ko vérifiés. Ces contrôles ne sont pas exhaustifs et ne prouvent pas l’absence de vulnérabilité. Rapports et empreinte : security/trash-view-*.json. Aucun push, tag distant, téléversement ni publication ; copies de publication inchangées.

---

# Mode hors connexion — candidats locaux du 5 octobre 2026

Android 3.7.1-test.31 (code 31), Linux 3.7.1.dev31. Sauvegardes avant-hors-ligne-2026-10-05. Le bouton Publier enregistre d’abord une modification locale chiffrée NIP-44 et signée, puis l’envoie lorsque les relais sont joignables et l’application ouverte/déverrouillée. État en attente, reprise au redémarrage, protection des brouillons de l’éditeur, détection des conflits et publication explicite d’une copie. Corbeille et épinglage restent manuels. Dernière copie locale acceptée conservée pour détecter les envois simultanés, puis retirée si sa suppression définitive manuelle est connue. Détails et limites dans OFFLINE.md.

Validation : 14 tests JVM ; suite principale Android de 40 cas (le cas nécessitant un relais est activé séparément), 7 cas avec relais local et 2 biométriques, soit 43 cas distincts exercés. Émulateur Android 11/API 30 ; aucun test physique Android 17. 91 tests Python fonctionnels et 1 test de cohérence de version passent. Tests GTK de corbeille et de mode hors connexion, dont ce dernier sur l’AppImage extraite ; éditeur, langues, version et PDF du paquet contrôlés. Formats chiffrés vérifiés dans les deux sens entre Linux et Android. Les tests dédiés à la synchronisation utilisent des relais locaux ou simulés et des comptes fictifs identifiés. Le test graphique historique interceptait l’ancien envoi direct : il a été adapté à l’enregistrement local, avec reprises automatiques neutralisées pour isoler ce test.

699 coordonnées OSV applicatives et registres/maintenance officiels réinterrogés, sans alerte retournée ni erreur de métadonnées. Composants et chaîne de compilation qualifiés inchangés, exceptions de compatibilité maintenues ; TOAST UI reste archivé. AAR Rust/JNA inchangés et contrôlés, WebView Android fourni par le système. Index Ubuntu rafraîchis pour les 191 composants de référence : aucune mise à jour candidate supplémentaire. Aucun nouvel identifiant d’avis natif depuis le contrôle du 4 octobre. Les 140 signalements bruts de 3.7.1 et les quatre avis ajoutés le 4 octobre restent analysés dans les rapports cités, sans affirmation qu’ils sont tous corrigés ou inexploitables. WebKitGTK 2.54.0 conservé ; anciennes métadonnées de distribution distinguées du moteur réellement inclus.

Contrôles ciblés : 1 597 entrées de sources/archives, images/métadonnées, historiques et identités auteur ; 4 320 entrées de paquets décompressées. Aucun marqueur personnel recherché non expliqué. Cinq fixtures publiques GnuTLS reconnues par empreinte. Sources et paquets ne comprennent pas de compte utilisateur ni de notes personnelles utilisés pour les essais. Contrôles non exhaustifs. APK non débogable, signature v2/certificat conservés, alignement 16 Ko et natifs comparés aux AAR. AppImage extraite et comparée à l’AppDir. Empreintes et limites dans security/offline-*.json.

Aucune publication, aucun push ou tag distant ; copies de publication 3.7.1 inchangées. Les anciens auteurs personnels présents dans les historiques publics sont conservés, sans réécriture.

---

# Correctif Android du chargement — candidat local du 5 octobre 2026

Android 3.7.1-test.30 (code 30). Sauvegarde préalable avant-correctif-demarrage-2026-10-05. Blocage après biométrie ou mot de passe : la comparaison quadratique des événements et suppressions exécutée sur le fil de l’interface prend 39 756 ms sur 600 événements fictifs. Indexation des suppressions : 369 ms sur le même émulateur. Opérations du compte à clé locale déplacées sur IO ; ressources natives libérées après la fin de l’opération annulée lors du verrouillage. Format de corbeille conservé ; Linux 3.7.1.dev29 inchangé.

14 tests JVM, 33 tests instrumentés de régression, 2 tests biométriques et 1 test sur relais local réussis (36 instrumentés au total). Relais fictif remis à zéro pour isoler les suppressions terminales des essais précédents. Ces derniers comprennent un historique de 300 notes et 300 suppressions, la biométrie réelle de l’émulateur et le verrouillage pendant chargement. Émulateur Android 11/API 30 ; aucun test physique sur Android 17. Le symptôme signalé correspond au chemin bloquant reproduit, sans prétendre disposer du journal du téléphone.

699 coordonnées OSV et métadonnées officielles/maintenance reconsultées : aucune alerte retournée ni erreur de récupération. Versions stables inchangées depuis le contrôle précédent ; la variation du catalogue Gradle concerne les versions de développement. Chaîne qualifiée et exceptions de compatibilité conservées. AAR Rust/JNA inchangés et vérifiés ; TOAST UI archivé, WebView fourni par le système. Les réserves natives Linux antérieures demeurent documentées ; Linux n’est pas recompilé ici. Aucune affirmation d’absence de vulnérabilité.

1 587 entrées sources/archives et 490 entrées APK décompressées contrôlées par motifs ciblés, images/métadonnées et historique examinés : aucun marqueur personnel recherché non expliqué. Anciennes identités auteur publiques conservées, sans réécriture. APK non débogable, signature v2 et certificat historiques, alignement 16 Ko vérifiés. Contrôles non exhaustifs. Empreinte et résultats : security/startup-*.json. Aucun push, tag distant, release ni téléversement ; copies de publication inchangées.

---

# Corbeille commune — candidats locaux du 4 octobre 2026

Android 3.7.1-test.29 (code 29), Linux 3.7.1.dev29. Corbeille chiffrée commune, restauration et suppression définitive manuelles, aucune expiration ni vidage automatique. Sauvegardes avant-corbeille-2026-10-04. Aucun push, tag distant, release ou téléversement autorisé ; copies de publication 3.7.1 inchangées. Protocole et limites détaillés dans TRASH.md.

14 tests JVM, 31 tests instrumentés Android et 1 test avec relais local, 79 Python ; test graphique de corbeille Linux et contrôles AppImage éditeur/langue/version/PDF réussis. Échanges NIP-44 signés validés Linux vers Android et Android vers Linux. Annulation, échec d’envoi, restauration, suppression terminale et retrait des contenus du cache testés. Comptes fictifs identifiés uniquement, aucun relais public. Aucun test effectué sur le téléphone physique de l’utilisateur.

699 coordonnées OSV et registres officiels recontrôlés sans nouvelle alerte applicative ni erreur de récupération. Index Ubuntu rafraîchis et 191 composants de référence relus : aucune mise à jour candidate disponible. Bibliothèques natives inchangées depuis 3.7.1. Les réserves natives étendues de 3.7.1 restent ouvertes. Quatre avis supplémentaires : SoupServer non utilisé (CVE-2026-103399), serveur TLS SNI non utilisé (CVE-2026-19445), hostname TLS explicitement transmis par websockets 17.1 (CVE-2026-19553), polices CID malveillantes exclues du contenu des notes mais environnement de polices système à considérer (CVE-2026-95512). Ces analyses d’exposition ne constituent pas des correctifs du code natif. TOAST UI demeure archivé.

Contrôles ciblés de 1 586 entrées de sources/archives et du contenu décompressé des paquets, métadonnées d’images et historique : aucun marqueur propriétaire recherché non expliqué détecté. Clés publiques d’autotest GnuTLS reconnues par empreinte. APK non débogable, signature v2 et certificat inchangés, alignement et AAR natifs vérifiés. AppImage extraite et comparée à l’AppDir. Anciennes identités auteur déjà publiques conservées ; aucun historique réécrit. Contrôles non exhaustifs. Rapports datés, versions et empreintes : security/trash-*.json.

---

# Clôture publication 3.7.1 — 3 octobre 2026

Android 23b385e (code 28), Linux 77eaded, tags v3.7.1 publiés sur GitHub. Les huit fichiers publics ont été téléchargés anonymement et comparés aux fichiers contrôlés ; signature et certificat de l’APK public vérifiés. Rapport `security/release-3.7.1-publication.json`. Les réserves natives détaillées ci-dessous restent ouvertes ; publication ne signifie pas absence de vulnérabilité. Historiques et modifications README distantes conservés.

---

# Version 3.7.1 — 3 octobre 2026

Correction du collage natif dans les champs URL et Texte du lien de l’éditeur visuel, sur Android et Linux. Android versionCode 28. Publication GitHub explicitement autorisée pour cette version. Sauvegardes des sources réalisées avant préparation.

## Dépendances et réserves

699 coordonnées Android, JavaScript, Rust, Python et outils de compilation réinterrogées : aucune nouvelle alerte OSV retournée, aucune erreur de métadonnées. Versions stables et maintenance consultées sur les registres officiels. Les composants applicatifs examinés restent aux versions qualifiées de 3.7. TOAST UI 3.2.2 est archivé ; DOMPurify 3.4.16 et les restrictions de contenu sont conservés. WebView Android dépend du système.

Index Ubuntu isolés rafraîchis : 191 composants de référence contrôlés ; libgbm1 passe à 25.2.8-0ubuntu0.24.04.4, testé dans le paquet final. WebKitGTK/JavaScriptCore 2.54.0 et leurs typelibs restent ceux compilés pour 3.7. Aucun autre candidat Ubuntu plus récent trouvé dans ce périmètre.

Le contrôle natif élargi à 137 paquets sources a retourné 140 signalements bruts pour 31 paquets. Deux proviennent de métadonnées de l’ancien WebKit 2.52.6 alors que le moteur distribué est 2.54.0. Les autres sont analysés par famille dans `security/release-3.7.1-native-exposure.json` : outils ou fonctions non utilisés, architectures différentes, erreurs d’attribution de composants Python, restrictions de contenu et risques résiduels. **Ils ne sont pas annoncés tous corrigés ni tous inexploitables.** Le précédent résumé limité aux cinq avis de codecs n’était pas un inventaire complet des avis natifs.

Les versions Ubuntu maintenues sont conservées pour compatibilité ABI en l’absence de candidats corrigés dans les index consultés. Les images et SVG des notes sont exclus, le HTML brut et les accès réseau de l’éditeur restreints, les relais utilisent Python websockets. Cela réduit certaines expositions sans démontrer leur absence. Points à suivre : rendu Cairo/HarfBuzz, chemins indirects libsoup/GTK, IDNA Python, aperçus du sélecteur de fichiers et serveur X non fiable. Une migration des bibliothèques amont et de TOAST UI exige une qualification dédiée. Ces limites sont explicites ; aucune certification « sans vulnérabilité » n’est donnée.

## Validation et confidentialité

14 tests JVM, 29 tests Android instrumentés sur l’APK final, 75 tests Python. Collage Linux natif : URL, remplacement, libellé, annulation conservant le Markdown et insertion préservant les titres/listes. Paquet AppImage final : moteur 2.54.0, éditeur, paramètres/version/langues et export PDF validés. APK non débogable, signature v2/certificat conservés, alignement 16 Ko et bibliothèques natives comparées aux AAR examinés.

Sources, images/métadonnées, historique et paquets décompressés soumis aux contrôles ciblés de chemins personnels, identités connues, clés privées et jetons. Aucun marqueur recherché non expliqué détecté. Les cinq clés publiques de test GnuTLS sont identifiées par empreinte. Archives Python sans identité du compte de construction. Tests exclusivement synthétiques ; aucune note, capture personnelle, base locale ou journal brut distribué.

Des identités personnelles préexistantes restent dans les anciens commits publics ; historique conservé et nouveaux commits avec identité neutre du projet. Les vérifications ne sont pas exhaustives et peuvent manquer des données inconnues ou encodées. Empreintes et résultats datés dans les rapports `security/release-3.7.1-*.json`.

---

# Candidats locaux — collage URL — 2 octobre 2026

Android 3.7-test.27 (code 27) et Linux 3.7.dev27 corrigent le gestionnaire global de collage : les champs input/textarea utilisent désormais le collage natif, sans insertion dans le corps de la note. Sauvegardes avant-collage-url-2026-10-02. Aucune publication autorisée ou effectuée ; copies de publication 3.7 inchangées.

Validation : 14 tests JVM, 29 tests Android instrumentés sur émulateur (collage natif URL et annulation sans modification du Markdown), régression JavaScript sur les deux plateformes. Test natif Linux WebKit 2.54.0 : collage/remplacement URL, annulation, collage du libellé, insertion du lien avec conservation de la structure titres/listes. Le paquet Linux décompressé correspond à l’AppDir ; seuls editor.js et le numéro de version diffèrent du paquet 3.7 contrôlé. Test sur téléphone physique restant à faire par l’utilisateur. L’APK conserve son certificat, est non débogable, signé v2 et aligné ; bibliothèques natives comparées aux AAR examinés.

Nouvelle interrogation de 699 coordonnées OSV et des métadonnées officielles : aucune alerte retournée ni erreur de récupération ; cela ne démontre pas l’absence de vulnérabilité. Les versions des bibliothèques restent celles de 3.7. Les candidats APT de 181 composants de référence ont été relus dans les index locaux existants, sans rafraîchissement APT dans cette exécution. WebKit source 2.54.0 et mises à jour natives du paquet final 3.7 sont conservés octet pour octet. Les cinq exceptions de codecs, TOAST UI archivé et l’historique auteur personnel restent documentés ci-dessous.

Contrôles ciblés : 1 567 entrées de sources/archives et 4 319 entrées de paquets ; aucun marqueur personnel ou secret recherché non expliqué. Les cinq clés publiques d’autotest GnuTLS sont reconnues par empreinte, pas ignorées globalement. Métadonnées d’images et historique examinés ; deux anciens commits Android et un Linux conservent leurs identités auteur. Aucun historique réécrit. La capture personnelle fournie n’est pas intégrée aux sources ou paquets. Rapports datés et empreintes dans security/paste-url-*.json. Ces contrôles ne sont pas exhaustifs.

---

# Clôture Linux 3.7 — 2 octobre 2026

Linux 3.7 publié au commit f5060ab, Android 3.7 déjà publié au commit 4093c79. Les téléchargements publics des paquets Linux et de leurs SHA-256 correspondent aux fichiers contrôlés ; APK Android public et signature vérifiés précédemment. Voir `security/release-3.7-publication.json` et `security/release-3.7-packages.json`.

WebKitGTK/JavaScriptCore 2.54.0 remplacent 2.52.6 et traitent WSA-2026-0006. Compilation isolée Ubuntu 24.04, archive officielle vérifiée, garde vidéo de compilation documentée. Tests graphiques du paquet final réussis : éditeur, langues/version, PDF ; vrai clic droit et curseur main testés avec interception du navigateur. 376 ELF sans dépendance manquante. 75 tests Python réussis. 4 442 entrées de paquets contrôlées avec motifs ciblés, aucune correspondance non expliquée. Ce n’est pas un audit exhaustif.

Onze paquets natifs supplémentaires sont aux candidats Ubuntu disponibles. Cinq avis OSV sont conservés comme exceptions d’exposition documentées dans la source Linux `security/release-3.7-extra-native.json` : encodage non utilisé et problème propre à 32 bits, sur une AppImage x86_64. Ces avis ne sont pas annoncés corrigés. TOAST UI archivé et anciennes identités auteur Git restent des réserves documentées.

---

# Version 3.7 — contrôle du 1er octobre 2026

Publication Android 3.7 explicitement autorisée par l’utilisateur, versionCode 26. Cache avant synchronisation, liens externes corrigés, version et choix français/anglais dans les paramètres.

699 coordonnées OSV et métadonnées officielles Maven/Cargo/npm/PyPI recontrôlées sans alerte retournée ni erreur finale de récupération. Les dépendances restent identiques au candidat local validé ; le SDK Rust corrigé, JNA nettoyé et DOMPurify 3.4.16 sont conservés. Exceptions de compatibilité précédemment documentées maintenues ; TOAST UI archivé. Aucun nouveau binaire Rust compilé. Le WebView Android est fourni et mis à jour par le système.

14 tests JVM et 28 tests instrumentés sur l’APK final réussis. APK non débogable, signature v2 et certificat inchangés, alignement 16 Ko ; bibliothèques natives identiques aux AAR contrôlés. 490 entrées APK inspectées, sans marqueur personnel ciblé détecté. Sources, ressources, images et blobs Git contrôlés ; deux anciens commits Android contiennent des métadonnées d’auteur personnelles déjà publiques. Historique conservé, nouveaux commits et tags avec identité neutre du projet. Aucun compte personnel ou journal brut distribué. Les contrôles sont ciblés et ne garantissent pas l’absence de données inconnues ou encodées.

Voir `security/release-3.7-android.json`, `security/release-3.7-dependencies-2026-10-01.json` et `security/release-3.7-source-history-privacy.json` pour les versions, dates, empreintes et limites. La mise à jour Linux est contrôlée séparément : la réserve WebKit WSA-2026-0006 ne concerne pas le moteur système Android.

---

# Candidats locaux version/langue — 1er octobre 2026

Android 3.6-test.25 (code 25) et Linux 3.6.dev25. Version dans les paramètres, français/anglais mémorisé. Interface Android immédiate ; Linux au prochain lancement. Catalogue limité aux messages de l’application, jamais appliqué au contenu des notes ou aux champs des protocoles. Voir `LANGUAGES.md` et `security/language-*.json`.

14 tests JVM, 28 instrumentés Android, 70 Python et tests graphiques AppImage/PDF réussis. Persistance de la préférence, conservation du texte et numéro compilé vérifiés. Aucun compte personnel utilisé. 699 coordonnées OSV sans alerte retournée et métadonnées officielles reconsultées ; 181 paquets natifs comparés aux index actualisés, aucune mise à jour supplémentaire. Dépendances inchangées ; exceptions antérieures maintenues, notamment WSA-2026-0006 et maintenance TOAST UI.

4 160 entrées des paquets inspectées : aucun marqueur personnel ciblé détecté, cinq fixtures publiques GnuTLS classifiées par empreinte. APK signé v2, certificat inchangé, non débogable et aligné 16 Ko. Bibliothèques natives Android comparées aux AAR contrôlés, contenu Linux comparé après extraction. Contrôles ciblés non exhaustifs ; images inchangées. Historiques et identités anciennes documentées inchangés ; GitHub et copies de publication intacts. Aucune publication autorisée pour ce candidat.

---

# Candidats locaux menu/liens — 1er octobre 2026

Android 3.6-test.24, versionCode 24 ; Linux 3.6.dev24. Menu natif Linux dirigé vers le lanceur externe après synchronisation du brouillon, garde de révision conservée. Curseur main HTTP/HTTPS sur les deux plateformes. Aucune publication ni modification des copies de publication.

68 tests Python, 14 JVM, 10 instrumentés Android et essais graphiques AppImage/PDF réussis. Vrai clic droit et activation du menu testés avec interception du lanceur ; association réelle au navigateur à tester localement. Rapports datés et empreintes : `security/menu-links-*.json`. 699 coordonnées OSV et métadonnées officielles recontrôlées ; 181 paquets natifs comparés, aucune mise à jour supplémentaire. Exceptions précédentes maintenues, notamment WSA-2026-0006 et maintenance TOAST UI.

4 159 entrées des paquets inspectées, sans marqueur personnel ciblé détecté ; cinq fixtures publiques GnuTLS reconnues par empreinte. Signature v2, certificat, absence de mode debug et alignement APK vérifiés. Aucun compte personnel consulté. Contrôles ciblés non exhaustifs. Voir la notice `livrables/test-menu-liens-24/LIRE-AVANT-TEST.md` dans le dossier parent.

---

# Candidats locaux cache/liens — contrôles du 30 septembre au 1er octobre 2026

Android 3.6-test.23, versionCode 23 ; Linux 3.6.dev23. Aucun push, tag ou téléversement ; copies de publication 3.6 inchangées. Sauvegardes des sources avant modification. Voir `CACHE-LINKS.md` et les rapports `security/cache-links-*.json`.

Cache affiché avant synchronisation Android et consultable pendant celle-ci sur les deux plateformes. Résultats déchiffrés Android uniquement en mémoire, effacés au verrouillage. Cache disque toujours composé d’événements chiffrés, signés et vérifiés pour le compte. Liens HTTP/HTTPS dirigés vers le navigateur depuis un clic réel ; navigation externe dans l’éditeur interdite, vérification de révision conservée. Confirmation Android pour les brouillons avant le verrouillage lors de l’ouverture du navigateur.

14 tests JVM, 26 tests instrumentés sur l’APK final, 61 tests Python et tests graphiques du paquet Linux réussis. Banque de données et comptes synthétiques uniquement. Signature v2 et certificat Android inchangés, APK non débogable, bibliothèques natives identiques aux AAR contrôlés. 4 159 entrées décompressées inspectées : aucun marqueur personnel ciblé détecté. Cinq clés de GnuTLS ont été comparées aux fixtures publiques d’autotest amont ; cette classification précise les anciennes mentions génériques de constantes PEM. Aucun historique distant réécrit ; anciennes identités d’auteur déjà documentées inchangées.

699 coordonnées OSV sans alerte retournée, 699 métadonnées officielles consultées sans erreur finale. Versions compatibles et exceptions AGP/AndroidX/Rust conservées ; TOAST UI archivé. Six mises à jour Ubuntu embarquées (gvfs, gvfs-libs, heif-gdk-pixbuf, libheif1, libgbm1, libssl3t64). Les inventaires natifs distinguent les versions de départ et les mises à jour appliquées.

**Exception ouverte pour le test Linux : WSA-2026-0006**, publié le 29 septembre, recommande WebKitGTK 2.54.0. Les index Ubuntu Noble consultés proposent toujours 2.52.6. Contenu signé et chiffré pour le compte, filtre DOMPurify, CSP, aucune page externe, médias/images/canvas et WebGL désactivés limitent l’exposition ; le rendu Skia demeure utilisé et aucune inatteignabilité exhaustive n’est affirmée. Mettre à jour vers un moteur compatible corrigé ou vérifier un correctif rétroporté avant toute nouvelle publication. Le contrôle OSV Maven/Cargo/npm/PyPI ne couvre pas cette réserve native. Détail dans `security/cache-links-webkit-exception.json`.

Les contrôles de confidentialité et de sécurité sont ciblés, pas exhaustifs. Téléphone réel, Amber réel et association du navigateur par défaut à vérifier par l’utilisateur. Les clés et notes personnelles n’ont pas été consultées.

---

# Version 3.6 — contrôle du 27 septembre 2026

Publication GitHub des versions Android et Linux 3.6 explicitement autorisée par l’utilisateur. Android versionCode 22. Numéros publics alignés, historiques conservés.

Dépendances : 699 versions Maven/Cargo/npm/PyPI interrogées via OSV, aucune alerte retournée ; 699 métadonnées officielles consultées sans erreur. Inventaire partagé basé sur les graphes inchangés des candidats testés. Index APT renouvelés et 181 paquets natifs Linux comparés : aucun correctif supplémentaire disponible après intégration de cURL 8.5.0-2ubuntu10.15 et Expat 2.6.1-2ubuntu0.6. Les 41 versions Python verrouillées sont conservées. DOMPurify 3.4.16 et retrait de l’ancien filtre interne inclus sur les deux plateformes. SDK Rust corrigé et JNA nettoyé identiques aux composants Android déjà contrôlés.

Exceptions de maintenance conservées : TOAST UI 3.2.2 archivé, migration nécessaire à terme ; composition JavaScript minifiée reconstruite à partir des dépendances amont, non garantie exhaustive. AGP/AndroidX et Rust de construction conservés pour leur compatibilité, sans prétendre utiliser partout la dernière version. WebKitGTK fourni par la branche Ubuntu corrigée plutôt que par la toute dernière version amont. Les avis Rust Windows/Cygwin ne concernent pas les cibles Linux/Android utilisées ; aucun nouveau binaire Rust compilé pour cette version.

Validation finale : 14 tests JVM, 19 tests instrumentés Android release et 45 tests Python réussis ; essais graphiques de l’AppImage réussis. APK non débogable, signature v2 et certificat des versions précédentes vérifiés. Contrôles ciblés sur 4 159 fichiers des deux paquets décompressés, sans marqueur personnel recherché ; les en-têtes PEM des parseurs de dépendances sont des constantes de format, pas des clés. Aucun téléphone réel ni Amber réel utilisé pour ces tests.

Confidentialité des sources et de l’historique : 1 678 entrées de sources et d’archives inspectées sans correspondance aux marqueurs personnels recherchés. Deux commits Android et un commit Linux déjà publiés portent des métadonnées d’auteur personnelles. Ces données restent dans l’historique existant, qui n’est pas réécrit ; elles ne sont pas reproduites dans les nouveaux fichiers. Aucun marqueur du propriétaire détecté dans les blobs historiques examinés. Les nouveaux commits et tags emploient l’identité neutre du projet.

Les résultats de confidentialité, tests, signatures et empreintes finales sont consignés dans les rapports `security/release-3.6-*.json`. Les sections suivantes conservent l’historique des contrôles, dont les anciens statuts de candidats. L’absence d’avis dans les bases interrogées ne garantit pas l’absence de vulnérabilité inconnue.

---

# Candidats locaux avec épinglage — 26 septembre 2026

Aucune publication distante. Sauvegarde des deux projets effectuée avant modification ; archives et SHA-256 conservés séparément des livrables. Android 2.0-test.21 (versionCode 21), Linux 3.5.3.dev1. Format et limites fonctionnelles dans PINNING.md.

Contrôle renouvelé : 699 coordonnées Maven, Cargo, npm et PyPI interrogées via OSV, aucune correspondance retournée ; 699 consultations de métadonnées des registres officiels, sans erreur restante. Les erreurs initiales de décodage de l’index Rust ont été corrigées et toutes ses lignes relues. Inventaires et empreintes dans les rapports pinning-*.json. Graphe Android inchangé depuis 2.0 ; SDK natif et JNA conformes aux AAR vérifiés. Versions stables et avis de l’outillage consultés ; exceptions AGP/AndroidX/JDK/Rust de la revue précédente conservées. Les avis Rust examinés concernent Windows/Cygwin ou des versions antérieures à l’outil utilisé ; pas de reconstruction Rust pour ce changement. L’absence d’avis OSV ne démontre pas l’absence de risque.

Maintenance : TOAST UI 3.2.2 demeure archivé ; son remplacement reste une action à prévoir. Graphe JavaScript interne reconstitué, sans preuve exhaustive de chaque composant minifié. Linux reçoit DOMPurify 3.4.16 et le retrait de l’ancien module 2.3.3 intégré : nettoyage par défaut et personnalisé utilisent le même filtre maintenu. Les adaptations Linux de l’éditeur sont conservées. Pas de nouvelle bibliothèque pour l’épinglage.

Linux : index APT actualisés dans un dossier temporaire ; 181 paquets comparés. Correctifs cURL/GnuTLS 8.5.0-2ubuntu10.15 et Expat 2.6.1-2ubuntu0.6 téléchargés depuis Ubuntu et intégrés à l’AppImage. Correctifs Kerberos précédents conservés. Aucun paquet du système hôte installé/modifié. WebKitGTK 2.52.6 reste la branche de distribution, conforme à WSA-2026-0005 ; 2.54.0 amont non adoptée. Les 41 dépendances Python verrouillées ont été revérifiées avec OSV et PyPI, sans alerte identifiée. Le runtime AppImage conserve l’empreinte déjà contrôlée.

Confidentialité : contrôles ciblés des sources, ressources, images, archives AAR/JAR imbriquées et fichiers décompressés des deux livrables. Les essais utilisent uniquement les scalaires synthétiques publics 1 et 2. Aucun compte utilisateur réel consulté. Les chaînes d’en-tête PEM dans GnuTLS, libssh et cryptography sont des marqueurs de format, pas des clés privées ; un exemple nsec dans la documentation de distribution de monstr est retiré du paquet final, avec mise à jour du RECORD. Les licences et le code de monstr restent inchangés. Ces contrôles ne couvrent pas tous les encodages ni les données personnelles inconnues. Aucun historique Git, journal de test brut, cache utilisateur ou clé de signature n’est livré. Les historiques et la copie de publication ne sont pas modifiés.

Validation : 14 tests JVM, 8 tests instrumentés Android release, 45 tests Linux réussis. Tests GTK/WebKit de l’AppImage avec les correctifs natifs réussis ; épinglage/désépinglage et brouillon inchangé vérifiés. Lecture croisée Android/Linux validée avec événements signés et chiffrés synthétiques. Aucun test sur téléphone réel, Amber réel ou relais public. Limite Android de 2 000 événements par kind conservée.

APK non débogable, signature v2 et certificat de 2.0 conservés. Bibliothèques natives de l’APK identiques aux AAR vérifiés ; alignement de paquet vérifié. Empreintes finales et résultats de confidentialité dans security/pinning-artifacts.json et dans les SHA256SUMS remis avec les fichiers de test. Ces contrôles ne sont pas exhaustifs.

---

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
# Contrôles de sécurité — Notestr Android 3.9.1 (11 octobre 2026)

APK de publication 3.9.1, versionCode 46, SHA-256 `93961a5f133c2d7c2ec4bca7c4a3d1f4251865f51fc40a4dd235a8f6d0192f33`. L’APK final est non débogable, sa signature v2 est vérifiée et son certificat est identique à celui de la version précédente. Il a été installé et lancé sur l’émulateur Android 11/API 30.

Mises à jour : Kotlin 2.4.21, AndroidX Activity 1.13.0 et Fragment 1.9.1. Les 17 tests JVM de release réussissent. Les 18 tests instrumentés ciblés sur le verrouillage, l’éditeur, la restauration et les images réussissent, avec un test ignoré par condition. Le test d’intégration Android du verrouillage/déverrouillage, refus de suppression et relecture après redémarrage réussit sur un relais loopback synthétique. La suite instrumentée complète a deux échecs biométriques liés à l’émulateur non enrôlé (`BIOMETRIC_ERROR_NONE_ENROLLED`, puis absence d’interface Compose) ; ils ne sont pas masqués ni comptés comme réussites.

Lot OSV du 11 octobre : 708 coordonnées Maven, crates.io, npm et PyPI ; une alerte ressort pour `prosemirror-view` 1.27.0 dans les outils npm archivés. Le code embarqué de l’éditeur utilise la version revue et corrigée 1.42.6. Les versions Compose/Lifecycle n’ont pas été augmentées car les versions stables plus récentes exigent une plateforme et un plugin Gradle plus récents que le SDK/API 36 actuellement validé. Les exceptions antérieures du SDK natif sont conservées dans les sections historiques de cette revue.

Contrôle ciblé des 496 entrées de l’APK : aucun chemin personnel connu, courriel du propriétaire, `nsec` complet ou jeton GitHub correspondant aux motifs recherchés. Cette recherche n’est pas exhaustive. Aucun compte réel ni note utilisateur n’a servi aux tests. Le téléphone Android 17 de l’utilisateur et Amber réel n’ont pas été testés. Rapport : `security/release-3.9.1-android.json` et `security/release-3.9.1-dependencies.json`.

# Contrôles de sécurité — Notestr Android 3.9 (9 octobre 2026)


# Vérification APK Notestr Android 3.9.1 — versionCode 47 (12 octobre 2026)

Recompilation de la release existante pour permettre la mise à niveau Android : versionName `3.9.1` conservée, versionCode `47`. SHA-256 APK : `509d6fe20bc3f0cef47b4bf5460d64dcb61098d76bccaae371cc67e521a9ba90`. 17 tests unitaires release réussis. Signature de publication vérifiée (certificat SHA-256 `8de9e60e3088c01bdf9c51695a1e06c7af0e89099e8d9800dc58c1bead3e63d8`) et alignement ZIP vérifié. Les 496 entrées ont été inspectées par recherche ciblée : aucun marqueur recherché pour chemin personnel connu, clé privée, nsec ou jeton GitHub. Dépendances inchangées ; les avis et limites du contrôle du 11 octobre ci-dessus restent applicables. Pas d'installation sur appareil physique dans cet environnement. Rapport : `security/release-3.9.1-versioncode-47.json`.


# Candidat bêta Android — alignement des listes (14 octobre 2026)

APK local non publié, versionName `3.9.1-beta`, versionCode `48`, basé sur les sources publiées 3.9.1. SHA-256 `9be93f51b14b3162daf2e617cf4862d5ca759cef9b65e357fa0bb5a29620c7cc`. Signature et zipalign vérifiés ; certificat identique à la version publique. 17 tests unitaires réussis, 0 échec(s). Scan ciblé des 496 entrées sans correspondance des motifs owner path, clé Nostr exacte, en-tête de clé privée ou jeton GitHub. Aucun appareil Android connecté pour vérifier le rendu sur téléphone. Dépendances inchangées ; les limites de la revue du 11 octobre restent applicables. Rapport `security/list-alignment-test-2026-10-14.json`.

## Candidat local — téléchargement d’images (14 octobre 2026)

APK de développement `3.9.1-dev`, versionCode 49, SHA-256 `43f5d90ffdab6b179a0d903fcc2f1154e73dd793396635d401e4dc4a507406b1`. 17 tests JVM et compilation release réussis ; signature v2 et alignement ZIP vérifiés, 496 entrées contrôlées. Le menu contextuel Android propose le téléchargement après confirmation et utilise le sélecteur de fichiers système ; l’image exportée est déchiffrée et non chiffrée dans l’emplacement explicitement choisi. Scan ciblé : aucun chemin `[local path omitted]`, nsec complet ou jeton GitHub correspondant aux motifs recherchés dans l’APK. Scan de 124 fichiers source/ressource Android/Linux : aucun marqueur ciblé. Aucun téléphone ni émulateur Android n’était disponible dans cette exécution, donc pas de validation tactile sur appareil. Dépendances inchangées ; les réserves sur l’éditeur Toast UI et le SDK natif restent celles des sections précédentes. Aucun compte ni note réelle utilisé. Paquet local uniquement, aucune publication.

## Contrôles Android 3.9.2 — 14 octobre 2026

Le wrapper Gradle est passé de 8.14.5 à 8.14.6. Les notes officielles de Gradle indiquent que 8.14.6 corrige trois vulnérabilités élevées ; l’empreinte officielle du ZIP binaire (`7988ed…7bd24`) est inscrite dans le wrapper. Kotlin 2.4.21, AGP 8.13.2 et les bibliothèques runtime déjà qualifiées restent inchangés. Les versions AndroidX plus récentes n’ont pas été prises sans validation coordonnée de la plateforme API 36.

Gradle 8.14.6 : `testReleaseUnitTest` (17 tests) et `assembleRelease` réussis. APK 3.9.2, versionCode 50, SHA-256 `f5bffc660328f7cf048f5d29cd9a74a91e5229a72836abb379caaf675ef42ba7`. Non débogable ; signature v2 vérifiée ; certificat SHA-256 identique à celui de l’APK 3.9.1 versionCode 47 ; zipalign 16 Ko réussi. Aucun appareil ou émulateur Android n’était disponible pour test instrumenté/physique.

Les 496 entrées de l’APK ont été examinées par recherche ciblée de chemins personnels connus, nsec complète, en-têtes de clé privée et jetons GitHub : aucun marqueur. Les fichiers source suivis et ajoutés ont aussi été contrôlés. Une ancienne mention de chemin local dans cette revue a été retirée. Les identités d’auteur des anciens commits publics ne sont pas réécrites. Rapport : `security/release-3.9.2-android.json`.

## Contrôles Linux 3.9.2 — 14 octobre 2026

L’AppImage reprend le runtime, les bibliothèques natives et le jeu de dépendances de l’AppImage 3.9.1 contrôlée, puis y intègre les sources de l’application 3.9.2. WebKitGTK 2.54.0 et le bundle embarqué ProseMirror 1.42.6 sont conservés. Les versions PyPI aiohttp 3.14.5, cryptography 50.0.2, websockets 17.2 et Pillow 12.3.0 demeurent celles du paquet précédent contrôlé. La revue complète antérieure des dépendances du 11 octobre est réutilisée ; ce n’est pas un nouveau balayage exhaustif des registres natifs.

119 tests Python réussis. Le `--check` et les essais GTK/WebKit de l’AppImage finale réussissent, dont éditeur, paramètres, confidentialité CSP et export PDF. Le wheel et l’archive source sont construits depuis ces sources ; les propriétaires de l’archive source sont normalisés. AppImage reconstruite à partir du payload 3.9.1 et de ses bibliothèques natives : ce n’est pas une reconstruction complète de WebKitGTK.

Contrôle ciblé de 3 871 fichiers extraits : aucun chemin personnel connu, nsec complète ou jeton GitHub. Douze marqueurs d’en-tête PEM sont présents dans libgnutls, libssh et le module SSH de cryptography, composants amont embarqués ; aucune clé Notestr ou clé de compte n’a été trouvée dans le code de l’application. Aucun cache ou base utilisateur n’est inclus. Les identités d’auteur des anciens commits publics sont conservées. Ces contrôles ciblés ne prouvent pas l’absence de toute donnée personnelle inconnue ou encodée. Rapport : `security/release-3.9.2-linux.json`.

# Bêta locale — export PDF et dépendances (18 octobre 2026)

Candidat Android `3.9.3-beta`, versionCode `51`. Ajout d’un bouton « Exporter en PDF » : l’éditeur charge d’abord les images chiffrées de la note puis ouvre l’interface d’impression Android où l’utilisateur peut enregistrer le document en PDF. Mise en page A4, image limitée à 165 mm de hauteur, texte conservé dans le flux autour de l’image. Compilé avec JDK Temurin 17, Gradle 8.14.6, AGP 8.13.2 et SDK 36. Les 17 tests unitaires JVM passent ; `assembleRelease` passe. Pas de test instrumenté ni de test sur téléphone physique dans cette exécution.

Mises à niveau AndroidX vérifiées sur les pages officielles : Compose BOM 2026.06.00 (Compose 1.11.3) et Lifecycle 2.10.0, dernière combinaison stable vérifiée compatible avec compileSdk 36 dans ce projet. Compose BOM 2026.09.00/Lifecycle 2.11.0 ont été écartés après l’échec AAR officiel : API 37 et AGP 9.1 requis. Autres dépendances natives/JS non modifiées ; avis et limites de la revue du 11 octobre restent ouverts. Cela ne constitue pas un audit exhaustif de tous les binaires FFI natifs.

APK final copié et inspecté : versionName `3.9.3-beta`, versionCode `52`, package `fr.decentralia.notestr`, SHA-256 `e41aae28483faa5314164266482170dbaf503715b74afaa62300b6c84479a9e8`. Signature v2 vérifiée, certificat de test Android Debug SHA-256 `8de9e60e3088c01bdf9c51695a1e06c7af0e89099e8d9800dc58c1bead3e63d8`, identique au certificat des précédents candidats locaux. Analyse ciblée de 223 fichiers source et des 505 entrées de l’APK : aucun chemin personnel du propriétaire, courriel configuré, clé privée PEM, secret Nostr nsec complet ou jeton GitHub correspondant aux motifs recherchés dans le paquet ; un ancien rapport source mentionnait explicitement un chemin local et a été corrigé. Analyse limitée et non exhaustive. Téléphone Android 17 non testé ; les notes réelles n’ont pas été utilisées. Rapport détaillé dans `security/pdf-beta-2026-10-18.json`. Paquet non publié.

## Correctif export PDF Android avec images — 18 octobre 2026

Après le signalement que les exports avec images échouaient, l’éditeur attend maintenant le décodage de chaque image dans la WebView et confirme leur disponibilité avant d’ouvrir l’interface d’impression. Les images qui ne sont pas décodées dans le délai prévu produisent un message au lieu d’un PDF incomplet. Le code Android passe à 52 pour permettre l’installation du candidat sur le premier APK bêta ; le nom reste `3.9.3-beta`.

17 tests unitaires JVM et compilation release réussis ; analyse JavaScript (`node --check`) et contrôle de patch réussis. La signature v2 et le contenu décompressé de l’APK ont été vérifiés. Le contrôle ciblé a trouvé un ancien chemin local dans le texte du rapport lui-même ; il a été retiré, puis le paquet a été rescanné sans correspondance dans 223 fichiers source examinés et 505 entrées d’APK pour les marqueurs ciblés. L’ADB n’a pas pu démarrer dans cet environnement, donc l’impression d’une note contenant une image doit encore être testée sur le téléphone. Rapport : `security/pdf-beta-2026-10-18.json`. Paquet bêta local uniquement, aucune publication.

## Correctif Blossom Android — 18 octobre 2026

Comparaison du chargement Android avec Linux : Android ne tentait le serveur Blossom intégré à la note que s’il figurait encore dans le profil Nostr, et interrogeait le profil avant la première tentative. Le candidat final essaie maintenant directement le serveur de la référence puis, en cas d’échec, les serveurs du profil en tant que miroirs. Il conserve le serveur d’origine comme repli, même si le profil a changé. Le décodage des images est confirmé avant l’impression. VersionName `3.9.3-beta`, versionCode `53`.

18 tests unitaires JVM et compilation release réussis, dont un test des replis Blossom ; signature v2 et alignement 16 Ko vérifiés. L’APK final SHA-256 `ba79523ef3a8c06922ebe3c17ebd7d34012d3149e7df76ebac56eb15cefcd716` a 505 entrées. Scan ciblé de 223 fichiers source et des entrées APK sans chemin personnel connu, courriel configuré, clé PEM privée, nsec complète ou jeton GitHub correspondant aux motifs. Pas d’essai sur téléphone : l’ADB ne démarre pas dans cet environnement. Il reste donc à valider l’export PDF d’une note avec image sur l’appareil. Rapport : `security/pdf-beta-2026-10-18.json`. Candidat local uniquement, aucune publication.

## Correctif du second chargement d’image pour le PDF — 18 octobre 2026

Le candidat précédent perdait le lien entre la source intégrée affichée dans la WebView et la référence chiffrée. L’export demandait de résoudre les images à nouveau, mais aucune requête Blossom n’était alors émise et l’export restait en attente. La WebView conserve maintenant le lien référence/source intégrée et peut redemander une image déjà affichée. Test de régression Android ajouté : il simule le premier affichage puis le second chargement utilisé par l’export.

VersionName `3.9.3-beta`, versionCode `54`. 18 tests unitaires JVM réussis, compilation du test instrumenté réussie, compilation release réussie. Le test instrumenté n’a pas pu être exécuté : aucun appareil/emulateur ADB disponible. Signature v2 et alignement 16 Ko vérifiés. Contrôle ciblé de 223 fichiers source et 505 entrées APK sans marqueur personnel recherché. APK SHA-256 `d8730a6b3f654feff3d4681d9668342520e1b1228d8625cab8926b413dfcb5ab`. Il reste nécessaire de valider le vrai parcours d’export sur le téléphone. Rapport `security/pdf-beta-2026-10-18.json`. Candidat local non publié.


## Candidat Android 3.9.3 — paquet local contrôlé — 18 octobre 2026

L’APK local est `3.9.3`, versionCode `55`, paquet `fr.decentralia.notestr`. La compilation release avec JDK Temurin 17.0.20.1+1, Gradle 8.14.6, AGP 8.13.2, SDK 36 et Build Tools 36.0.0 a réussi. Les 18 tests JVM release passent ; la compilation Kotlin des tests instrumentés Debug passe, mais aucun appareil/émulateur n’était connecté pour les exécuter. Licence Android SDK acceptée pour cette compilation conformément à l’accord utilisateur.

SHA-256 APK : `42031c737638d2f3efeaa8da1f0cfbb0db80d0865e94313635aef59beb3f6b8e`. Signature APK v2 vérifiée ; certificat Android Debug de test SHA-256 `8de9e60e3088c01bdf9c51695a1e06c7af0e89099e8d9800dc58c1bead3e63d8`, identique aux candidats antérieurs et au certificat publié 3.9.2. Alignement 16 Ko réussi ; APK non débogable ; 505 entrées. Les métadonnées du paquet confirment versionName 3.9.3/versionCode 55/compileSdk 36.

Le contrôle source ciblé antérieur porte sur 172 fichiers ; l’inspection ciblée des 505 entrées APK n’a trouvé aucun chemin propriétaire, nsec complète, marqueur PEM de clé privée ni jeton GitHub correspondant aux motifs. Le balayage a repéré 44 chaînes ressemblant à des courriels dans des composants compilés tiers/natifs et les ressources de l’éditeur ; ce motif générique ne désigne pas de courriel configuré du propriétaire dans les sources ou la configuration de l’application. Les contrôles restent ciblés, et non exhaustifs.

Les réserves de sécurité Android précédentes restent applicables : dépendances Toast UI intégrées et couverture incomplète des binaires FFI. Les mises à niveau plus récentes de Compose/Lifecycle incompatibles avec API 36/AGP validé n’ont pas été forcées. Les mises à jour Linux Pillow 13.0.0 et paquets Ubuntu sont documentées dans la revue Linux. Le test Android sur appareil, notamment l’export PDF d’une note contenant une image Blossom, reste à faire ; le candidat local doit être validé par l’utilisateur avant toute publication. Rapport détaillé : `security/release-3.9.3-android.json`. Aucun téléversement, push ou tag distant effectué.
# Candidat local Android — tableaux adaptatifs et geste Retour — 18 octobre 2026

Android 3.9.3-dev.56 (versionCode 56), APK SHA-256 `ee2291abe7365abc6d6cf96649c9d7e3bda8bd62dce59e2de13466ec3afa8e1f`. Les tableaux Visuel sont contraints à la largeur disponible sur téléphone, avec cellules à mise en page fixe et texte long renvoyé à la ligne. Le geste système Retour depuis l’éditeur et les paramètres revient à la liste des notes.

18 tests JVM réussis ; compilation des tests instrumentés réussie, dont le test de largeur et de retour à la ligne des tableaux. Leur exécution sur appareil n’a pas été possible : aucun appareil ni émulateur connecté. Les dépendances ne changent pas ; la revue du jour de la version 3.9.3 et ses limites AndroidX, Toast UI et FFI restent applicables. APK non débogable, version et code vérifiés, signature APK v2 vérifiée, alignement 4 octets et pages 16 Kio vérifiés.

Contrôle ciblé de 95 fichiers source et des 505 entrées APK : aucun marqueur recherché de chemin personnel, nom du propriétaire, nsec complet, clé privée PEM ou jeton GitHub détecté. Le contrôle n’est pas exhaustif. Candidat de test local uniquement, sans publication. Rapport : `security/android-table-navigation-dev-2026-10-18.json`.

# Candidat Android local — export PDF avec plusieurs images — 18 octobre 2026

Android 3.9.3-dev.57 (versionCode 57), APK SHA-256 `2e724b88473d65a73e4bd835daab0b29a1f34a14e1a9c87dba8b48cb50a9642b`. L’export PDF ne retélécharge plus les images déjà affichées et déchiffrées ; les seules images absentes sont récupérées avant impression. Les chargements en cours sont suivis pour éviter les requêtes doublées, et un échec libère l’état afin qu’un nouvel essai puisse reprendre.

Cause identifiée dans le parcours précédent : le bouton PDF réinitialisait l’ensemble des requêtes d’image et récupérait toutes les images à nouveau. Les opérations du dépôt sont sérialisées par un mutex, et l’impression attendait la récupération et le décodage de toutes les images. Une note avec plusieurs images déclenchait donc des requêtes Blossom/déchiffrements/base64 redondants en série. Le nouveau test instrumenté couvre deux images distinctes et vérifie qu’elles sont réutilisées. 18 tests JVM réussis, test instrumenté compilé ; son exécution n’a pas été possible, ADB ne démarrant pas dans cet environnement. Aucun téléphone/émulateur validé.

Pas de dépendance modifiée ; revue de sécurité du jour de la 3.9.3 réutilisée avec ses réserves AndroidX, Toast UI et FFI. APK non débogable, version/code confirmés, signature v2 vérifiée, alignement 4 octets et pages 16 Kio vérifiés. Contrôle ciblé de 95 fichiers source et 505 entrées APK : aucun marqueur recherché de chemin personnel, nom du propriétaire, nsec complet, clé privée PEM ou jeton GitHub détecté ; contrôle non exhaustif. Candidat de test local seulement. Détails : `security/android-pdf-multiple-images-dev-2026-10-18.json`.

## Correctif de régression du chargement des images — Android — 18 octobre 2026

Le candidat 3.9.3-dev.57 ne consultait plus que la table `imageSources`. Quand l’éditeur normalisait la source d’une image, la référence chiffrée pouvait rester uniquement dans la table de secours `embeddedAssets`, donc l’image ne se chargeait plus. La résolution consulte à nouveau les deux tables. Les images déjà converties en `data:image/` sont ignorées afin de ne pas les redemander.

APK local 3.9.3-dev.58, versionCode 58, SHA-256 `ff128b44889d280d6267e0097dc9182751766aa9018198ac05736bb9ac101421`. 18 tests JVM réussis, compilation des tests instrumentés réussie ; pas d’exécution sur téléphone/émulateur. Vérification de la signature v2, des métadonnées, de l’alignement 16 Kio et de la logique embarquée dans l’APK réussie. Scan ciblé de 95 fichiers source et des 505 entrées APK sans marqueur sélectionné ; scan non exhaustif. Les dépendances sont inchangées ; les réserves AndroidX, Toast UI et FFI de la revue 3.9.3 restent applicables. Candidat local, aucune publication. Rapport : `security/android-image-loading-regression-dev-2026-10-18.json`.

## Correctif de la course de démarrage des images et du PDF — Android — 18 octobre 2026

Une course était possible entre l’appel JavaScript `setDocument` et le retour asynchrone de `evaluateJavascript` : l’éditeur pouvait émettre ses premières demandes d’image avant que Kotlin accepte les messages du WebView. La révision est désormais marquée active avant l’appel, et désactivée si l’initialisation échoue. Cela devrait permettre le chargement normal des images et la résolution des références avant l’export ; le scénario doit encore être confirmé sur téléphone.

APK local 3.9.3-dev.59, versionCode 59, SHA-256 `52e649f826f0d80bd377c58addb206a055003f1050c1e3979ff5b68edd3c43f2`. 18 tests JVM réussis ; tests instrumentés compilés mais non exécutés faute d’appareil/émulateur. Signature v2, version, alignement 16 Kio et contenus ciblés contrôlés. Scans ciblés de 96 fichiers source et 505 entrées APK sans marqueur recherché ; ils ne sont pas exhaustifs. Dépendances inchangées ; les réserves AndroidX, Toast UI et FFI restent applicables. Candidat local non publié. Rapport : `security/android-image-pdf-bridge-race-dev-2026-10-18.json`.

## Correctif de disponibilité du PDF avec plusieurs images — Android — 18 octobre 2026

L’étape de préparation PDF attend désormais le décodage effectif de chaque image et retombe sur les événements `load`/`error` si l’API `decode()` tarde ou échoue. Elle vérifie uniquement les références encore présentes dans les images de la note, et ne bloque plus sur une entrée obsolète de la table. Le test instrumenté couvrant deux images vérifie aussi que `prepareForPrint()` renvoie un résultat prêt.

APK local 3.9.3-dev.60, versionCode 60, SHA-256 `9e4407adc6eab860cef2fd9a53f90481177bd24ada888c5a6ff7a57126a0a7d3`. 18 tests JVM réussis ; tests instrumentés compilés mais non exécutés, ADB ne démarrant pas dans cet environnement. Signature v2, métadonnées, alignement 16 Kio et contenu embarqué vérifiés. Scan ciblé de 96 fichiers source et de 505 entrées APK sans marqueur recherché ; contrôle non exhaustif. Dépendances inchangées ; réserves AndroidX, Toast UI et FFI toujours applicables. Candidat local non publié. Rapport : `security/android-pdf-two-images-dev-2026-10-18.json`.

## Diagnostic PDF pour notes avec plusieurs images — Android — 18 octobre 2026

Le contrôle PDF se limite désormais aux images connectées et rendues dans le contenu actuel. Les images déjà complètement chargées sont acceptées sans attendre `decode()`. En cas d’échec restant, l’application affiche le nombre d’images chargées et le nombre de références encore à récupérer, ce qui permettra de distinguer un blocage du téléchargement d’un problème de rendu.

APK local 3.9.3-dev.61, versionCode 61, SHA-256 `83270ba0f9221a093f35d541a2738444f73398324303507b872de67c3fbbe85b`. 18 tests JVM réussis ; test instrumenté à deux images compilé mais non exécuté, aucun appareil/émulateur n’étant accessible ici. Signature v2, version, alignement 16 Kio vérifiés. Scan ciblé de 96 fichiers source et 505 entrées APK sans marqueur sélectionné ; scan non exhaustif. Dépendances inchangées et réserves AndroidX, Toast UI et FFI conservées. Candidat local non publié. Rapport : `security/android-pdf-images-diagnostic-dev-2026-10-18.json`.

## Déduplication des images dans l’export PDF — Android — 18 octobre 2026

Le diagnostic fourni par l’utilisateur pour dev.61 indiquait 6 éléments d’image, dont seulement 2 chargés, alors que la note comportait deux photos et qu’aucune référence ne restait à récupérer. L’export ne compte maintenant que les images rendues dans la zone de contenu WYSIWYG et déduplique celles qui partagent la même source. Le test instrumenté vérifie qu’une note de deux images donne exactement deux éléments imprimables.

APK local 3.9.3-dev.62, versionCode 62, SHA-256 `c6304f3f61c1b5a31614e8fbb6a6a83b73c0e6eac949c15f1a5d2d28c57f4517`. 18 tests JVM réussis ; tests instrumentés compilés mais non exécutés, faute d’appareil/émulateur accessible. Signature v2, métadonnées, alignement 16 Kio vérifiés. Scan ciblé de 96 fichiers source et 505 entrées APK sans marqueur sélectionné ; non exhaustif. Dépendances inchangées ; réserves AndroidX, Toast UI et FFI conservées. Candidat local non publié. Rapport : `security/android-pdf-image-dom-dedup-dev-2026-10-18.json`.

## Candidat Android dev.63 — numéro de candidat uniquement — 18 octobre 2026

Le candidat passe de 3.9.3-dev.62/versionCode 62 à 3.9.3-dev.63/versionCode 63 afin de permettre son installation comme mise à jour. Aucun code fonctionnel ni aucune dépendance n’a changé. Sauvegarde de `app/build.gradle.kts` : `sauvegardes/avant-dev63-2026-10-18/build.gradle.kts`.

La compilation utilise Temurin 17.0.20.1+1 (archive officielle vérifiée par SHA-256 via l’API Adoptium), Gradle 8.14.6, AGP 8.13.2, SDK 36 et Build Tools 35.0.0. `testReleaseUnitTest`, `assembleRelease` et `compileDebugAndroidTestKotlin` passent ; 18 tests JVM réussis. Les tests instrumentés ne sont pas exécutés sur appareil/émulateur. L’APK non débogable compte 505 entrées ; sa version, la signature v2 et l’alignement 16 Kio sont vérifiés. SHA-256 : `b20a1765d1b494f2cfd8b997c123fd04eefbde4f6a8ab955b2d5734404544c2b`.

Contrôle ciblé des 505 entrées APK : aucun chemin propriétaire connu, nsec complète, en-tête PEM de clé privée ni jeton GitHub selon les motifs recherchés. Ce contrôle n’est pas exhaustif. Les réserves AndroidX, Toast UI, FFI et dépendances tierces des revues précédentes restent applicables. L’export PDF d’une note avec plusieurs images doit encore être vérifié sur le téléphone. Paquet local uniquement ; aucune publication GitHub. Rapport : `security/android-pdf-images-dev63-2026-10-18.json`.


## Mise à jour de la release Android 3.9.3 — 18 octobre 2026

Après validation de l’APK dev.63 par l’utilisateur, l’asset APK de la release GitHub existante v3.9.3 a été remplacé par le paquet testé. Nom d’application embarqué : 3.9.3-dev.63 ; versionCode 63 ; numéro de release conservé : 3.9.3. SHA-256 : `b20a1765d1b494f2cfd8b997c123fd04eefbde4f6a8ab955b2d5734404544c2b`. Le tag v3.9.3 n’a pas été déplacé et l’historique n’a pas été réécrit.

Le correctif compte uniquement les images visibles et distinctes de la note pour la préparation PDF. L’utilisateur a confirmé que l’export avec plusieurs images fonctionne. 18 tests JVM passent ; compilation des tests instrumentés réussie. Signature APK v2, métadonnées et alignement 16 Kio vérifiés. Contrôle ciblé des 505 entrées APK et des sources publiées sans chemin propriétaire connu, nsec complète, clé PEM privée ni jeton GitHub détecté ; ces contrôles ne sont pas exhaustifs. Réserves AndroidX, Toast UI et FFI documentées précédemment conservées. Rapport : `security/release-3.9.3-android.json`.

## Correction du nom affiché de la version Android 3.9.3 — 18 octobre 2026

Le paquet publié indiquait `3.9.3-dev.63` dans les paramètres. La configuration Android est corrigée vers `versionName 3.9.3` et `versionCode 64`. Aucun code fonctionnel ni aucune dépendance n’a changé ; le code 64 permet à Android d’installer le paquet au-dessus du code 63.

La revue des dépendances s’appuie sur le rapport 3.9.3 du 18 octobre (`security/release-3.9.3-android.json`) : cette correction ne modifie aucune dépendance. Les limites AndroidX, Toast UI et FFI déjà documentées restent ouvertes ; l’absence de modification ne vaut pas nouvelle analyse exhaustive. Compilation Temurin 17.0.20.1+1, Gradle 8.14.6, AGP 8.13.2, SDK 36, Build Tools 35.0.0. Les 18 tests JVM passent ; la compilation des tests instrumentés passe, mais aucun appareil/émulateur n’est disponible ici. L’utilisateur a validé la fonction PDF multi-images avec le paquet précédent, dont le code fonctionnel est identique ; l’utilisateur a installé le paquet corrigé de code 64 et confirmé qu’il fonctionne ; la publication de ce paquet est autorisée.

L’APK non débogable contient 505 entrées. `aapt` confirme `fr.decentralia.notestr`, versionName `3.9.3`, versionCode `64` et compileSdk 36. La signature APK v2 est valide et conserve le certificat habituel (SHA-256 `8de9e60e3088c01bdf9c51695a1e06c7af0e89099e8d9800dc58c1bead3e63d8`) ; l’alignement 16 Kio passe. SHA-256 APK : `82405f5ea657a50599b5570b7a0ed99adcd6fd4b3486187be67bf1035941e8a6`.

Scan ciblé de 235 fichiers suivis et des 505 entrées APK : aucun chemin personnel connu, nsec complète, marqueur de clé privée PEM ni jeton GitHub détecté. Les motifs d’adresses e-mail génériques repèrent des chaînes dans les bibliothèques intégrées ; ils ne prouvent ni ne réfutent la présence d’autres données personnelles. Les contrôles sont ciblés, pas exhaustifs. Les identités et commits déjà publiés sont conservés, sans réécriture de l’historique. Release GitHub et tag `v3.9.3` inchangés en attente du test de ce paquet. Rapport : `security/release-3.9.3-android.json`.
