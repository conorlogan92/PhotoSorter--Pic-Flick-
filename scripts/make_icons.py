"""Generate native package icons from resources/images/picflick-logo.png."""
from pathlib import Path
import sys
from PIL import Image

source = Path('resources/images/picflick-logo.png')
if not source.is_file():
    raise SystemExit(f'Missing branding image: {source}')

img = Image.open(source).convert('RGBA')
# Retain transparent padding and the full PicFlick wordmark within a square.
canvas = Image.new('RGBA', (1024, 1024), (0, 0, 0, 0))
img.thumbnail((940, 940), Image.Resampling.LANCZOS)
canvas.alpha_composite(img, ((1024 - img.width) // 2, (1024 - img.height) // 2))
out = Path('target/icons')
out.mkdir(parents=True, exist_ok=True)
if sys.platform == 'darwin':
    iconset = out / 'PicFlick.iconset'
    iconset.mkdir(exist_ok=True)
    for size in [16, 32, 128, 256, 512]:
        canvas.resize((size, size), Image.Resampling.LANCZOS).save(iconset / f'icon_{size}x{size}.png')
        canvas.resize((size * 2, size * 2), Image.Resampling.LANCZOS).save(iconset / f'icon_{size}x{size}@2x.png')
    import subprocess
    subprocess.run(['iconutil', '-c', 'icns', str(iconset), '-o', str(out / 'PicFlick.icns')], check=True)
else:
    canvas.save(out / 'PicFlick.ico', format='ICO', sizes=[(16, 16), (32, 32), (48, 48), (64, 64), (128, 128), (256, 256)])
