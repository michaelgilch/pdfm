PRAGMA journal_mode = WAL;
PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS pdf (
  id               INTEGER PRIMARY KEY,
  md5              TEXT NOT NULL UNIQUE,        -- identity: rename + dedup key
  file_name        TEXT NOT NULL,               -- current basename
  rel_path         TEXT NOT NULL,               -- path relative to library root
  size_bytes       INTEGER NOT NULL,
  mtime            REAL NOT NULL,               -- fast-path change detection
  page_count       INTEGER,
  descriptive_name TEXT,                          -- human title; auto-seeded from filename
  author           TEXT,
  publisher        TEXT,
  isbn             TEXT,
  year             INTEGER,
  category         TEXT,
  type             TEXT,                          -- book/manual/cheatsheet/paper/...
  present          INTEGER NOT NULL DEFAULT 1,   -- 0 = file missing on last scan (metadata kept)
  created_at       TEXT NOT NULL DEFAULT (datetime('now')),
  updated_at       TEXT NOT NULL DEFAULT (datetime('now'))
);
CREATE INDEX IF NOT EXISTS idx_pdf_type ON pdf(type);
CREATE INDEX IF NOT EXISTS idx_pdf_year ON pdf(year);

CREATE TABLE IF NOT EXISTS tag (
  id   INTEGER PRIMARY KEY,
  name TEXT NOT NULL UNIQUE COLLATE NOCASE
);

CREATE TABLE IF NOT EXISTS pdf_tag (
  pdf_id INTEGER NOT NULL REFERENCES pdf(id) ON DELETE CASCADE,
  tag_id INTEGER NOT NULL REFERENCES tag(id) ON DELETE CASCADE,
  PRIMARY KEY (pdf_id, tag_id)
);
CREATE INDEX IF NOT EXISTS idx_pdftag_tag ON pdf_tag(tag_id);

-- Full-text search. rowid is kept equal to pdf.id and maintained from
-- services/search.py (delete+insert) on every metadata or tag change.
CREATE VIRTUAL TABLE IF NOT EXISTS pdf_fts USING fts5(
  descriptive_name,
  author,
  publisher,
  file_name,
  tags,
  body,
  tokenize = "unicode61 remove_diacritics 2"
);

CREATE TABLE IF NOT EXISTS schema_meta (key TEXT PRIMARY KEY, value TEXT);
