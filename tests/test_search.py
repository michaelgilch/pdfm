from pdfman import db
from pdfman.services import scanner, search
from pdfman.services import tags as tags_svc


def test_metadata_search(env, make_pdf):
    lib = env.library.dir
    make_pdf(lib / "Algorithms-Unlocked_Cormen.pdf")
    make_pdf(lib / "Clean-Code_Martin.pdf")
    scanner.scan(env)

    conn = db.connect(env.db_path)
    ids = search.search_ids(conn, "algorithms")
    titles = [
        conn.execute("SELECT descriptive_name FROM pdf WHERE id = ?", (i,)).fetchone()[0]
        for i in ids
    ]
    assert any("Algorithms" in t for t in titles)
    assert not any("Clean" in t for t in titles)
    conn.close()


def test_reindex_after_tagging(env, make_pdf):
    make_pdf(env.library.dir / "Clean-Code_Martin.pdf")
    scanner.scan(env)
    conn = db.connect(env.db_path)
    pid = conn.execute("SELECT id FROM pdf").fetchone()["id"]

    assert search.search_ids(conn, "favorite") == []
    tags_svc.set_tags(conn, pid, ["favorite"])
    search.reindex(conn, pid)
    conn.commit()
    assert pid in search.search_ids(conn, "favorite")
    conn.close()
