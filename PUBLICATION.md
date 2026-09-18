# Vérification avant publication — 1.2.1

Le périmètre vérifié comprend les sources publiées, les ressources et les 486 entrées décompressées de l’APK complet. Aucun nom de compte système local, nom de machine, chemin personnel local, clé nsec ou secret de connexion Nostr n’a été détecté. L’image originale ne contient pas de métadonnées annexes. Les tests utilisent des données synthétiques et des vecteurs cryptographiques publics.

Les caches de compilation, sauvegardes, captures de test, propriétés locales et clés de signature sont exclus du dépôt. Les notes, comptes et préférences d’un téléphone ne sont pas intégrés à l’APK : ces données sont créées à l’exécution dans l’espace privé de l’application. Le relais par défaut relay.decentralia.fr et l’identifiant fr.decentralia.notestr sont des paramètres publics de l’application.

L’APK est signé avec le certificat Android Debug existant pour permettre la mise à jour des installations précédentes. La clé privée de signature n’est pas publiée. Cette vérification ciblée n’est pas un audit de sécurité exhaustif.
