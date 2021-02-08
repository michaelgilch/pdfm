package PDFManager

// allow references to logInfo() rather than LogHelper.logInfo()
import static LogHelper.*

class Pdfm {
    String getGreeting() {
        logInfo('This is a test log')
        return 'Hello World from the Controller!'

    }
}
