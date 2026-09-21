# Local security patch

TOAST UI Editor 3.2.2 is retained for Markdown compatibility. Its upstream repository is archived. The DOMPurify 2.3.3 module embedded in the distributed bundle has been removed entirely and replaced by a reference to the separately loaded DOMPurify 3.4.15. The page loads purify.min.js before toastui-editor.js. Both the default sanitizer and the application sanitizer now use that same implementation. Initialization fails if DOMPurify is unavailable.

Reproduce using tools/externalize-editor-sanitizer.py with the original bundle and an output path. Expected original SHA-256: `f31d7876111587cc3296e6e76de5e81aa5f1b02c949601e213d227c1281d4721`.

The upstream MIT license and separate DOMPurify notices remain applicable. This patch is not a claim that the entire archived editor is free of vulnerabilities.
