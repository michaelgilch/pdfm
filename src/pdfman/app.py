"""FastAPI application factory and lifespan wiring."""

from __future__ import annotations

import asyncio
from contextlib import asynccontextmanager
from pathlib import Path

from fastapi import FastAPI
from fastapi.staticfiles import StaticFiles

from . import db, scanstate
from .config import get_settings
from .routes import admin, files, pages, pdfs


@asynccontextmanager
async def lifespan(app: FastAPI):
    settings = get_settings()
    db.init_db(settings.db_path)
    settings.thumbs_dir.mkdir(parents=True, exist_ok=True)

    tasks: list[asyncio.Task] = []
    if settings.scan.on_startup:
        scanstate.state["status"] = "scanning"
        tasks.append(asyncio.create_task(scanstate.run_scan()))
    if settings.scan.interval_minutes > 0:
        tasks.append(asyncio.create_task(scanstate.interval_loop(settings.scan.interval_minutes)))

    try:
        yield
    finally:
        for t in tasks:
            t.cancel()


def create_app() -> FastAPI:
    app = FastAPI(title="pdfman", lifespan=lifespan)
    static_dir = Path(__file__).parent / "static"
    static_dir.mkdir(parents=True, exist_ok=True)
    app.mount("/static", StaticFiles(directory=str(static_dir)), name="static")
    app.include_router(pages.router)
    app.include_router(pdfs.router)
    app.include_router(files.router)
    app.include_router(admin.router)
    return app


app = create_app()
