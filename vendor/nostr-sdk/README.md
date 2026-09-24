# SDK Nostr reconstruit pour Notestr Android

Artefact local : `nostr-sdk-0.45.1-notestr.1.aar`. Il ne s’agit pas de l’AAR officiel inchangé : les bibliothèques natives sont reconstruites avec les dépendances corrigées du verrou Cargo joint. Le code amont, les classes JVM et les bindings Kotlin sont conservés. Le minimum Android est aligné sur l’application (API 26).

`provenance.json` contient la révision amont, les URL et les empreintes des téléchargements officiels. L’archive source complète reste un cache local ignoré par Git : les exemples amont contiennent des clés de démonstration qui ne sont pas redistribuées dans ce dépôt ni dans l’APK. `native-build.json` contient les empreintes effectivement produites ; `abi-verification.json` atteste le contrat UniFFI et les 522 sommes de contrôle d’API vérifiées. `SHA256SUMS` est contrôlé automatiquement avant chaque compilation Android.

## Reconstruction

Installer Rust/rustup, Python 3.12 ou ultérieur et le NDK exact indiqué dans `provenance.json`. Ajouter les cibles Android au compilateur 1.95.0 :

```sh
rustup toolchain install 1.95.0 --profile minimal
rustup target add --toolchain 1.95.0 aarch64-linux-android armv7-linux-androideabi x86_64-linux-android i686-linux-android
python3 tools/rebuild-nostr-sdk.py --ndk /chemin/vers/android-ndk-r30 --source-dir /tmp/notestr-sdk-rebuild
```

Le script télécharge et vérifie l’archive source épinglée, utilise `Cargo.lock` avec `--locked`, télécharge et vérifie les AAR/sources officiels, compile, vérifie l’interface et crée l’artefact local. Il ne publie rien. Les variables habituelles `CARGO_HOME` et `RUSTUP_HOME` permettent une chaîne isolée. `--package-only` vérifie et empaquette une reconstruction déjà effectuée. Les chemins de construction sont remappés ; les binaires sont liés avec alignement de page 16 Ko.

La procédure fixe les entrées et permet une reconstruction contrôlée ; une reproductibilité binaire indépendante sur une seconde machine n’a pas été démontrée. Les essais sur émulateur ne remplacent pas le test sur smartphone. Voir `../../SECURITY-REVIEW.md` pour les résultats et limites du contrôle de sécurité.
