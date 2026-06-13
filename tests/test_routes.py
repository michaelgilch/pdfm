from fastapi.testclient import TestClient

from pdfman import db
from pdfman.app import create_app
from pdfman.services import scanner


def _seed(env, make_pdf):
    make_pdf(env.library.dir / "Clean-Code_Martin.pdf")
    scanner.scan(env)
    conn = db.connect(env.db_path)
    pid = conn.execute("SELECT id FROM pdf").fetchone()["id"]
    conn.close()
    return pid


def test_library_page_lists_pdf(env, make_pdf):
    _seed(env, make_pdf)
    with TestClient(create_app()) as client:
        r = client.get("/")
        assert r.status_code == 200
        assert "Clean Code" in r.text


def test_edit_persists_and_search_finds(env, make_pdf):
    pid = _seed(env, make_pdf)
    with TestClient(create_app()) as client:
        r = client.put(
            f"/pdfs/{pid}",
            data={
                "descriptive_name": "Clean Code",
                "author": "Robert C. Martin",
                "publisher": "",
                "isbn": "",
                "year": "2008",
                "category": "",
                "type": "book",
                "tags": "craft, classic",
            },
        )
        assert r.status_code == 200
        assert "Robert C. Martin" in r.text

        # Tag now searchable and filterable.
        assert "Clean Code" in client.get("/pdfs", params={"q": "craft"}).text
        assert "Clean Code" in client.get("/pdfs", params={"tag": "classic"}).text

    conn = db.connect(env.db_path)
    row = conn.execute("SELECT year, type FROM pdf WHERE id = ?", (pid,)).fetchone()
    assert row["year"] == 2008
    assert row["type"] == "book"
    conn.close()


def test_pdf_file_streams_with_range(env, make_pdf):
    pid = _seed(env, make_pdf)
    with TestClient(create_app()) as client:
        r = client.get(f"/pdfs/{pid}/file")
        assert r.status_code == 200
        assert r.headers["content-type"] == "application/pdf"

        r2 = client.get(f"/pdfs/{pid}/file", headers={"Range": "bytes=0-9"})
        assert r2.status_code == 206
        assert len(r2.content) == 10


def test_healthz(env):
    with TestClient(create_app()) as client:
        assert client.get("/healthz").json() == {"status": "ok"}
