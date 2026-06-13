import shutil

from pdfman import db
from pdfman.services import scanner
from pdfman.services import tags as tags_svc


def test_new_file_inserted(env, make_pdf):
    make_pdf(env.library.dir / "Clean-Code_Martin.pdf")
    report = scanner.scan(env)
    assert report.new == 1
    conn = db.connect(env.db_path)
    row = conn.execute("SELECT * FROM pdf").fetchone()
    assert row["file_name"] == "Clean-Code_Martin.pdf"
    assert row["descriptive_name"] == "Clean Code"
    assert row["page_count"] == 1
    conn.close()


def test_rename_keeps_id_and_metadata(env, make_pdf):
    lib = env.library.dir
    original = lib / "Clean-Code_Martin.pdf"
    make_pdf(original)
    scanner.scan(env)

    conn = db.connect(env.db_path)
    row = conn.execute("SELECT * FROM pdf").fetchone()
    pdf_id, md5 = row["id"], row["md5"]
    tags_svc.set_tags(conn, pdf_id, ["keepme"])
    conn.commit()
    conn.close()

    original.rename(lib / "Clean-Code-2nd-Ed_Martin.pdf")
    report = scanner.scan(env)
    assert report.renamed == 1
    assert report.new == 0

    conn = db.connect(env.db_path)
    rows = conn.execute("SELECT * FROM pdf").fetchall()
    assert len(rows) == 1
    assert rows[0]["id"] == pdf_id
    assert rows[0]["md5"] == md5
    assert rows[0]["file_name"] == "Clean-Code-2nd-Ed_Martin.pdf"
    assert tags_svc.tags_for(conn, pdf_id) == ["keepme"]
    conn.close()


def test_duplicate_not_double_inserted(env, make_pdf):
    lib = env.library.dir
    make_pdf(lib / "original.pdf")
    shutil.copyfile(lib / "original.pdf", lib / "copy.pdf")
    report = scanner.scan(env)
    assert report.new == 1
    assert report.duplicates == 1
    conn = db.connect(env.db_path)
    assert conn.execute("SELECT COUNT(*) AS c FROM pdf").fetchone()["c"] == 1
    conn.close()


def test_missing_file_soft_deleted(env, make_pdf):
    f = env.library.dir / "x_Author.pdf"
    make_pdf(f)
    scanner.scan(env)
    f.unlink()
    report = scanner.scan(env)
    assert report.missing == 1
    conn = db.connect(env.db_path)
    assert conn.execute("SELECT present FROM pdf").fetchone()["present"] == 0
    conn.close()


def test_fastpath_skips_rehash(env, make_pdf, monkeypatch):
    make_pdf(env.library.dir / "a_Author.pdf")
    scanner.scan(env)

    calls = []
    real = scanner.hashing.md5_of
    monkeypatch.setattr(
        scanner.hashing, "md5_of", lambda *a, **k: (calls.append(1), real(*a, **k))[1]
    )
    scanner.scan(env)
    assert calls == []


def test_edits_survive_rescan(env, make_pdf):
    make_pdf(env.library.dir / "Clean-Code_Martin.pdf")
    scanner.scan(env)
    conn = db.connect(env.db_path)
    pid = conn.execute("SELECT id FROM pdf").fetchone()["id"]
    conn.execute("UPDATE pdf SET descriptive_name = ? WHERE id = ?", ("My Title", pid))
    conn.commit()
    conn.close()

    scanner.scan(env)
    conn = db.connect(env.db_path)
    assert conn.execute("SELECT descriptive_name FROM pdf WHERE id = ?", (pid,)).fetchone()[0] == "My Title"
    conn.close()
