import pytest

from pdfman.services.extractor import parse_filename


@pytest.mark.parametrize(
    "filename, title, author",
    [
        ("Accelerated-C++_Koenig-Moo.pdf", "Accelerated C++", "Koenig, Moo"),
        ("7-Morning-Routines-of-Tech-CEOs.pdf", "7 Morning Routines of Tech CEOs", ""),
        (
            "Operating-Systems-Three-Easy-Pieces_Arpaci-Dusseau.pdf",
            "Operating Systems Three Easy Pieces",
            "Arpaci, Dusseau",
        ),
        ("Making-Embedded-Systems_White.pdf", "Making Embedded Systems", "White"),
    ],
)
def test_title_and_author(filename, title, author):
    meta = parse_filename(filename)
    assert meta["descriptive_name"] == title
    assert meta["author"] == author


def test_arxiv_token():
    meta = parse_filename("The-Prompt-Report_arXiv-2406.06608v3.pdf")
    assert meta["type"] == "paper"
    assert "arxiv" in meta["tags"]
    assert meta["author"] == ""


def test_duplicate_token():
    meta = parse_filename("100-Productivity-Hacks_Bailey_DUPLICATE.pdf")
    assert meta["author"] == "Bailey"
    assert "duplicate" in meta["tags"]
    assert meta["descriptive_name"] == "100 Productivity Hacks"


def test_year_from_title():
    assert parse_filename("Linux-Bible-2006_Negus.pdf")["year"] == 2006
