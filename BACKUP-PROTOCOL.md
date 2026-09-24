# Sauvegarde partagée Linux / Android

Disponible dans Notestr Android 1.2.5. Compatibilité du format vérifiée dans les deux sens avec Linux ; candidat testé sur smartphone. Voir SECURITY-REVIEW.md et VALIDATION.md pour la portée des essais.

## Utilisation

À chaque publication d’une note existante, son contenu précédemment publié devient l’unique sauvegarde. Elle est chiffrée et conservée dans le cache local ainsi que sur les relais qui l’acceptent. Une nouvelle note n’a pas encore de sauvegarde.

Dans l’éditeur, **Version précédente** demande confirmation avant de remplacer le texte. La sauvegarde est chargée dans les modes Visuel et Markdown. **Publier** confirme la restauration en conservant l’identifiant original ; la version remplacée devient alors la sauvegarde. Une annulation ou l’absence de sauvegarde ne modifie pas le texte.

Une actualisation récupère les sauvegardes écrites par l’autre application. Les deux applications doivent utiliser le même compte et au moins un relais commun qui accepte le kind de sauvegarde. Le texte de la sauvegarde n’apparaît pas comme une seconde note dans la liste.

## Contrat d’échange

| Champ | Valeur |
| --- | --- |
| Note courante | kind `33457`, premier tag `d` conservé exactement |
| Sauvegarde | kind `30078` |
| Adresse de sauvegarde | `notestr/previous/` suivi du SHA-256 hexadécimal minuscule de l’identifiant de note encodé en UTF-8 |
| Tag `e` | ID de l’événement de la version précédente |
| Contenu | Reprise exacte du contenu déjà chiffré NIP-44 v2 de la version précédente |
| Auteur et signature | Même compte que la note ; signature vérifiée avant utilisation |
| Horodatage de la sauvegarde | Celui de la nouvelle version de la note |
| Remplacement | Plus grand `created_at`, puis plus petit ID en cas d’égalité, comme sous Linux |
| Suppression | kind `5`, coordonnées `a` de la note et de la sauvegarde |

L’adresse dérivée et l’ID de l’événement précédent sont des métadonnées publiques. Le contenu reste chiffré ; ce mécanisme ne masque pas l’existence ou les dates des modifications.

## Publication et conservation

1. Préparer et signer la note modifiée et sa sauvegarde (clé locale ou Amber).
2. Envoyer la sauvegarde aux relais et attendre leurs accusés positifs.
3. La conserver en local par une écriture atomique. Un échec du disque interrompt l’envoi de la note modifiée.
4. Envoyer la note modifiée uniquement aux relais ayant accepté sa sauvegarde.
5. Fusionner la note acceptée dans le cache. Signaler une éventuelle erreur locale après publication, sans présenter l’envoi déjà accepté comme un refus réseau.

Un refus total de la sauvegarde empêche l’envoi de la nouvelle note. Un refus de la nouvelle note conserve le texte dans l’éditeur. Les deux publications ne constituent pas une transaction atomique : certains relais peuvent posséder une sauvegarde sans avoir accepté la mise à jour.

Le cache garde une seule sauvegarde par adresse, fusionne les réponses des relais avec les événements locaux et vérifie aussi les signatures des données relues sur disque. Une réponse vide ou incomplète ne remplace plus tout le cache. Les demandes de suppression sont interprétées avec leur date, sans ressusciter une ancienne version supprimée.

## Limites

- Les relais peuvent refuser, oublier ou conserver des événements remplacés ; une seule sauvegarde active ne garantit pas l’effacement physique de toutes les copies.
- La récupération Android reste bornée à 2 000 événements par kind et par requête, sans pagination exhaustive. Une sauvegarde ancienne peut manquer sur une nouvelle installation au-delà de cette borne ; les données déjà en cache sont conservées.
- Les modifications simultanées sur plusieurs appareils suivent la sélection Nostr ; aucune fusion des textes n’est effectuée.
- Le test de compatibilité porte sur le code Linux local disponible le 23 septembre 2026, pas sur tous les clients Nostr.
