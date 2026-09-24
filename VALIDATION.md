# Validation de publication — 2.0, 24 septembre 2026

VersionCode 20 ; publication autorisée explicitement par l’utilisateur. Sauvegardes et actions à icônes issues des candidats locaux ; normalisation des titres inchangée avec nom de fonction générique et présentation actualisée.

- **13 tests JVM réussis** : titres, normalisation Markdown et sauvegarde lors de la publication.
- **17 tests instrumentés réussis sur l’APK release final** : sauvegarde/refus de relais, compatibilité des sauvegardes, restauration par icône puis publication, NIP-44, Amber simulé, coffre, clavier et Retour.
- Contrôles de sources, archives imbriquées, métadonnées d’images et contenu décompressé de l’APK ; aucun secret ou marqueur personnel recherché trouvé dans les fichiers distribués.
- Réserve historique : deux commits déjà publiés sur GitHub possèdent une adresse d’auteur ; aucune réécriture d’historique. Identité neutre pour le nouveau commit et le tag.
- Signature et certificat existant vérifiés ; APK non débogable, versionCode 20 ; empreintes natives conformes et alignements 64 bits vérifiés.

SHA-256 : `e4f8717281f4acb6c303e6120aff6c658b7e1f4eaa9b32745f8af0800c12cfe5`.

Émulateur jetable API 30, clés synthétiques et relais loopback. Pas d’essai complet sur tous les téléphones, de TalkBack manuel ou d’Amber réel. Les tests isolés de l’éditeur HTML ont passé sur 1.2.5 avec les mêmes ressources ; ils ne sont pas comptés dans ces 30 tests. Limites des audits et de pagination conservées. Voir les rapports `security/release-2.0-*.json`.

Les sections suivantes sont historiques.

---

# Candidat local à icônes — 1.2.5-test.19

VersionCode 19. Aucune publication ; copie de publication inchangée.

Actions Actualiser, Réglages, Verrouiller, Retour, Version précédente, Supprimer et Publier présentées sous forme d’icônes de 24 dp dans des cibles tactiles de 48 dp. Infobulles par appui long et descriptions accessibles. Nouvelle note utilise une icône plus. Version précédente rejoint la barre de l’éditeur ; les confirmations gardent leurs boutons explicites.

13 tests JVM réussis ; 3 tests UI release réussis sur émulateur API 30 : restauration/publication, clavier, Retour avec marge système et appui réel. Signature, manifeste non débogable, empreintes natives et contenu APK contrôlés. Revue de sécurité renouvelée sans alerte OSV retournée ; les limites et exceptions de 1.2.5 restent applicables.

SHA-256 : `c09322ae18b0d323530a269dac2c2c94d60fe3e5cd9ca4b373a674b578edc537`.

Rendu et ergonomie sur smartphone à tester. Les sections suivantes concernent les versions précédentes.

---

# Validation de publication — 1.2.5, 24 septembre 2026

Le candidat de sauvegarde 1.2.4-test.17 a été testé sur smartphone ; l’utilisateur a indiqué qu’il fonctionne et autorisé sa publication sous 1.2.5 après contrôle. Version finale : versionCode 18, DOMPurify 3.4.16 et métadonnées JNA anonymisées en complément. Aucune autre modification fonctionnelle depuis le candidat validé.

- Compilation release non débogable, signature existante et **13 tests JVM réussis**.
- **17 tests Android réussis sur l’APK release final** : sauvegarde/refus de relais, compatibilité Linux, restauration, NIP-44, Amber simulé, coffre, clavier de l’éditeur et bouton Retour.
- **5 tests isolés de l’éditeur réussis en debug** avec les mêmes ressources et bibliothèques : sélection, mise en forme, copie du code et filtrage HTML sur les deux chemins de l’éditeur. L’assertion de version du test a été mise à jour pour DOMPurify 3.4.16.
- Fixture produite par l’Android final déchiffrée avec le code Linux existant : note Unicode et sauvegarde unique correctes. Fixture Linux vérifiée côté Android. Aucune source Linux modifiée.
- Contrôle de confidentialité des fichiers de publication et des archives imbriquées ; signatures, empreintes natives, alignements et contenu décompressé de l’APK vérifiés. Aucun secret ou chemin personnel correspondant aux motifs recherchés détecté.

SHA-256 APK : `d1242c65ed785f54203f3444110839ed9e08055aafbd19b8ead1e9ef74e20182`.

Essais sur émulateur API 30 jetable avec clés synthétiques et relais loopback. Amber réel, relais publics, tous les matériels et le téléphone avec le binaire final ne sont pas couverts. Les cinq tests d’éditeur utilisent leur activité réservée au debug ; ils ne sont pas annoncés comme des tests release. Les limites de pagination et de sécurité déjà documentées restent applicables. Rapports dans `security/release-1.2.5-*.json`.

Les sections suivantes sont historiques.

---

# Validation du 24 septembre 2026 — candidat 1.2.4-test.17

Candidat Android local, versionCode 17, aucune publication. SDK reconstruit et dépendances corrigées ; détails et limites dans SECURITY-REVIEW.md.

- 13 tests JVM réussis avec Gradle 8.14.5 et le SDK mis à jour.
- Sur l’APK release final non débogable : 15 tests instrumentés réussis sur émulateur API 30 (relais local, compatibilité des sauvegardes, restauration UI, NIP-44, Amber simulé, coffre).
- Les cinq tests isolés de l’éditeur nécessitent l’activité fournie par la variante debug. Leur lancement initial en release échouait avant exécution faute de cette activité ; l’essai de manifeste séparé a été retiré. Ils ont tous réussi dans leur configuration debug habituelle, avec le même code et SDK (5/5).
- Android → Linux vérifié à nouveau avec la fixture synthétique produite par cet APK : note courante Unicode et unique sauvegarde lues par le code Linux existant. Linux → Android couvert par les tests instrumentés. Aucun changement des sources Linux.
- Inspection de 478 entrées décompressées de l’APK : aucun marqueur ciblé de secret, donnée personnelle ou fichier de test détecté ; empreintes natives conformes à l’AAR contrôlé. Signature v2 et certificat inchangé vérifiés. Alignement des bibliothèques 64 bits à 16 Ko vérifié.
- APK : SHA-256 `e31831f5d2c0810a3ad6d54b49e91d319280b43d5be76ab6208f163f6734396b`, 25 776 429 octets.

Reste à faire par l’utilisateur : test sur smartphone, notamment sauvegarde/restauration et Amber réel si utilisé. Les essais locaux ne couvrent pas tous les matériels, WebView, relais publics ou comportement d’Amber réel. La pagination au-delà de 2 000 événements par kind reste limitée. Aucune publication sans autorisation explicite pour cette version.

Les sections suivantes sont historiques ; leur ancien blocage SDK est remplacé par le contrôle du 24 septembre.

---

# Validation du 23 septembre 2026 — sauvegarde compatible Linux

Statut : changement local, non publié ; aucun APK à livrer tant que les alertes natives de SECURITY-REVIEW.md ne sont pas corrigées. La copie de publication est inchangée. Version applicative volontairement inchangée pour ces seuls essais internes.

## Résultats

- Gradle 8.14.4, JDK Temurin 17.0.20.1+1 : `testDebugUnitTest assembleDebug assembleDebugAndroidTest --no-build-cache --offline` réussis.
- **13 tests JVM réussis**, dont cinq nouveaux cas : nouvelle note sans sauvegarde, restriction aux relais ayant accepté la sauvegarde, refus total de sauvegarde, refus de mise à jour, échec du disque avant envoi de la mise à jour.
- **19 tests instrumentés réussis** sur émulateur Android 11 / API 30 jetable : BackupRelayTest (1), NoteBackupCompatibilityTest (4), BackupRestoreUiTest (1), Nip44CompatibilityTest (1), AmberSignerTest (4), AmberAuthorizationTest (3), MarkdownEditorTest (5).
- Linux → Android : fixture réellement produite par `Identity.save` et `Identity.backup` du code Linux local ; signature et contenu NIP-44 vérifiés dans l’émulateur, accents/Unicode et identifiant historique non limité à six caractères conservés.
- Android → Linux : fixture écrite par le code Android, lue avec `Identity.notes` et `Identity.previous` sous Linux ; note courante et sauvegarde correctement déchiffrées et sélectionnées.
- Sur deux chemins d’un relais loopback, dont un refuse le kind 30078 : publication réussie sur le relais acceptant, aucune note mise à jour envoyée au relais refusant la sauvegarde (trace des neuf envois contrôlée).
- Après effacement du cache de test : récupération de la sauvegarde depuis le relais, restauration avec le même identifiant, sauvegarde remplacée par la version quittée, une seule sauvegarde active en cache.
- Réponse de relais vide : note et sauvegarde locales conservées. Suppression : note et sauvegarde deviennent toutes deux indisponibles après actualisation.
- Dates identiques/futures refusées, départage par plus petit ID, suppressions datées, rejet d’un autre auteur et d’un contenu altéré vérifiés.
- Interface : annulation sans publication, chargement explicite de la sauvegarde, contenu visible en mode Markdown, publication séparée, conservation de la note courante comme référence de sauvegarde.

## Reproduction du test réseau local

Le test BackupRelayTest est ignoré sans l’argument d’instrumentation `localBackupRelay=true`. Il utilise uniquement une clé synthétique publique (scalaire 1) et les adresses `ws://10.0.2.2:18765/accept`, `/reject`, `/empty` sur l’émulateur. Le champ des relais de l’application continue d’exiger `wss://`.

Démarrer `tests/backup-relay.py` dans un environnement Python possédant `websockets`. Installer les APK debug et androidTest uniquement sur un émulateur jetable, puis exécuter :

```sh
adb -s emulator-5584 shell am instrument -w \
  -e localBackupRelay true \
  -e class fr.decentralia.notestr.BackupRelayTest \
  fr.decentralia.notestr.test/androidx.test.runner.AndroidJUnitRunner
```

Le relais est strictement local, n’utilise aucun compte réel et écrit seulement la trace des événements synthétiques dans `/tmp/notestr-backup-relay-traffic.json`. Relancer le relais avec un état vide avant de rejouer le scénario. Le port/série peuvent être adaptés au nouvel émulateur.

## Limites et reste à faire

- Tests exécutés en debug sur x86_64/API 30. Aucun essai sur le smartphone de l’utilisateur, sur ARM réel, sur relais public ou avec une véritable installation Amber pour ce changement.
- Les tests Amber portent sur un signataire simulé ; le code de protocole est commun aux deux modes, mais les doubles demandes de signature avec Amber réel doivent encore être validées sur appareil.
- Le chargement dépassement de 2 000 événements par kind n’est pas couvert par une pagination exhaustive.
- Corriger/reconstruire les composants natifs du SDK, répéter les tests, attribuer une version de test supérieure à versionCode 16, puis effectuer les contrôles de l’APK release non débogable avant remise pour test sur smartphone.
- **Aucune publication GitHub sans validation explicite de l’utilisateur après son test de la version concernée.**

---

# Mise à jour 1.2.4 : bouton Retour

VersionCode 16. Marges système appliquées aux formulaires, en dehors de la zone défilante. Le libellé Retour reste sous la barre système et les découpes de l’écran.

Compilation release et 8 tests unitaires réussis. Le test de marge échoue sur l’ancienne version et passe après correction. Deux tests tactiles exécutés sur l’APK release final : appui réel sur le quart supérieur du texte Retour et non-régression du clavier dans une note longue.

Les validations ci-dessous restent celles de la publication initiale ; elles n’ont pas toutes été répétées pour ce changement de marges.

---

# Validation de Notestr Android 1.2.4

VersionCode 16. Inclut le logo de connexion, le placement du menu de sélection Android et le redimensionnement de l’éditeur au-dessus du clavier. Kotlin 2.4.20 ; filtrage par DOMPurify 3.4.15 commun aux chemins par défaut et personnalisés.

- 8 tests unitaires réussis.
- 15 tests Android réussis sur émulateur API 30 : éditeur, appui long première ligne, gras/italique, titres, copie de code, bascules de mode, saisie d’une note de 45 lignes avec clavier et fenêtre bord à bord, filtrage HTML, Amber, vecteur NIP-44 et coffre à mot de passe.
- 2 tests biométriques réussis, avec capteur virtuel et dialogue Android réel (coffre et parcours UI).
- Compilation de l’APK release non débogable réussie ; certificat identique aux versions précédentes.
- 10 tests supplémentaires réussis sur l’APK release final (tests compilés pour la même variante) : clavier, Amber, NIP-44 et coffre. Les APK de test debug ne sont pas interchangeables avec ceux de release à cause des noms internes Kotlin.
- Patch de suppression du DOMPurify intégré reproduit à l’identique ; rejet d’une entrée dont l’empreinte ne correspond pas.
- Contrôle de confidentialité : 79 fichiers source/publication et 479 entrées d’APK analysés, sans détection de marqueur personnel ni secret recherché.

Les comportements propres au matériel et à la version Android du téléphone ne sont pas tous reproduits par l’émulateur. Le contrôle de sécurité et ses limites sont décrits dans SECURITY-REVIEW.md.

---

# Validation de Notestr Android 1.2.3 — copie des blocs de code

VersionCode 11. Bouton Copier dans le mode Visuel, remplaçant le champ de langage TOAST UI. Contrôles hors du document ProseMirror ; copie via le bridge Android existant et ClipboardManager. Confirmation après écriture effective. Le langage du bloc reste conservé dans le Markdown.

Vérifications :
- 8 tests unitaires réussis ; assembleDebug et assembleDebugAndroidTest réussis.
- Tests navigateur à 412 px : plusieurs blocs, caractères spéciaux, lignes vides et indentation, contenu édité, absence de modification du Markdown par la copie, champ de langage masqué. Capture inspectée.
- 3 tests Android MarkdownEditorTest réussis sur émulateur API 30 : vraie interaction tactile avec le bouton et lecture du presse-papiers Android, menu des titres, bascules répétées Markdown/Visuel.
- Signature APK vérifiée, identique à 1.2.2.

APK complet 1.2.3 publié sur ngit avec le tag v1.2.3. Tests Amber et biométriques complets non réexécutés pour ce changement d’éditeur.

---

# Validation de Notestr Android 1.2.2 — Amber

Version code 10. Reprise des sources dans le dossier App Android/notestr-android.

## Correction

L’ancien client assimilait une réponse null du ContentProvider à un refus définitif. Désormais, cette absence de permission mémorisée déclenche une demande Android ciblée vers le signer enregistré : chiffrement NIP-44, signature (notes 33457 et suppressions 5), déchiffrement. Un refus mémorisé reste respecté sans relance interactive. Les requêtes ContentProvider s’exécutent hors du thread principal.

La session reste ouverte pendant la demande Amber. Annulation/refus laisse l’éditeur en place ; fin de demande, verrouillage et annulation nettoient la requête. Une recréation d’activité ne relance pas une demande déjà envoyée. Les erreurs de déchiffrement remontent désormais au lieu de masquer silencieusement des notes.

## Vérifications effectuées

- testDebugUnitTest : 8 tests réussis.
- assembleDebug et assembleDebugAndroidTest : réussis.
- Android 11/API 30 : 7 tests Amber réussis (AmberSignerTest et AmberAuthorizationTest), exécutés avec adb am instrument. Signer simulé, aucun compte personnel ni relais public utilisé.
- Couverture : absence de permission et demande de chiffrement/signature ; autorisation mémorisée ; refus mémorisé sans ouverture ; refus interactif ; caractères Markdown dans l’URI ; maintien de l’éditeur pendant la demande ; annulation/refus ; verrouillage normal et réponse tardive après verrouillage.
- Signature APK vérifiée et certificat identique à 1.2.1 : mise à jour sans désinstallation possible.
- connectedDebugAndroidTest via Gradle indisponible hors ligne (dépendance UTP absente du cache) ; mêmes APK de tests installés et exécutés directement via ADB.

## Limites

Pas de test avec l’application Amber réelle ni sur le téléphone de l’utilisateur. La publication de bout en bout sur un relais et les permissions propres à sa version d’Amber restent à confirmer sur appareil. Les tests biométriques complets n’ont pas été réexécutés.

Références : [NIP-55](https://github.com/nostr-protocol/nips/blob/master/55.md), [décodage des intents Amber](https://github.com/greenart7c3/Amber/blob/master/app/src/main/java/com/greenart7c3/nostrsigner/service/IntentUtils.kt).

---

# Validation de Notestr Android 1.2.1

Version code 9. Ajout de l’icône fournie par l’utilisateur au lanceur Android : fond clair, premier plan adaptatif et ressources dans cinq densités. Les attributs icon et roundIcon utilisent cette ressource. Illustration originale conservée dans artwork/notestr.png ; génération reproductible avec tools/generate-launcher-icons.py (Pillow).

Compilation assembleDebug réussie ; signature identique à la version 1.2.0 ; manifeste de l’APK vérifié (version, activité du lanceur et icône). Aperçu sous masque circulaire inspecté. Aucun changement de logique applicative ; les tests fonctionnels ci-dessous sont ceux de la version 1.2.0, non réexécutés pour cet ajout de ressources.

# Validation de Notestr Android 1.2.0

Version code 8. Cette version ajoute le déverrouillage biométrique ; les changements Amber ont été reportés à la demande de l’utilisateur.

## Fonctionnement

- Activation volontaire dans Réglages après ouverture d’une session existante ; confirmation par le dialogue Android.
- Empreinte ou visage classé BIOMETRIC_STRONG par Android. Aucun échantillon biométrique n’est accessible à Notestr.
- Coffre biométrique distinct : identifiant de connexion chiffré en AES-256-GCM avec une clé Android Keystore exigeant une authentification à chaque opération, via BiometricPrompt.CryptoObject.
- L’enveloppe est liée au coffre à mot de passe courant par des données authentifiées dérivées de son contenu chiffré. Ces données sont transmises au moteur cryptographique après l’authentification, comme l’exige Keystore pour cette clé.
- L’option n’enregistre pas le mot de passe. Le coffre à mot de passe existant reste utilisable.
- Désactivation et suppression de la clé biométrique lors d’un changement de mot de passe ou de compte. Clé configurée pour être invalidée en cas de modification des biométries enregistrées.
- Annulation du dialogue et capteur indisponible : accès par mot de passe conservé.
- Verrouillage lors du passage en arrière-plan, annulation des opérations en cours et rejet des résultats issus d’une ancienne session.
- Le code du formulaire de connexion Amber est identique à celui de 1.1.4. Les corrections de l’éditeur et des titres restent incluses.

## Vérifications

Émulateur Android 11 / API 30, empreinte simulée enregistrée, aucun compte utilisateur réel.

- 8 tests unitaires : titres, conversion des titres à la publication et normalisation du Markdown.
- BiometricVaultTest : une opération sans authentification est refusée ; la clé exige une authentification par opération ; chiffrement et déchiffrement après authentification réussis ; annulation sans déverrouillage ; mot de passe de secours opérationnel ; changement du mot de passe supprimant l’accès et la clé biométriques.
- BiometricUiTest : activation depuis les vrais réglages de MainActivity, annulation, nouvelle activation, verrouillage au passage en arrière-plan, retour au compte par biométrie, puis désactivation.
- Non-régression Android : menu H1–H6, bascules Markdown/Visuel, édition visuelle, coffre à mot de passe et vecteur NIP-44.
- APK complet compilé et signature vérifiée avant livraison.

Le pilote `tests/run-biometric-emulator.py` injecte les échantillons dans le capteur virtuel au moment des dialogues système ; il n’imite pas un succès de BiometricPrompt. Les tests biométriques effacent le coffre de l’application et sont réservés à un émulateur jetable. Ils utilisent une clé de test publique connue et uniquement une adresse de relais locale sans serveur.

## Limites

Le Pixel sous Android 17 n’est pas connecté. Le visage et le matériel biométrique du téléphone doivent encore être vérifiés sur l’appareil réel. La biométrie concerne l’ouverture de Notestr ; les autorisations propres à Amber restent indépendantes.

Références de conception : [BiometricPrompt et CryptoObject — Android](https://developer.android.com/identity/sign-in/biometric-auth), [NIP-55 — communication avec le signer](https://github.com/nostr-protocol/nips/blob/master/55.md).
