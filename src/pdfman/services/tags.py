"""Normalized tag storage (tag + pdf_tag join table)."""

from __future__ import annotations

import sqlite3
from collections.abc import Iterable


def get_or_create(conn: sqlite3.Connection, name: str) -> int | None:
    name = name.strip()
    if not name:
        return None
    row = conn.execute("SELECT id FROM tag WHERE name = ? COLLATE NOCASE", (name,)).fetchone()
    if row:
        return row["id"]
    cur = conn.execute("INSERT INTO tag(name) VALUES (?)", (name,))
    return cur.lastrowid


def set_tags(conn: sqlite3.Connection, pdf_id: int, names: Iterable[str]) -> None:
    """Replace all tags on a PDF. Tags are normalized to lowercase."""
    conn.execute("DELETE FROM pdf_tag WHERE pdf_id = ?", (pdf_id,))
    seen: set[str] = set()
    for raw in names:
        name = raw.strip().lower()
        if not name or name in seen:
            continue
        seen.add(name)
        tid = get_or_create(conn, name)
        if tid is not None:
            conn.execute(
                "INSERT OR IGNORE INTO pdf_tag(pdf_id, tag_id) VALUES (?, ?)", (pdf_id, tid)
            )


def tags_for(conn: sqlite3.Connection, pdf_id: int) -> list[str]:
    rows = conn.execute(
        "SELECT t.name FROM tag t JOIN pdf_tag pt ON pt.tag_id = t.id "
        "WHERE pt.pdf_id = ? ORDER BY t.name",
        (pdf_id,),
    ).fetchall()
    return [r["name"] for r in rows]


def tag_names_snapshot(conn: sqlite3.Connection, pdf_id: int) -> str:
    """Space-joined tag names, for the search index."""
    return " ".join(tags_for(conn, pdf_id))


def all_tags_with_counts(conn: sqlite3.Connection) -> list[sqlite3.Row]:
    return conn.execute(
        "SELECT t.name AS name, COUNT(*) AS cnt "
        "FROM tag t JOIN pdf_tag pt ON pt.tag_id = t.id "
        "JOIN pdf p ON p.id = pt.pdf_id "
        "WHERE p.present = 1 "
        "GROUP BY t.id ORDER BY t.name"
    ).fetchall()
