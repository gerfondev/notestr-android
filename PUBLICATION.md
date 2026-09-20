# Vérification avant publication — 1.2.3

Vérification ciblée des sources, ressources et 486 entrées décompressées de l’APK complet. Aucun nom personnel du propriétaire, chemin de son compte local, nom de sa machine, clé nsec complète, secret nbunksec, clé privée PEM ni jeton d’accès courant n’a été détecté. Recherche dans les octets et les chaînes UTF-16 ; examen des valeurs hexadécimales et métadonnées des images.

Les clés figurant dans les tests sont les valeurs synthétiques 1 et 2 et leur clé publique connue. Elles ne correspondent pas au compte de l’utilisateur et les tests ne sont pas inclus dans l’APK livré. Les mots nsec et password présents dans le code sont des noms de champs, de variables ou des libellés.

Les bibliothèques natives tierces contiennent des chemins de compilation de leurs fournisseurs (JNA et rust-nostr). Ils ne sont pas issus de l’ordinateur du propriétaire de ce dépôt. Le relais relay.decentralia.fr et l’identifiant fr.decentralia.notestr sont des paramètres publics. La publication ngit est associée à la clé publique de son auteur.

Caches, journaux, résultats de tests, sauvegardes, captures, propriétés locales et clés privées de signature sont exclus du dépôt. Les notes, identifiants et préférences du téléphone sont créés à l’exécution dans l’espace privé de l’application et ne sont pas embarqués dans cet APK.

APK signé avec le même certificat Android Debug que 1.2.1 et 1.2.2. La clé privée de signature n’est pas publiée. SHA-256 de l’APK : `335edf1786ea8fd7935cae6b1f75375bc228572042efa0c1dd94a5d063bfc17c`.

Cette vérification ciblée ne constitue pas un audit de sécurité exhaustif.
