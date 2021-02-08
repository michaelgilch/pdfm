# pdfm

PDFM will one day be a PDF Manager. I have a large library of PDF files including books, manuals, cheat sheets, and documents. For years, I have struggled to keep them organized. 
- Does a book about using Swing in Groovy go in a 'groovy' folder? 'swing' folder? 'java' folder? 
- Does the Dymo Printer Manual go in a 'manuals' folder or 'printers' folder?
- Does a cheatsheet on setting up a gitignore file for a gradle project go in 'gradle' or 'git'?

The idea is to have a single folder containing all of my PDFs with a GUI to categorize, tag, and search. Fundamentally, a file browser for PDFs with tagging.

# What I want to be when I grow up

- A single folder will contain all of my PDF files, which I index.
- On command, and/or at set intervals (via user configuration), the folder will be scanned for changes (additions, deletions, renames).
- The full list of PDFs will be displayed in a UI along with options to Open, Edit Attributes, Search, and maybe Send to Remarkable2.
- Attributes will be stored for each PDF including:
    - filename
    - human readable filename (BW_Manual.pdf = Bob's Widget Manual)
    - author
    - publisher (if applicable)
    - year
    - type (book, manual, cheatsheet, etc)
    - tags (groovy, gradle, java, c++, hibernate, etc)
- There will be filters for specific attributes and search functionality.

# Technologies to consider

- Groovy
- Gradle for building
- Hibernate with H2 Database for storing attributes
- Swing for UI
- SLF4J with logback for logging
- JUnit4 for testing



