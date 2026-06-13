"""Full-page HTML routes."""

from __future__ import annotations

import sqlite3

from fastapi import APIRouter, Depends, HTTPException, Request

from .. import queries, scanstate
from ..config import get_settings
from ..deps import get_db
from ..services import tags as tags_svc
from ..templating import templates

router = APIRouter()


@router.get("/")
def library(request: Request, conn: sqlite3.Connection = Depends(get_db)):
    settings = get_settings()
    ctx = {
        "pdfs": queries.list_pdfs(conn),
        "view": settings.ui.default_view,
        "q": "",
        "sel_tags": [],
        "sel_types": [],
        "sel_year": None,
        "sort": "name",
        "tags": tags_svc.all_tags_with_counts(conn),
        "types": queries.type_facets(conn),
        "years": queries.year_facets(conn),
        "scan": scanstate.state,
    }
    return templates.TemplateResponse(request, "library.html", ctx)


@router.get("/pdfs/{pdf_id}")
def detail(pdf_id: int, request: Request, conn: sqlite3.Connection = Depends(get_db)):
    pdf = queries.get_pdf(conn, pdf_id)
    if pdf is None:
        raise HTTPException(status_code=404, detail="PDF not found")
    return templates.TemplateResponse(request, "detail.html", {"p": pdf})
