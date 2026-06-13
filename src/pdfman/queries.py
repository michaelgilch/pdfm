"""Read queries and facets backing the views (kept thin and testable)."""

from __future__ import annotations

import sqlite3
from collections.abc import Sequence

from .services import search
from .services import tags as tags_svc


def _row_to_view(conn: sqlite3.Connection, row: sqlite3.Row) -> dict:
    d = dict(row)
    d["tags"] = tags_svc.tags_for(conn, row["id"])
    d["title"] = d.get("descriptive_name") or d.get("file_name") or "(untitled)"
    return d


def get_pdf(conn: sqlite3.Connection, pdf_id: int) -> dict | None:
    row = conn.execute("SELECT * FROM pdf WHERE id = ?", (pdf_id,)).fetchone()
    return _row_to_view(conn, row) if row else None


def _sort_key(sort: str):
    if sort == "author":
        return lambda r: (r.get("author") or "").lower()
    if sort == "year":
        return lambda r: (r.get("year") or 0)
    if sort == "pages":
        return lambda r: (r.get("page_count") or 0)
    return lambda r: (r.get("descriptive_name") or r.get("file_name") or "").lower()


def list_pdfs(
    conn: sqlite3.Connection,
    *,
    q: str | None = None,
    tag_names: Sequence[str] = (),
    types: Sequence[str] = (),
    year: int | None = None,
    sort: str = "name",
    limit: int = 1000,
) -> list[dict]:
    where = ["present = 1"]
    params: list = []
    order_ids: list[int] | None = None

    if q:
        order_ids = search.search_ids(conn, q, limit=limit)
        if not order_ids:
            return []
        where.append(f"id IN ({','.join('?' * len(order_ids))})")
        params.extend(order_ids)

    if tag_names:
        names = list({t for t in tag_names if t})
        placeholders = ",".join("?" * len(names))
        where.append(
            f"id IN (SELECT pt.pdf_id FROM pdf_tag pt JOIN tag t ON t.id = pt.tag_id "
            f"WHERE t.name IN ({placeholders}) "
            f"GROUP BY pt.pdf_id HAVING COUNT(DISTINCT t.name) = ?)"
        )
        params.extend(names)
        params.append(len(names))

    if types:
        tlist = list(types)
        where.append(f"type IN ({','.join('?' * len(tlist))})")
        params.extend(tlist)

    if year:
        where.append("year = ?")
        params.append(year)

    sql = "SELECT * FROM pdf WHERE " + " AND ".join(where) + f" LIMIT {int(limit)}"
    rows = [_row_to_view(conn, r) for r in conn.execute(sql, params).fetchall()]

    if order_ids is not None:
        pos = {pid: i for i, pid in enumerate(order_ids)}
        rows.sort(key=lambda r: pos.get(r["id"], len(pos)))
    else:
        rows.sort(key=_sort_key(sort))
    return rows


def type_facets(conn: sqlite3.Connection) -> list[sqlite3.Row]:
    return conn.execute(
        "SELECT type, COUNT(*) AS cnt FROM pdf "
        "WHERE present = 1 AND type IS NOT NULL AND type <> '' "
        "GROUP BY type ORDER BY type"
    ).fetchall()


def year_facets(conn: sqlite3.Connection) -> list[int]:
    rows = conn.execute(
        "SELECT DISTINCT year FROM pdf WHERE present = 1 AND year IS NOT NULL ORDER BY year DESC"
    ).fetchall()
    return [r["year"] for r in rows]
