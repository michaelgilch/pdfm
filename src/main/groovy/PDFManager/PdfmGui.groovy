package PDFManager

class PdfmGui {

    PdfmGui() {
        println 'Hello World from the GUI!'
    }

    static void main(String[] args) {
        new PdfmGui()
        def pdfmController = new Pdfm()
        println pdfmController.getGreeting()
    }
}
