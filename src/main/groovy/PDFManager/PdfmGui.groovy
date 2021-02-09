package PDFManager

import PDFManager.uiComponents.*

// allow references to logInfo() rather than LogHelper.logInfo()
import static PDFManager.utils.LogHelper.*

import groovy.swing.SwingBuilder

import static java.awt.Component.CENTER_ALIGNMENT

import java.awt.*

import static javax.swing.WindowConstants.EXIT_ON_CLOSE
import javax.swing.*

class PdfmGui {
    static String APP_TITLE = 'pdfm'
    static int DEFAULT_GUI_WIDTH = 1200
    static int DEFAULT_GUI_HEIGHT = 600
    static def DEFAULT_GUI_LOCATION = [150, 150]
    static def DEFAULT_GUI_SIZE = [DEFAULT_GUI_WIDTH, DEFAULT_GUI_HEIGHT]

    static int COMPONENT_SPACING = 5
    static int STANDARD_HBOX_HEIGHT = 50
    static def STANDARD_BUTTON_SIZE = [100, 30]
    static def SEARCH_BOX_SIZE = [250, 30]

    SwingBuilder swingBuilder

    static Font textBoxFont = new Font('Arial', Font.BOLD, 12)

    def gui = [
            mainWindow: null,
            refreshButton: null,
            searchBox: null,
            searchButton: null,
            scrollablePdfList: null,
    ]

    PdfmGui() {
        swingBuilder = new SwingBuilder()

        swingBuilder.build {
            lookAndFeel("nimbus")
            gui.mainWindow = frame(title: APP_TITLE, location: DEFAULT_GUI_LOCATION, size: DEFAULT_GUI_SIZE, defaultCloseOperation: EXIT_ON_CLOSE) {
                panel(border: emptyBorder(COMPONENT_SPACING)) {
                    boxLayout(axis: BoxLayout.Y_AXIS)
                    hbox(alignmentX: CENTER_ALIGNMENT, border: emptyBorder(COMPONENT_SPACING), preferredSize: [DEFAULT_GUI_WIDTH, STANDARD_HBOX_HEIGHT], minimumSize: [DEFAULT_GUI_WIDTH, STANDARD_HBOX_HEIGHT], maximumSize: [DEFAULT_GUI_WIDTH * 2, STANDARD_HBOX_HEIGHT]) {
                        gui.refreshButton = button( new Button('Refresh'), actionPerformed: { refreshFileList() })
                        glue()
                        gui.searchBox = textField(text: '', font: textBoxFont, minimumSize: SEARCH_BOX_SIZE, preferredSize: SEARCH_BOX_SIZE, maximumSize: SEARCH_BOX_SIZE)
                        hstrut(COMPONENT_SPACING)
                        gui.searchButton = button( new Button('Search'), actionPerformed: { refreshFileList() })
                    }
                    hbox(alignmentX: CENTER_ALIGNMENT, border: emptyBorder(COMPONENT_SPACING), preferredSize: DEFAULT_GUI_SIZE) {
                        gui.scrollablePdfList = scrollPane(
                                verticalScrollBar: scrollBar(
                                        blockIncrement: 20,
                                        unitIncrement: 20
                                )
                        )
                    }
                    hbox(alignmentX: CENTER_ALIGNMENT, border: emptyBorder(COMPONENT_SPACING), preferredSize: [DEFAULT_GUI_WIDTH, STANDARD_HBOX_HEIGHT], minimumSize: [DEFAULT_GUI_WIDTH, STANDARD_HBOX_HEIGHT], maximumSize: [DEFAULT_GUI_WIDTH * 2, STANDARD_HBOX_HEIGHT]) {
                        glue()
                        button(new Button('Edit'), actionPerformed: { editPdfAttributes() })
                        hstrut(COMPONENT_SPACING)
                        button(new Button('Send'), actionPerformed: { sendPdfToRemarkable() })
                        hstrut(COMPONENT_SPACING)
                        button(new Button('Open'), actionPerformed: { openPdf() })
                    }
                }
            }
        }
        gui.mainWindow.setVisible(true)
    }

    def refreshFileList() {
        logInfo('TODO refresh file list')
    }

    def editPdfAttributes() {
        logInfo('TODO edit PDF attributes')
    }

    def sendPdfToRemarkable() {
        logInfo('TODO send PDF to Remarkable2')
    }

    def openPdf() {
        logInfo('TODO open PDF')
    }

    static void main(String[] args) {
        new PdfmGui()
        def pdfmController = new Pdfm()
    }
}
