"""Derive metadata for a PDF from its filename, enriched by embedded metadata.

The filename convention (``Title-With-Hyphens_Author.pdf``) is the primary and
most reliable source for this library; embedded PDF metadata only fills gaps
because real-world title/author fields are frequently junk.
"""

from __future__ import annotations

import logging
import re
from pathlib import Path

# pypdf logs noisy parser warnings ("Object X not defined", "Multiple
# definitions...") for many real-world PDFs; they are non-fatal here.
logging.getLogger("pypdf").setLevel(logging.ERROR)

_YEAR_RE = re.compile(r"\b(?:19|20)\d{2}\b")


def parse_filename(filename: str) -> dict:
    """Parse a filename into metadata fields. Never raises."""
    stem = filename[:-4] if filename.lower().endswith(".pdf") else filename
    out: dict = {
        "descriptive_name": "",
        "author": "",
        "publisher": "",
        "isbn": "",
        "year": None,
        "category": "",
        "type": "",
        "tags": [],
    }

    parts = stem.split("_")
    title_token = parts[0]
    trailing = parts[1:]

    author_tokens: list[str] = []
    for tok in trailing:
        low = tok.lower()
        if low == "duplicate":
            out["tags"].append("duplicate")
        elif low == "shortform":
            out["tags"].append("shortform")
        elif low.startswith("arxiv"):
            out["type"] = "paper"
            out["tags"].append("arxiv")
        else:
            author_tokens.append(tok)

    if author_tokens:
        # The trailing token closest to the title is the author segment.
        names = [n for n in author_tokens[-1].split("-") if n]
        out["author"] = ", ".join(names)

    out["descriptive_name"] = title_token.replace("-", " ").strip()

    m = _YEAR_RE.search(title_token)
    if m:
        out["year"] = int(m.group(0))

    return out


def enrich_from_pdf(path: Path) -> dict:
    """Read embedded metadata + page count via pypdf. Never raises."""
    out: dict = {}
    try:
        from pypdf import PdfReader

        reader = PdfReader(str(path))
        try:
            out["page_count"] = len(reader.pages)
        except Exception:
            pass
        md = reader.metadata
        if md:
            title = (md.title or "").strip()
            author = (md.author or "").strip()
            if title:
                out["pdf_title"] = title
            if author:
                out["pdf_author"] = author
            try:
                cd = md.creation_date
                if cd:
                    out["pdf_year"] = cd.year
            except Exception:
                pass
    except Exception:
        pass
    return out


def extract(path: Path, filename: str) -> dict:
    """Full metadata for a file: filename parse, gap-filled by embedded data."""
    base = parse_filename(filename)
    pdf = enrich_from_pdf(path)

    if not base["author"] and pdf.get("pdf_author"):
        base["author"] = pdf["pdf_author"][:200]
    if not base["descriptive_name"] and pdf.get("pdf_title"):
        base["descriptive_name"] = pdf["pdf_title"][:300]
    if not base["year"] and pdf.get("pdf_year"):
        base["year"] = pdf["pdf_year"]
    base["page_count"] = pdf.get("page_count")
    return base
