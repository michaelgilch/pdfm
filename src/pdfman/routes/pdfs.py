"""HTMX partial routes: result list, metadata view, edit form, save."""

from __future__ import annotations

import sqlite3

from fastapi import APIRouter, Depends, Form, HTTPException, Query, Request

from .. import queries
from ..deps import get_db
from ..services import search
from ..services import tags as tags_svc
from ..templating import templates

router = APIRouter()


def _parse_year(raw: str) -> int | None:
    raw = (raw or "").strip()
    return int(raw) if raw.isdigit() and 1000 <= int(raw) <= 9999 else None


@router.get("/pdfs")
def results(
    request: Request,
    q: str = Query(""),
    tag: list[str] = Query(default=[]),
    type: list[str] = Query(default=[]),
    year: int | None = Query(default=None),
    view: str = Query("grid"),
    sort: str = Query("name"),
    conn: sqlite3.Connection = Depends(get_db),
):
    pdfs = queries.list_pdfs(
        conn, q=q or None, tag_names=tag, types=type, year=year, sort=sort
    )
    return templates.TemplateResponse(
        request, "partials/results.html", {"pdfs": pdfs, "view": view}
    )


@router.get("/pdfs/{pdf_id}/meta")
def meta(pdf_id: int, request: Request, conn: sqlite3.Connection = Depends(get_db)):
    pdf = queries.get_pdf(conn, pdf_id)
    if pdf is None:
        raise HTTPException(status_code=404)
    return templates.TemplateResponse(request, "partials/metadata.html", {"p": pdf})


@router.get("/pdfs/{pdf_id}/edit")
def edit(pdf_id: int, request: Request, conn: sqlite3.Connection = Depends(get_db)):
    pdf = queries.get_pdf(conn, pdf_id)
    if pdf is None:
        raise HTTPException(status_code=404)
    return templates.TemplateResponse(
        request, "partials/edit_form.html", {"p": pdf, "tags_str": ", ".join(pdf["tags"])}
    )


@router.put("/pdfs/{pdf_id}")
def save(
    pdf_id: int,
    request: Request,
    descriptive_name: str = Form(""),
    author: str = Form(""),
    publisher: str = Form(""),
    isbn: str = Form(""),
    year: str = Form(""),
    category: str = Form(""),
    type: str = Form(""),
    tags: str = Form(""),
    conn: sqlite3.Connection = Depends(get_db),
):
    if conn.execute("SELECT 1 FROM pdf WHERE id = ?", (pdf_id,)).fetchone() is None:
        raise HTTPException(status_code=404)
    conn.execute(
        "UPDATE pdf SET descriptive_name = ?, author = ?, publisher = ?, isbn = ?, "
        "year = ?, category = ?, type = ?, updated_at = datetime('now') WHERE id = ?",
        (
            descriptive_name.strip(), author.strip(), publisher.strip(), isbn.strip(),
            _parse_year(year), category.strip(), type.strip(), pdf_id,
        ),
    )
    tags_svc.set_tags(conn, pdf_id, [t.strip() for t in tags.split(",") if t.strip()])
    search.reindex(conn, pdf_id)
    conn.commit()
    pdf = queries.get_pdf(conn, pdf_id)
    return templates.TemplateResponse(request, "partials/metadata.html", {"p": pdf})
