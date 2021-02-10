package PDFManager

import PDFManager.uiComponents.*

// allow references to logInfo() rather than LogHelper.logInfo()
import static PDFManager.utils.LogHelper.*

import groovy.swing.SwingBuilder

import static java.awt.Component.CENTER_ALIGNMENT
import static java.awt.Component.LEFT_ALIGNMENT
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.*

import static javax.swing.WindowConstants.EXIT_ON_CLOSE
import javax.swing.*

class PdfmGui {
    static String APP_TITLE = 'pdfm'
    static int DEFAULT_GUI_WIDTH = 1200
    static int DEFAULT_GUI_HEIGHT = 600
    static def DEFAULT_GUI_LOCATION = [150, 150]
    static Dimension DEFAULT_GUI_SIZE = new Dimension(DEFAULT_GUI_WIDTH, DEFAULT_GUI_HEIGHT)

    static int COMPONENT_SPACING = 5
    static int STANDARD_HBOX_HEIGHT = 50
    static def SEARCH_BOX_SIZE = [250, 30]

    //static Color EVEN_ITEM_COLOR = new Color(0, 0, 0, 0)
    //static Color ODD_ITEM_COLOR = new Color(0,0,0,16)
    static Color SELECTED_ITEM_COLOR = new Color(0, 185, 255, 128)

    Pdfm pdfmController
    SwingBuilder swingBuilder

    static Font textBoxFont = new Font('Arial', Font.BOLD, 12)
    static Font pdfTitleFont = new Font('Arial', Font.BOLD, 12)

    def gui = [
            mainWindow: null,
            refreshButton: null,
            searchBox: null,
            searchButton: null,
            scrollablePdfList: null,
            selectedItemPanel: null,
            openButton: null,
    ]

    def selectedItem = ""

    PdfmGui() {

        pdfmController = new Pdfm()

        swingBuilder = new SwingBuilder()

        swingBuilder.build {
            //lookAndFeel(UIManager.getSystemLookAndFeelClassName())
            lookAndFeel('nimbus')
            gui.mainWindow = frame(title: APP_TITLE, location: DEFAULT_GUI_LOCATION, size: DEFAULT_GUI_SIZE, defaultCloseOperation: EXIT_ON_CLOSE) {
                panel(border: emptyBorder(COMPONENT_SPACING)) {
                    boxLayout(axis: BoxLayout.Y_AXIS)
                        hbox(alignmentX: CENTER_ALIGNMENT, border: emptyBorder(COMPONENT_SPACING), preferredSize: [DEFAULT_GUI_WIDTH, STANDARD_HBOX_HEIGHT], minimumSize: [DEFAULT_GUI_WIDTH, STANDARD_HBOX_HEIGHT], maximumSize: [DEFAULT_GUI_WIDTH * 2, STANDARD_HBOX_HEIGHT]) {
                            gui.refreshButton = button(new Button('Refresh'), actionPerformed: { refreshFileList() })
                            glue()
                            gui.searchBox = textField(text: '', font: textBoxFont, minimumSize: SEARCH_BOX_SIZE, preferredSize: SEARCH_BOX_SIZE, maximumSize: SEARCH_BOX_SIZE)
                            hstrut(COMPONENT_SPACING)
                            gui.searchButton = button(new Button('Search'), actionPerformed: { refreshFileList() })
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
                        gui.openButton = button(new Button('Open'), enabled: false, actionPerformed: { openPdf() })
                    }
                }
            }
        }
        refreshFileList()
        gui.mainWindow.setVisible(true)
    }

    def refreshFileList() {
        def pdfDomainObjects = pdfmController.getListOfPdfs()
        swingBuilder.edt {
            scrollablePdfListContents = vbox() {
                //def objCount = 0
                pdfDomainObjects.each { pdfDomainObj ->
                    //objCount++
                    def panelId = "panel" + pdfDomainObj.id
                    hbox(background: Color.BLUE, alignmentX: LEFT_ALIGNMENT, border: lineBorder(color: Color.LIGHT_GRAY, thickness: 1), maximumSize: [DEFAULT_GUI_WIDTH * 2, STANDARD_HBOX_HEIGHT]) {
                        gui.selectedItemPanel = panel(id: panelId, background: Color.WHITE, alignmentX: LEFT_ALIGNMENT, border: emptyBorder(COMPONENT_SPACING), maximumSize: [DEFAULT_GUI_WIDTH * 2, STANDARD_HBOX_HEIGHT]) {
                            boxLayout(axis: BoxLayout.Y_AXIS)
                            hbox(alignmentX: LEFT_ALIGNMENT, border: emptyBorder(COMPONENT_SPACING), maximumSize: [DEFAULT_GUI_WIDTH * 2, STANDARD_HBOX_HEIGHT]) {
                                hstrut(COMPONENT_SPACING * 2)
                                label(new Label(pdfDomainObj.fileName), font: pdfTitleFont)
                            }
                        }
                    }

//                    if (objCount % 2 == 0) {
//                        gui.selectedItemPanel.setBackground(EVEN_ITEM_COLOR)
//                        //println gui.selectedItemPanel.getBackground().getAlpha()
//                    } else {
//                        gui.selectedItemPanel.setBackground(ODD_ITEM_COLOR)
//                        //println gui.selectedItemPanel.getBackground().getAlpha()
//                    }

                    gui.selectedItemPanel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mousePressed(MouseEvent e) {
                            if (selectedItem == panelId) {
                                swingBuilder."$panelId".setBackground(Color.WHITE)
                                selectedItem = ""
                                gui.openButton.setEnabled(false)
                            } else {
                                if (selectedItem != "") {
                                    swingBuilder."$selectedItem".setBackground(Color.WHITE)
                                    selectedItem = ""
                                    gui.openButton.setEnabled(false)
                                }
                                swingBuilder."$panelId".setBackground(SELECTED_ITEM_COLOR)
                                selectedItem = panelId
                                gui.openButton.setEnabled(true)
                            }
                        }
                    })
                }
            }

            gui.scrollablePdfList.setViewportView(scrollablePdfListContents)
        }
    }

    def editPdfAttributes() {
        logInfo('TODO edit PDF attributes')
    }

    def sendPdfToRemarkable() {
        logInfo('TODO send PDF to Remarkable2')
    }

    def openPdf() {
        pdfmController.openPdfById(selectedItem.replace('panel','').toInteger())
    }

    static void main(String[] args) {
        new PdfmGui()

    }
}
