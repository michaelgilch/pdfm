package PDFManager

// allow references to logInfo() rather than LogHelper.logInfo()
import static PDFManager.utils.LogHelper.*

class PdfmGui {

    PdfmGui() {
        println 'Hello World from the GUI!'
    }

    static void main(String[] args) {
        new PdfmGui()
        def pdfmController = new Pdfm()
        logInfo(pdfmController.getGreeting())
    }
}
