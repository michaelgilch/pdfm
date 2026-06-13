"""Raw PDF streaming (for the in-browser viewer) and cover thumbnails."""

from __future__ import annotations

import sqlite3

from fastapi import APIRouter, Depends, HTTPException, Response
from fastapi.responses import FileResponse

from ..config import get_settings
from ..deps import get_db
from ..services import thumbnails

router = APIRouter()

_PLACEHOLDER_SVG = (
    b'<svg xmlns="http://www.w3.org/2000/svg" width="200" height="260">'
    b'<rect width="100%" height="100%" fill="#e9e9ee"/>'
    b'<text x="50%" y="50%" font-family="sans-serif" font-size="22" fill="#9a9aa5" '
    b'text-anchor="middle" dominant-baseline="middle">PDF</text></svg>'
)


def _row_and_path(conn: sqlite3.Connection, pdf_id: int):
    row = conn.execute(
        "SELECT rel_path, md5 FROM pdf WHERE id = ?", (pdf_id,)
    ).fetchone()
    if row is None:
        raise HTTPException(status_code=404)
    return row, get_settings().library.dir / row["rel_path"]


@router.get("/pdfs/{pdf_id}/file")
def pdf_file(pdf_id: int, conn: sqlite3.Connection = Depends(get_db)):
    _row, path = _row_and_path(conn, pdf_id)
    if not path.exists():
        raise HTTPException(status_code=404, detail="file missing on disk")
    return FileResponse(path, media_type="application/pdf", content_disposition_type="inline")


@router.get("/pdfs/{pdf_id}/thumb")
def pdf_thumb(pdf_id: int, conn: sqlite3.Connection = Depends(get_db)):
    row, path = _row_and_path(conn, pdf_id)
    thumb = thumbnails.ensure_thumb(get_settings().thumbs_dir, row["md5"], path)
    if thumb is None:
        return Response(content=_PLACEHOLDER_SVG, media_type="image/svg+xml")
    return FileResponse(thumb, media_type="image/png")
