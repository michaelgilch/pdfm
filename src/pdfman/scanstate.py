"""Shared scan state and a single-flight trigger used by routes and lifespan."""

from __future__ import annotations

import asyncio

from .config import get_settings
from .services import scanner

_lock = asyncio.Lock()

state: dict = {"status": "idle", "report": None}


async def run_scan() -> bool:
    """Run one scan unless one is already in progress. Returns False if busy."""
    if _lock.locked():
        return False
    async with _lock:
        state["status"] = "scanning"
        try:
            settings = get_settings()
            report = await asyncio.to_thread(scanner.scan, settings)
            state["report"] = report.as_dict()
        finally:
            state["status"] = "idle"
    return True


async def interval_loop(minutes: int) -> None:
    while True:
        await asyncio.sleep(minutes * 60)
        await run_scan()
