# Validation de Notestr Android 1.2.4

VersionCode 14. Inclut le logo de connexion, le placement du menu de sélection Android et le redimensionnement de l’éditeur au-dessus du clavier. Kotlin 2.4.20 ; filtrage par DOMPurify 3.4.15 commun aux chemins par défaut et personnalisés.

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

- 8 tests unitaires : titres, conversion compatible Pages et normalisation du Markdown.
- BiometricVaultTest : une opération sans authentification est refusée ; la clé exige une authentification par opération ; chiffrement et déchiffrement après authentification réussis ; annulation sans déverrouillage ; mot de passe de secours opérationnel ; changement du mot de passe supprimant l’accès et la clé biométriques.
- BiometricUiTest : activation depuis les vrais réglages de MainActivity, annulation, nouvelle activation, verrouillage au passage en arrière-plan, retour au compte par biométrie, puis désactivation.
- Non-régression Android : menu H1–H6, bascules Markdown/Visuel, édition visuelle, coffre à mot de passe et vecteur NIP-44.
- APK complet compilé et signature vérifiée avant livraison.

Le pilote `tests/run-biometric-emulator.py` injecte les échantillons dans le capteur virtuel au moment des dialogues système ; il n’imite pas un succès de BiometricPrompt. Les tests biométriques effacent le coffre de l’application et sont réservés à un émulateur jetable. Ils utilisent une clé de test publique connue et uniquement une adresse de relais locale sans serveur.

## Limites

Le Pixel sous Android 17 n’est pas connecté. Le visage et le matériel biométrique du téléphone doivent encore être vérifiés sur l’appareil réel. La biométrie concerne l’ouverture de Notestr ; les autorisations propres à Amber restent indépendantes.

Références de conception : [BiometricPrompt et CryptoObject — Android](https://developer.android.com/identity/sign-in/biometric-auth), [NIP-55 — communication avec le signer](https://github.com/nostr-protocol/nips/blob/master/55.md).
