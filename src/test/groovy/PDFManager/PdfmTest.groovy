package PDFManager

import org.junit.*

class PdfmTest {

    @Test
    void TestPdfm() {
        def pdfController = new Pdfm()
        assert pdfController.getGreeting() == 'Hello World from the Controller!'
    }
}
