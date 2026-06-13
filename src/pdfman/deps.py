"""FastAPI dependencies."""

from __future__ import annotations

import sqlite3
from collections.abc import Iterator

from . import db
from .config import get_settings


def get_db() -> Iterator[sqlite3.Connection]:
    conn = db.connect(get_settings().db_path)
    try:
        yield conn
    finally:
        conn.close()
