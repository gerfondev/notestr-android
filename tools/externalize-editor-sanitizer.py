"""Replace TOAST UI 3.2.2's bundled DOMPurify with the separately maintained copy.

Usage: python3 tools/externalize-editor-sanitizer.py ORIGINAL_BUNDLE OUTPUT_BUNDLE
The input must be the unmodified vendored TOAST UI 3.2.2 bundle.
"""
from pathlib import Path
import hashlib
import sys

source = Path(sys.argv[1]).read_bytes()
if hashlib.sha256(source).hexdigest() != "f31d7876111587cc3296e6e76de5e81aa5f1b02c949601e213d227c1281d4721":
    raise SystemExit("Unexpected upstream bundle; inspect it before changing the patch.")
text = source.decode()
start = text.index("368:function(e){")
end = text.index("},928:function(", start)
assert "DOMPurify 2.3.3" in text[start:end]
replacement = '368:function(e){if(!self.DOMPurify)throw new Error("DOMPurify must be loaded before the editor");e.exports=self.DOMPurify'
text = text[:start] + replacement + text[end:]
assert "DOMPurify 2.3.3" not in text
Path(sys.argv[2]).write_text(text)
