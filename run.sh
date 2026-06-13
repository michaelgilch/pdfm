#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
PORT="${PDFMAN_SERVER__PORT:-8000}"
# Open the browser shortly after the server starts.
( sleep 1.5 && xdg-open "http://127.0.0.1:${PORT}" >/dev/null 2>&1 || true ) &
exec uv run pdfman
