"""Console entrypoint: launch the local web server."""

from __future__ import annotations


def main() -> None:
    import uvicorn

    from .config import get_settings

    settings = get_settings()
    uvicorn.run("pdfman.app:app", host=settings.server.host, port=settings.server.port)


if __name__ == "__main__":
    main()
