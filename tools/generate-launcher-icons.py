"""Generate Android launcher resources from the supplied original artwork (Pillow)."""
from pathlib import Path
from PIL import Image
root = Path(__file__).resolve().parents[1]
res = root / 'app/src/main/res'
art = Image.open(root / 'artwork/notestr.png').convert('RGBA')
art = art.crop(art.getbbox())
for density, factor in [('mdpi',1),('hdpi',1.5),('xhdpi',2),('xxhdpi',3),('xxxhdpi',4)]:
    folder = res / f'mipmap-{density}'
    folder.mkdir(parents=True, exist_ok=True)
    foreground = Image.new('RGBA', (round(108*factor),)*2)
    scaled = art.copy()
    scaled.thumbnail((round(66*factor),)*2, Image.Resampling.LANCZOS)
    foreground.alpha_composite(scaled, ((foreground.width-scaled.width)//2,(foreground.height-scaled.height)//2))
    foreground.save(folder/'ic_launcher_foreground.png')
    legacy = Image.new('RGBA', (round(48*factor),)*2)
    scaled = art.copy()
    scaled.thumbnail((round(44*factor),)*2, Image.Resampling.LANCZOS)
    legacy.alpha_composite(scaled, ((legacy.width-scaled.width)//2,(legacy.height-scaled.height)//2))
    legacy.save(folder/'ic_launcher.png')
folder = res / 'mipmap-anydpi-v26'
folder.mkdir(parents=True, exist_ok=True)
(folder/'ic_launcher.xml').write_text('''<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background" />
    <foreground android:drawable="@mipmap/ic_launcher_foreground" />
</adaptive-icon>
''')
(res/'values/ic_launcher_colors.xml').write_text('''<?xml version="1.0" encoding="utf-8"?>
<resources><color name="ic_launcher_background">#F5F0FC</color></resources>
''')
