from __future__ import annotations

from pathlib import Path

import pytest
from pypdf import PdfWriter


def _make_pdf(path: Path, pages: int = 1) -> None:
    """Write a small valid PDF. Title metadata = stem keeps content unique."""
    writer = PdfWriter()
    for _ in range(max(1, pages)):
        writer.add_blank_page(width=200, height=300)
    writer.add_metadata({"/Title": path.stem})
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(path, "wb") as f:
        writer.write(f)


@pytest.fixture
def make_pdf():
    return _make_pdf


@pytest.fixture
def env(tmp_path, monkeypatch):
    """A Settings pointed at temp library + data dirs, with on-startup scan off."""
    lib = tmp_path / "lib"
    lib.mkdir()
    data = tmp_path / "data"
    data.mkdir()
    monkeypatch.setenv("PDFMAN_LIBRARY__DIR", str(lib))
    monkeypatch.setenv("PDFMAN_DATA_DIR", str(data))
    monkeypatch.setenv("PDFMAN_SCAN__ON_STARTUP", "false")

    from pdfman import db
    from pdfman.config import get_settings

    get_settings.cache_clear()
    settings = get_settings()
    db.init_db(settings.db_path)
    yield settings
    get_settings.cache_clear()
