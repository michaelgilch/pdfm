"""Incremental, MD5-based filesystem scan.

Classifies each PDF as new, renamed/moved, duplicate, or unchanged, and marks
rows whose files have disappeared as not-present (soft delete) without ever
discarding hand-entered metadata.
"""

from __future__ import annotations

import sqlite3
from collections.abc import Iterator
from dataclasses import dataclass, field
from pathlib import Path

from .. import db
from ..config import Settings
from . import extractor, hashing, search, tags as tags_svc

_MTIME_EPS = 1e-6


@dataclass
class ScanReport:
    new: int = 0
    renamed: int = 0
    duplicates: int = 0
    missing: int = 0
    unchanged: int = 0
    errors: list[str] = field(default_factory=list)

    def as_dict(self) -> dict:
        return {
            "new": self.new,
            "renamed": self.renamed,
            "duplicates": self.duplicates,
            "missing": self.missing,
            "unchanged": self.unchanged,
            "errors": list(self.errors),
        }


def iter_pdf_files(root: Path, recursive: bool) -> Iterator[Path]:
    if not root.exists():
        return
    walker = root.rglob("*") if recursive else root.iterdir()
    for p in walker:
        if p.is_file() and p.suffix.lower() == ".pdf":
            yield p


def _insert_new(conn: sqlite3.Connection, md5: str, path: Path, rel: str, size: int, mtime: float) -> int:
    meta = extractor.extract(path, path.name)
    cur = conn.execute(
        "INSERT INTO pdf(md5, file_name, rel_path, size_bytes, mtime, page_count, "
        "descriptive_name, author, publisher, isbn, year, category, type, present) "
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1)",
        (
            md5, path.name, rel, size, mtime, meta.get("page_count"),
            meta.get("descriptive_name"), meta.get("author"), meta.get("publisher"),
            meta.get("isbn"), meta.get("year"), meta.get("category"), meta.get("type"),
        ),
    )
    pdf_id = cur.lastrowid
    if meta.get("tags"):
        tags_svc.set_tags(conn, pdf_id, meta["tags"])
    search.reindex(conn, pdf_id)
    return pdf_id


def scan(settings: Settings) -> ScanReport:
    report = ScanReport()
    root = settings.library.dir
    conn = db.connect(settings.db_path)
    seen_ids: set[int] = set()
    try:
        for path in iter_pdf_files(root, settings.library.recursive):
            try:
                rel = str(path.relative_to(root))
                size, mtime = hashing.quick_stat(path)

                row = conn.execute("SELECT * FROM pdf WHERE rel_path = ?", (rel,)).fetchone()
                if (
                    row is not None
                    and row["present"] == 1
                    and row["size_bytes"] == size
                    and abs(row["mtime"] - mtime) < _MTIME_EPS
                ):
                    seen_ids.add(row["id"])
                    report.unchanged += 1
                    continue

                md5 = hashing.md5_of(path)
                existing = conn.execute("SELECT * FROM pdf WHERE md5 = ?", (md5,)).fetchone()

                if existing is None:
                    pdf_id = _insert_new(conn, md5, path, rel, size, mtime)
                    conn.commit()
                    seen_ids.add(pdf_id)
                    report.new += 1
                    continue

                seen_ids.add(existing["id"])

                if existing["rel_path"] == rel:
                    # Same path, content unchanged but size/mtime differ (e.g. touched).
                    conn.execute(
                        "UPDATE pdf SET size_bytes = ?, mtime = ?, present = 1, "
                        "updated_at = datetime('now') WHERE id = ?",
                        (size, mtime, existing["id"]),
                    )
                    conn.commit()
                    report.unchanged += 1
                    continue

                old_still_present = (root / existing["rel_path"]).exists()
                if old_still_present and existing["present"] == 1:
                    # The original copy still exists, so this file is a duplicate.
                    report.duplicates += 1
                    continue

                # Original is gone -> this is a rename/move. Keep all metadata.
                conn.execute(
                    "UPDATE pdf SET file_name = ?, rel_path = ?, size_bytes = ?, mtime = ?, "
                    "present = 1, updated_at = datetime('now') WHERE id = ?",
                    (path.name, rel, size, mtime, existing["id"]),
                )
                search.reindex(conn, existing["id"])
                conn.commit()
                report.renamed += 1
            except Exception as exc:  # noqa: BLE001 - per-file isolation
                report.errors.append(f"{path.name}: {exc}")

        # Soft-delete rows whose files were not seen this scan.
        present_rows = conn.execute("SELECT id FROM pdf WHERE present = 1").fetchall()
        for r in present_rows:
            if r["id"] not in seen_ids:
                conn.execute(
                    "UPDATE pdf SET present = 0, updated_at = datetime('now') WHERE id = ?",
                    (r["id"],),
                )
                report.missing += 1
        conn.commit()
    finally:
        conn.close()
    return report
