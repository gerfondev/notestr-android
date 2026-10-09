# État et reconstruction de l’éditeur

Le dépôt TOAST UI Editor 3.2.2 est archivé. Pour corriger l’avis GHSA-c8x8-7fp4-3x9w / CVE-2026-104847, le bundle est reconstruit depuis le tag amont `editor@3.2.2` (commit `9b94c04231d4600b42347ff1b99d9813e9becf67`) avec les versions stables suivantes : prosemirror-view 1.42.6, model 1.25.12, state 1.4.4, transform 1.12.2, commands 1.7.2, history 1.5.1, inputrules 1.5.1, keymap 1.2.3. L’avis concerne les versions antérieures à 1.42.3 ; le correctif est dans la branche incluse.

DOMPurify 3.4.16 reste une ressource séparée : le bundle reconstruit externalise ce module au profit de la copie chargée dans `index.html`. Le bundle n’intègre pas une seconde copie du sanitizer. Les mentions de licence amont sont conservées.

La suite de tests amont archivée n’est pas entièrement compatible avec les versions récentes de ProseMirror et DOMPurify ; elle n’est pas annoncée comme réussie. Les tests de l’application, la construction webpack de production et les tests visuels/intégration du paquet final sont consignés dans `SECURITY-REVIEW.md`. Cette reconstruction ne prouve pas que tout le dépôt amont archivé est exempt de défauts.

Pour assurer la compatibilité du bundle avec son API, ToastMark a été recompilé depuis le workspace source du même checkout amont. La distribution npm alpha fournit une API incomplète pour l’éditeur (absence de `Renderer`) et n’est donc pas intégrée. Le bundle final inclut l’export `Renderer` et a été contrôlé sur Android et Linux.

ToastMark émet `linebreak` pour les retours Markdown explicites ; TOAST UI Editor 3.2.2 attendait l’ancien type `softbreak`. Le convertisseur reconstruit traite les deux types afin de conserver les retours de paragraphe en mode visuel.
