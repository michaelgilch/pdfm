"""Scan trigger, scan-status polling, and health check."""

from __future__ import annotations

import asyncio

from fastapi import APIRouter, Request

from .. import scanstate
from ..templating import templates

router = APIRouter()


@router.post("/admin/rescan")
async def rescan(request: Request):
    if scanstate.state["status"] != "scanning":
        scanstate.state["status"] = "scanning"
        scanstate.state["report"] = None
        asyncio.create_task(scanstate.run_scan())
    return templates.TemplateResponse(
        request, "partials/scan_status.html", {"scan": scanstate.state}
    )


@router.get("/admin/scan-status")
async def scan_status(request: Request):
    return templates.TemplateResponse(
        request, "partials/scan_status.html", {"scan": scanstate.state}
    )


@router.get("/healthz")
def healthz():
    return {"status": "ok"}
