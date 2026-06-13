# pdfm

A local-first web app to index, tag, search, and read a personal PDF library.

I keep a large folder of PDFs (books, manuals, cheat sheets, papers). pdfm scans
that folder, auto-extracts what it can from each file, and gives me a browser UI
to browse covers, filter by tag/type, full-text search, edit metadata, and read
PDFs inline. It runs entirely on `localhost` — nothing leaves the machine.

> This is a ground-up rewrite of the original 2021 Groovy/Swing/H2 app. That
> version is preserved at the `archive/groovy-swing` git tag.

## Features

- **Folder scan with rename detection.** Files are fingerprinted by MD5, so moving
  or renaming a PDF keeps its metadata instead of creating a duplicate record.
- **Auto-seeded metadata.** Title and author are parsed from the filename
  convention (`Title-With-Hyphens_Author.pdf`); page count and embedded
  title/author/year are read with `pypdf`. You only edit the exceptions.
- **Cover thumbnails** rendered with poppler's `pdftoppm`, cached by content hash.
- **Tagging and faceted filtering** (normalized tags + type facets).
- **Full-text search** over metadata via SQLite FTS5 (optionally over PDF text).
- **In-browser reading** — PDFs open in the browser's native viewer; no external app.
- **Duplicate and missing-file detection** surfaced after each scan.

## Requirements

- Python 3.12+
- [`uv`](https://docs.astral.sh/uv/)
- `pdftoppm` (poppler-utils) on `PATH` — only needed for cover thumbnails

## Run

```sh
./run.sh           # starts the server and opens the browser
# or
uv run pdfman      # starts the server only
```

Then open <http://127.0.0.1:8000>. `uv` creates the virtualenv and installs
dependencies on first run.

## Configuration

Defaults work out of the box (library at `/data/pdf-library`). To change anything,
copy `config.example.toml` to `config.toml` and edit. Every value can also be set
via environment variables, e.g. `PDFMAN_LIBRARY__DIR=/some/path uv run pdfman`.

| Section | Key | Default | Meaning |
| --- | --- | --- | --- |
| `library` | `dir` | `/data/pdf-library` | Folder containing the PDFs |
| `library` | `recursive` | `true` | Also scan subfolders |
| `scan` | `interval_minutes` | `0` | Auto-rescan interval (`0` = manual/on-startup only) |
| `scan` | `on_startup` | `true` | Scan once when the server starts |
| `server` | `host` / `port` | `127.0.0.1` / `8000` | Bind address |
| `ui` | `default_view` | `grid` | `grid` or `list` |
| `features` | `extract_fulltext` | `false` | Index PDF text for search (slower scans) |

## Development

```sh
uv run pytest      # run the test suite
```

Runtime data (SQLite DB, thumbnail cache) lives under `data/` and is gitignored.

## Stack

FastAPI · stdlib `sqlite3` (+ FTS5) · Jinja2 · HTMX · pypdf · poppler. No ORM,
no build step, no JavaScript framework.
