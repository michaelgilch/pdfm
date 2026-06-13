"""FTS5 search over PDF metadata (and optional full text)."""

from __future__ import annotations

import re
import sqlite3

from . import tags as tags_svc

# bm25 column weights, matching pdf_fts column order:
# descriptive_name, author, publisher, file_name, tags, body
_WEIGHTS = (10.0, 6.0, 3.0, 4.0, 5.0, 1.0)


def _prepare(query: str) -> str:
    """Turn a user query into a safe FTS5 MATCH expression (AND of prefix terms)."""
    terms = []
    for tok in re.split(r"\s+", query.strip()):
        tok = tok.replace('"', " ").strip()
        if tok:
            terms.append(f'"{tok}"*')
    return " ".join(terms)


def reindex(conn: sqlite3.Connection, pdf_id: int, body: str | None = None) -> None:
    """Rebuild the FTS row for one PDF. Preserves existing body if not supplied."""
    row = conn.execute(
        "SELECT descriptive_name, author, publisher, file_name FROM pdf WHERE id = ?",
        (pdf_id,),
    ).fetchone()
    if row is None:
        return
    if body is None:
        old = conn.execute("SELECT body FROM pdf_fts WHERE rowid = ?", (pdf_id,)).fetchone()
        body = old["body"] if old else ""
    tag_snapshot = tags_svc.tag_names_snapshot(conn, pdf_id)
    conn.execute("DELETE FROM pdf_fts WHERE rowid = ?", (pdf_id,))
    conn.execute(
        "INSERT INTO pdf_fts(rowid, descriptive_name, author, publisher, file_name, tags, body) "
        "VALUES (?, ?, ?, ?, ?, ?, ?)",
        (
            pdf_id,
            row["descriptive_name"] or "",
            row["author"] or "",
            row["publisher"] or "",
            row["file_name"] or "",
            tag_snapshot,
            body or "",
        ),
    )


def search_ids(conn: sqlite3.Connection, query: str, limit: int = 1000) -> list[int]:
    """Return pdf ids matching the query, best-ranked first."""
    expr = _prepare(query)
    if not expr:
        return []
    try:
        rows = conn.execute(
            "SELECT rowid FROM pdf_fts WHERE pdf_fts MATCH ? "
            f"ORDER BY bm25(pdf_fts, {', '.join(str(w) for w in _WEIGHTS)}) LIMIT ?",
            (expr, limit),
        ).fetchall()
    except sqlite3.OperationalError:
        return []
    return [r["rowid"] for r in rows]
