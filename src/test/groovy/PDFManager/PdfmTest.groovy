package PDFManager

import org.junit.*

import PDFManager.domain.PdfData

class PdfmTest {

    def initializeTestConfig() {
        // TODO write test initialization
    }

    @Test
    void TestCheckFilesystemForChanges() {
        def pdfmController = new Pdfm('src/test/resources/test_config.properties')

        pdfmController.checkFilesystemForChanges()

        PdfData.withNewSession {
            assert PdfData.count == 1
        }
    }
}
