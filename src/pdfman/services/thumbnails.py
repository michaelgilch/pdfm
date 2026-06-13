"""Cover-image thumbnails via poppler's pdftoppm, cached by md5."""

from __future__ import annotations

import shutil
import subprocess
from pathlib import Path

_SCALE = 400  # longest edge, px


def thumb_path(thumbs_dir: Path, md5: str) -> Path:
    return thumbs_dir / f"{md5}.png"


def ensure_thumb(thumbs_dir: Path, md5: str, pdf_path: Path) -> Path | None:
    """Return the cached cover thumbnail, generating it on first request.

    Returns None if generation fails (missing pdftoppm, corrupt PDF, etc.).
    """
    out = thumb_path(thumbs_dir, md5)
    if out.exists():
        return out
    if shutil.which("pdftoppm") is None or not pdf_path.exists():
        return None
    thumbs_dir.mkdir(parents=True, exist_ok=True)
    prefix = out.with_suffix("")  # pdftoppm -singlefile appends ".png"
    try:
        subprocess.run(
            [
                "pdftoppm", "-png", "-f", "1", "-l", "1",
                "-singlefile", "-scale-to", str(_SCALE),
                str(pdf_path), str(prefix),
            ],
            check=True,
            capture_output=True,
            timeout=60,
        )
    except (subprocess.SubprocessError, OSError):
        return None
    return out if out.exists() else None
