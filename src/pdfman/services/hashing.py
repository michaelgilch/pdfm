"""Content fingerprinting and cheap change detection."""

from __future__ import annotations

import hashlib
from pathlib import Path

_CHUNK = 1 << 20  # 1 MiB


def md5_of(path: Path, chunk_size: int = _CHUNK) -> str:
    h = hashlib.md5()
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(chunk_size), b""):
            h.update(chunk)
    return h.hexdigest()


def quick_stat(path: Path) -> tuple[int, float]:
    """Return (size_bytes, mtime) for the fast-path that avoids re-hashing."""
    st = path.stat()
    return st.st_size, st.st_mtime
