package PDFManager

import PDFManager.domain.PdfData
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
    static Font labelFont = new Font('Arial', Font.PLAIN, 12)

    def gui = [
            mainWindow: null,
            refreshButton: null,
            searchBox: null,
            searchButton: null,
            scrollablePdfList: null,
            selectedItemPanel: null,
            openButton: null,
            editButton: null,
            editDialog: null,
            cancelButton: null,
            saveButton: null,
            displayNameField: null,
            tagField: null,
            typeField: null,
            categoryField: null,
            authorField: null,
            publisherField: null,
            yearField: null,
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
                        gui.editButton = button(new Button('Edit'), enabled: false, actionPerformed: { editPdfAttributes() })
                        hstrut(COMPONENT_SPACING)
                        gui.sendButton = button(new Button('Send'), enabled: false, actionPerformed: { sendPdfToRemarkable() })
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
                                swingBuilder."$selectedItem".setBackground(Color.WHITE)
                                selectedItem = ""
                                gui.openButton.setEnabled(false)
                                gui.editButton.setEnabled(false)
                                gui.sendButton.setEnabled(false)
                            } else {
                                if (selectedItem != "") {
                                    swingBuilder."$selectedItem".setBackground(Color.WHITE)
                                    selectedItem = ""
                                    gui.openButton.setEnabled(false)
                                    gui.editButton.setEnabled(false)
                                    gui.sendButton.setEnabled(false)
                                }
                                swingBuilder."$panelId".setBackground(SELECTED_ITEM_COLOR)
                                selectedItem = panelId
                                gui.openButton.setEnabled(true)
                                gui.editButton.setEnabled(true)
                                gui.sendButton.setEnabled(true)
                            }
                        }
                    })
                }
            }

            // retain selection on refresh
//            if (selectedItem != "") {
//                swingBuilder."$selectedItem".setBackground(SELECTED_ITEM_COLOR)
//            }

            gui.scrollablePdfList.setViewportView(scrollablePdfListContents)
        }
    }

    def editPdfAttributes() {
        logInfo('TODO edit PDF attributes')
        def selectedId = selectedItem.replace('panel', '').toInteger()
        PdfData pdf = pdfmController.getPdfObject(selectedId)
        swingBuilder.edt {
            gui.editDialog = frame(title: 'Edit', location: [250, 250], size: [800, 325]) {
                panel(border: emptyBorder(COMPONENT_SPACING)) {
                    boxLayout(axis: BoxLayout.Y_AXIS)
                    hbox(alignmentX: CENTER_ALIGNMENT, border: emptyBorder(COMPONENT_SPACING), maximumSize: [DEFAULT_GUI_WIDTH * 2, STANDARD_HBOX_HEIGHT]) {
                        label(new Label(pdf.fileName), font: pdfTitleFont)
                       glue()
                    }
                    hbox(alignmentX: CENTER_ALIGNMENT, border: emptyBorder(COMPONENT_SPACING), maximumSize: [DEFAULT_GUI_WIDTH * 2, STANDARD_HBOX_HEIGHT]) {
                        label(horizontalAlignment: JLabel.RIGHT, font: labelFont, text: 'Display Name:  ', minimumSize: [100, 30], preferredSize: [100, 30], maximumSize: [100, 30])
                        gui.displayNameField = textField(font: textBoxFont, text: pdf.descriptiveName, preferredSize: [DEFAULT_GUI_WIDTH, 30], maximumSize: [DEFAULT_GUI_WIDTH, 30])
                    }
                    hbox(alignmentX: CENTER_ALIGNMENT, border: emptyBorder(COMPONENT_SPACING), maximumSize: [DEFAULT_GUI_WIDTH * 2, STANDARD_HBOX_HEIGHT]) {
                        label(horizontalAlignment: JLabel.RIGHT, font: labelFont, text: 'Type:  ', minimumSize: [100, 30], preferredSize: [100, 30], maximumSize: [100, 30])
                        //gui.typeField = textField(font: textBoxFont, text: '<TYPE>', preferredSize: [DEFAULT_GUI_WIDTH, 30], maximumSize: [DEFAULT_GUI_WIDTH, 30])
                        gui.typeField = comboBox(font: textBoxFont, items: ['', 'Book', 'Manual', 'CheatSheet'], preferredSize: [DEFAULT_GUI_WIDTH, 30], maximumSize: [DEFAULT_GUI_WIDTH, 30])
                        glue()
                        label(horizontalAlignment: JLabel.RIGHT, font: labelFont, text: 'Category:  ', minimumSize: [100, 30], preferredSize: [100, 30], maximumSize: [100, 30])
                        gui.categoryField = comboBox(font: textBoxFont, items: ['', 'computer science', 'philosophy', 'productivity'], preferredSize: [DEFAULT_GUI_WIDTH, 30], maximumSize: [DEFAULT_GUI_WIDTH, 30])
                    }
                    hbox(alignmentX: CENTER_ALIGNMENT, border: emptyBorder(COMPONENT_SPACING), maximumSize: [DEFAULT_GUI_WIDTH * 2, STANDARD_HBOX_HEIGHT]) {
                        label(horizontalAlignment: JLabel.RIGHT, font: labelFont, text: 'Author:  ', minimumSize: [100, 30], preferredSize: [100, 30], maximumSize: [100, 30])
                        gui.authorField = textField(font: textBoxFont, text: '<AUTHOR>', preferredSize: [DEFAULT_GUI_WIDTH, 30], maximumSize: [DEFAULT_GUI_WIDTH, 30])
                        glue()
                        label(horizontalAlignment: JLabel.RIGHT, font: labelFont, text: 'Publisher:  ', minimumSize: [100, 30], preferredSize: [100, 30], maximumSize: [100, 30])
                        gui.publisherField = textField(font: textBoxFont, text: '<PUBLISHER>', preferredSize: [DEFAULT_GUI_WIDTH, 30], maximumSize: [DEFAULT_GUI_WIDTH, 30])
                        glue()
                        label(horizontalAlignment: JLabel.RIGHT, font: labelFont, text: 'Year:  ', minimumSize: [100, 30], preferredSize: [100, 30], maximumSize: [100, 30])
                        gui.yearField = textField(font: textBoxFont, text: '<YEAR>', minimumSize: [75, 30], preferredSize: [75, 30], maximumSize: [75, 30])
                    }
                    hbox(alignmentX: CENTER_ALIGNMENT, border: emptyBorder(COMPONENT_SPACING), maximumSize: [DEFAULT_GUI_WIDTH * 2, STANDARD_HBOX_HEIGHT * 2]) {
                        label(horizontalAlignment: JLabel.RIGHT, verticalAlignment: JLabel.TOP, font: labelFont, text: 'Tags:  ', minimumSize: [100, 90], preferredSize: [100, 90], maximumSize: [100, 90])
                        gui.tagField = textArea(wrapStyleWord: true, lineWrap: true, editable: true, font: textBoxFont, text: '', preferredSize: [DEFAULT_GUI_WIDTH, 90], maximumSize: [DEFAULT_GUI_WIDTH, 90], border: lineBorder(color: Color.GRAY, thickness: 1))
                    }
                    hbox(alignmentX: CENTER_ALIGNMENT, border: emptyBorder(COMPONENT_SPACING), maximumSize: [DEFAULT_GUI_WIDTH * 2, STANDARD_HBOX_HEIGHT]) {
                        gui.cancelButton = button(new Button('Cancel'), actionPerformed: { closeDialog(gui.editDialog) })
                        glue()
                        gui.saveButton = button(new Button('Save'), actionPerformed: { saveAttributeChanges(pdf, gui.displayNameField.getText(), gui.typeField.getSelectedItem(), gui.categoryField.getSelectedItem(), gui.authorField.getText(), gui.publisherField.getText(), gui.yearField.getText(), gui.tagField.getText()) })
                    }
                }
            }
        }
        gui.editDialog.setVisible(true)
    }

    def sendPdfToRemarkable() {
        logInfo('TODO send PDF to Remarkable2')
    }

    def openPdf() {
        pdfmController.openPdfById(selectedItem.replace('panel','').toInteger())
    }

    def closeDialog(dialog) {
        swingBuilder.edt {
            dialog.setVisible(false)
            dialog.dispose()
        }
    }

    def saveAttributeChanges(pdf, displayName, type, category, author, publisher, year, tags) {
        logInfo('TODO save changes')
        pdfmController.savePdfAttributeChanges(pdf, displayName, type, category, author, publisher, year, tags)
        closeDialog(gui.editDialog)
        refreshFileList()
    }

    static void main(String[] args) {
        new PdfmGui()

    }
}
