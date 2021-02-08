package PDFManager

import org.junit.*

import PDFManager.domain.PdfData

class PdfmTest {

    @Test
    void TestPdfm() {
        def pdfController = new Pdfm()
        assert pdfController.getGreeting() == 'Hello World from the Controller!'
    }

    @Test
    void TestAddDbEntry() {
        def pdfController = new Pdfm()
        PdfData.withNewSession {
            println PdfData.count
        }
    }
}
