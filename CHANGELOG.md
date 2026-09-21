# Historique de Notestr Android

## 1.2.4 — 21 septembre 2026

- Ajout du logo sur l’écran de lancement de l’application.
- Correction du chevauchement du menu de sélection Android avec la barre de mise en forme.
- Correction de la disparition de la barre de mise en forme pendant l’édition d’une note.

Maintenance : Kotlin 2.4.20 ; DOMPurify intégré ancien supprimé et remplacé par DOMPurify 3.4.15, commun aux chemins de filtrage HTML. Distribution Gradle vérifiée par SHA-256.

APK complet non débogable, versionCode 14, certificat identique aux versions précédentes. La version locale de test nommée 1.2.5 est regroupée dans cette publication 1.2.4 et n’est pas une release distincte.

- [Télécharger l’APK complet 1.2.4](https://github.com/gerfondev/notestr-android/releases/download/v1.2.4/Notestr-Android.apk)
- Tag : `v1.2.4`.
- SHA-256 : `d07486c5189289c87c2d30af4ee5ce44f5f794daf2fc911385ea89fbfbc66e25`.

## 1.2.3 — 20 septembre 2026

- Remplacement du champ de langage des blocs de code par un bouton Copier en mode Visuel.
- Copie du texte seul dans le presse-papiers Android, avec conservation des lignes vides, espaces et caractères spéciaux. Confirmation « Copié ! ».
- Boutons séparés du document éditable : aucun ajout au Markdown ni modification des langages déjà renseignés. Le langage reste modifiable en mode Markdown.

APK complet, versionCode 11. Signature identique aux versions précédentes.

- [Télécharger l’APK complet 1.2.3](https://github.com/gerfondev/notestr-android/releases/download/v1.2.3/Notestr-Android.apk)
- Tag : `v1.2.3`.
- SHA-256 : `335edf1786ea8fd7935cae6b1f75375bc228572042efa0c1dd94a5d063bfc17c`.
- Vérifications : 8 tests unitaires, tests navigateur et 3 tests Android de l’éditeur réussis.


## 1.2.2 — 19 septembre 2026

Correction d’un bug avec Amber lors de la publication d’une nouvelle note ou d’une note modifiée. Si une autorisation n’est pas mémorisée, Notestr ouvre Amber puis reprend le chiffrement et la signature après accord. L’éditeur et la session sont conservés pendant la demande. Les refus mémorisés sont respectés.

APK complet, versionCode 10 (versionName 1.2.2), signature identique à 1.2.1. Les données existantes sont conservées lors d’une installation par-dessus.

- [Télécharger l’APK complet 1.2.2](https://blossom.primal.net/dc7a6ec3b398f3ca6abbfca75405a3c15284481e8f82a11b535259d6ff4acac1)
- Tag : `v1.2.2`.
- SHA-256 : `dc7a6ec3b398f3ca6abbfca75405a3c15284481e8f82a11b535259d6ff4acac1`.
- Vérifications : 8 tests unitaires et 7 tests Android Amber avec signataire simulé. Le fonctionnement avec Amber réel reste à confirmer sur téléphone.

## 1.2.1 — 18 septembre 2026

Correction du mode Visuel, ajout du déverrouillage biométrique et d’une icône. Inclut les corrections des titres et du menu H1–H6.

- [Télécharger l’APK complet 1.2.1](https://blossom.primal.net/b2650dd82b5b96eaa9628baac46ae7742a125abd9aed74e1539dd6ecc5981e6b)
- Tag : `v1.2.1`.
- Commit source : `78114f3555bee5854f056513ae0f53cfb3fd3c5c`.
- SHA-256 : `b2650dd82b5b96eaa9628baac46ae7742a125abd9aed74e1539dd6ecc5981e6b`.

## Versions antérieures

Les versions de développement antérieures à 1.2.1 n’ont pas été publiées séparément dans ce dépôt ngit. Leurs changements intégrés sont décrits dans VALIDATION.md ; aucun tag historique artificiel n’a été ajouté.
