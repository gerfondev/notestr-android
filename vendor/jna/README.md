# JNA 5.19.1 — copie aux métadonnées anonymisées

L’archive officielle est vérifiée par SHA-256 avant transformation. Quatre bibliothèques natives contenaient un chemin personnel de construction du fournisseur dans leur nom de version ELF de base. Le script `tools/sanitize-jna-metadata.py` remplace uniquement ce nom par un chemin neutre, de même longueur, et recalcule le hash ELF correspondant. Il vérifie que toutes les sections exécutables sont inchangées. Les classes JVM, symboles JNI, alignements et licences sont conservés.

Le chemin personnel original n’est pas reproduit dans le rapport. Les empreintes amont et transformées figurent dans `provenance.json`. Ce fichier est un artefact local distinct de l’AAR officiel ; Gradle vérifie son empreinte avant compilation.

Reproduction : télécharger l’AAR depuis l’URL de `provenance.json`, puis exécuter `python3 tools/sanitize-jna-metadata.py /chemin/vers/jna-5.19.1.aar` depuis la racine du projet. Le script refuse toute autre empreinte d’entrée. Aucune publication ni modification du code machine n’est effectuée par cet outil.
