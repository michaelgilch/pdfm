package PDFManager

import PDFManager.domain.PdfData
import PDFManager.uiComponents.*

// allow references to logInfo() rather than LogHelper.logInfo()
import static PDFManager.utils.LogHelper.*

import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent

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

    static Font textBoxFont
    static Font labelFont
    static Font pdfTitleFont

    static def TYPE_ITEMS = ['', 'Book', 'Manual', 'Cheat Sheet']
    static def CATEGORY_ITEMS = ['', 'computer science', 'philosophy', 'productivity']

    Pdfm pdfmController
    SwingBuilder swingBuilder

    def categoryFilterModel = new DefaultListModel()
    def tagFilterModel = new DefaultListModel()

    def gui = [
            mainWindow: null,
            refreshButton: null,
            searchBox: null,
            searchButton: null,
            scrollablePdfList: null,
            selectedItemPanel: null,
            openButton: null,
            editButton: null,

            // Edit Attributes Dialog
            editDialog: null,
            displayNameField: null,
            tagField: null,
            typeField: null,
            categoryField: null,
            authorField: null,
            publisherField: null,
            isbnField: null,
            yearField: null,
            cancelButton: null,
            saveButton: null,

            // Filter Pane
            filterPane: null,
            tagList: null,
            clearFilterButton: null,
            refreshFilterButton: null,
    ]

    def selectedItem = ""

    PdfmGui() {
        pdfmController = new Pdfm()

        setupFonts()

        swingBuilder = new SwingBuilder()

        swingBuilder.build {
            //lookAndFeel(UIManager.getSystemLookAndFeelClassName())
            //lookAndFeel('nimbus')
            gui.mainWindow = frame(title: APP_TITLE, location: DEFAULT_GUI_LOCATION, size: DEFAULT_GUI_SIZE, defaultCloseOperation: EXIT_ON_CLOSE) {
                panel(border: emptyBorder(COMPONENT_SPACING)) {
                    boxLayout(axis: BoxLayout.Y_AXIS)
//                        hbox(alignmentX: CENTER_ALIGNMENT, border: emptyBorder(COMPONENT_SPACING), preferredSize: [DEFAULT_GUI_WIDTH, STANDARD_HBOX_HEIGHT], minimumSize: [DEFAULT_GUI_WIDTH, STANDARD_HBOX_HEIGHT], maximumSize: [DEFAULT_GUI_WIDTH * 2, STANDARD_HBOX_HEIGHT]) {
//                            gui.refreshButton = button(new Button('Refresh'), actionPerformed: { refreshFileList() })
//                            glue()
//                            gui.searchBox = textField(new TextField('', new Dimension(250,30)))
//                            hstrut(COMPONENT_SPACING)
//                            gui.searchButton = button(new Button('Search'), actionPerformed: { refreshFileList() })
//                        }
                    hbox(alignmentX: CENTER_ALIGNMENT, border: emptyBorder(COMPONENT_SPACING), preferredSize: DEFAULT_GUI_SIZE) {
                        gui.scrollablePdfList = scrollPane(
                                verticalScrollBar: scrollBar(
                                        blockIncrement: 20,
                                        unitIncrement: 20
                                )
                        )
                        hstrut(COMPONENT_SPACING * 2)
                        vbox() {
                            label(new Label('Search:'), alignmentX: CENTER_ALIGNMENT)
                            gui.searchBox = textField(new TextField('', new Dimension(200,30)))
                            vstrut(COMPONENT_SPACING)
                            label(new Label('Filter By Tag'), alignmentX: CENTER_ALIGNMENT)
                            gui.tagFilter = scrollPane(border: emptyBorder(2), minimumSize: [200, 50], preferredSize: [200, 50], maximumSize: [200, DEFAULT_GUI_HEIGHT],
                                    verticalScrollBar: scrollBar(
                                            border: lineBorder(color: Color.GRAY, thickness: 1),
                                            blockIncrement: 20,
                                            unitIncrement: 20,
                                    )
                            )
                            gui.tagFilter.setViewportView(gui.tagList = list(new ListBox(tagFilterModel)))
                            hbox(alignmentX: CENTER_ALIGNMENT, maximumSize: new Dimension(200, 50)) {
                                gui.clearFilterButton = button(new Button('Clear', new Dimension(90, 30)), actionPerformed: { clearFilters() })
                                glue()
                                gui.refreshFilterButton = button(new Button('Refresh', new Dimension(90, 30)), actionPerformed: { refreshFileList() })
                            }
                        }
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
//        gui.mainWindow.addComponentListener(new ComponentAdapter() {
//            @Override
//            public void componentResized(ComponentEvent e) {
//                Component c = (Component)e.getSource()
//                println("Size: " + c.getWidth() + " x " + c.getHeight())
//            }
//        })

        refreshFileList()
        //buildFilterPane()
        gui.mainWindow.setVisible(true)
        refreshFilterPane()
    }

    def setupFonts() {
        String fontFace = pdfmController.pdfConfig.getProperty('fontFace')
        int fontSize = pdfmController.pdfConfig.getProperty('fontSize').toInteger()
        textBoxFont = new Font(fontFace, Font.BOLD, fontSize)
        labelFont = new Font(fontFace, Font.PLAIN, fontSize)
        pdfTitleFont = new Font(fontFace, Font.BOLD, fontSize)
    }

    def refreshFilterPane() {
        tagFilterModel.clear()
        pdfmController.getTagList().each { tag ->
            tagFilterModel.addElement(tag)
        }
    }

    def clearFilters() {
        gui.tagList.clearSelection()
        refreshFileList()
    }

    def refreshFileList() {
        Map filter = [
                'tags': gui.tagList.getSelectedValuesList(),
        ]
        def pdfDomainObjects = pdfmController.getFilteredListOfPdfs(filter)
        swingBuilder.edt {
            def scrollablePdfListContents = vbox() {
                //def objCount = 0
                pdfDomainObjects.each { pdfDomainObj ->
                    //objCount++
                    def panelId = "panel" + pdfDomainObj.id
                    hbox(background: Color.BLUE, alignmentX: LEFT_ALIGNMENT, border: lineBorder(color: Color.LIGHT_GRAY, thickness: 1), maximumSize: [DEFAULT_GUI_WIDTH * 2, STANDARD_HBOX_HEIGHT * 2]) {
                        gui.selectedItemPanel = panel(id: panelId, background: Color.WHITE, alignmentX: LEFT_ALIGNMENT, border: emptyBorder(COMPONENT_SPACING), maximumSize: [DEFAULT_GUI_WIDTH * 2, STANDARD_HBOX_HEIGHT * 2]) {
                            boxLayout(axis: BoxLayout.Y_AXIS)
                            hbox(alignmentX: LEFT_ALIGNMENT, border: emptyBorder(COMPONENT_SPACING), maximumSize: [DEFAULT_GUI_WIDTH * 2, STANDARD_HBOX_HEIGHT * 2]) {
                                hstrut(COMPONENT_SPACING * 2)
                                vbox() {
                                    if (pdfDomainObj.descriptiveName == "") {
                                        label(new Label(pdfDomainObj.fileName, pdfTitleFont))
                                    } else {
                                        label(new Label(pdfDomainObj.descriptiveName, pdfTitleFont))
                                    }
                                    vstrut(COMPONENT_SPACING)
                                    def authPubYearLine = ""
                                    if (pdfDomainObj.publisher != "" && pdfDomainObj.publisher != null) {
                                        authPubYearLine += (pdfDomainObj.publisher + " - ")
                                    }
                                    if (pdfDomainObj.author != "" && pdfDomainObj.author != null) {
                                        authPubYearLine += (pdfDomainObj.author + " - ")
                                    }
                                    if (pdfDomainObj.year != "" && pdfDomainObj.year != null) {
                                        authPubYearLine += pdfDomainObj.year
                                    }

                                    label(new Label(authPubYearLine))
                                }
                                glue()
                                //vbox(minimumSize: new Dimension(200, 50), preferredSize: new Dimension(200, 50),maximumSize: new Dimension(200, 50), border: lineBorder(color: Color.RED, thickness: 1)) {
                                vbox(minimumSize: new Dimension(200, 50), preferredSize: new Dimension(200, 50),maximumSize: new Dimension(200, 50)) {
                                    if (pdfDomainObj.type == "Book") {
                                        label(new Label(pdfDomainObj.type + ": " + pdfDomainObj.isbn, new Font('DejaVu Sans', Font.PLAIN, 11)))
                                    } else {
                                        label(new Label(pdfDomainObj.type, new Font('DejaVu Sans', Font.PLAIN, 11)))
                                    }
                                    vstrut(3)
                                    label(new Label(pdfDomainObj.category, new Font('DejaVu Sans', Font.BOLD, 11)))
                                    vstrut(3)
                                    label(new Label(pdfDomainObj.tags, new Font('DejaVu Sans', Font.PLAIN, 11)))
                                }
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
                                //gui.sendButton.setEnabled(false)
                            } else {
                                if (selectedItem != "") {
                                    swingBuilder."$selectedItem".setBackground(Color.WHITE)
                                    selectedItem = ""
                                    gui.openButton.setEnabled(false)
                                    gui.editButton.setEnabled(false)
                                    //gui.sendButton.setEnabled(false)
                                }
                                swingBuilder."$panelId".setBackground(SELECTED_ITEM_COLOR)
                                selectedItem = panelId
                                gui.openButton.setEnabled(true)
                                gui.editButton.setEnabled(true)
                                //gui.sendButton.setEnabled(true)
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

        //refreshFilterPane()
    }

    def editPdfAttributes() {
        def selectedId = selectedItem.replace('panel', '').toInteger()
        PdfData pdf = pdfmController.getPdfObject(selectedId)
        swingBuilder.edt {
            gui.editDialog = frame(title: 'Edit', location: [250, 250], size: [800, 325], alwaysOnTop: true) {
                panel(border: emptyBorder(COMPONENT_SPACING)) {
                    boxLayout(axis: BoxLayout.Y_AXIS)
                    hbox(alignmentX: CENTER_ALIGNMENT, border: emptyBorder(COMPONENT_SPACING), maximumSize: [DEFAULT_GUI_WIDTH * 2, STANDARD_HBOX_HEIGHT]) {
                        label(new Label(pdf.fileName, pdfTitleFont))
                        glue()
                    }
                    hbox(alignmentX: CENTER_ALIGNMENT, border: emptyBorder(COMPONENT_SPACING), maximumSize: [DEFAULT_GUI_WIDTH * 2, STANDARD_HBOX_HEIGHT]) {
                        label(new Label('Display:  ', new Dimension(100, 30)), horizontalAlignment: JLabel.RIGHT)
                        gui.displayNameField = textField(new TextField(pdf.descriptiveName, new Dimension(DEFAULT_GUI_WIDTH * 2, 30)))
                    }
                    hbox(alignmentX: CENTER_ALIGNMENT, border: emptyBorder(COMPONENT_SPACING), maximumSize: [DEFAULT_GUI_WIDTH * 2, STANDARD_HBOX_HEIGHT]) {
                        label(new Label('Type:  ', new Dimension(100, 30)), horizontalAlignment: JLabel.RIGHT)
                        gui.typeField = comboBox(new ComboBox(TYPE_ITEMS), selectedItem: pdf.type)
                        glue()
                        label(new Label('ISBN:  ', new Dimension(100, 30)), horizontalAlignment: JLabel.RIGHT)
                        gui.isbnField = textField(new TextField(pdf.isbn))
                        glue()
                        label(new Label('Category:  ', new Dimension(100, 30)), horizontalAlignment: JLabel.RIGHT)
                        gui.categoryField = comboBox(new ComboBox(CATEGORY_ITEMS), selectedItem: pdf.category)
                    }
                    hbox(alignmentX: CENTER_ALIGNMENT, border: emptyBorder(COMPONENT_SPACING), maximumSize: [DEFAULT_GUI_WIDTH * 2, STANDARD_HBOX_HEIGHT]) {
                        label(new Label('Author:  ', new Dimension(100, 30)), horizontalAlignment: JLabel.RIGHT)
                        gui.authorField = textField(new TextField(pdf.author))
                        glue()
                        label(new Label('Publisher:  ', new Dimension(100, 30)), horizontalAlignment: JLabel.RIGHT)
                        gui.publisherField = textField(new TextField(pdf.publisher))
                        glue()
                        label(new Label('Year:  ', new Dimension(100, 30)), horizontalAlignment: JLabel.RIGHT)
                        gui.yearField = textField(new TextField(pdf.year, new Dimension(75, 30)))
                    }
                    hbox(alignmentX: CENTER_ALIGNMENT, border: emptyBorder(COMPONENT_SPACING), maximumSize: [DEFAULT_GUI_WIDTH * 2, STANDARD_HBOX_HEIGHT * 2]) {
                        label(new Label('Tags:  ', new Dimension(100, 90)), horizontalAlignment: JLabel.RIGHT)
                        gui.tagField = textArea(wrapStyleWord: true, lineWrap: true, editable: true, font: textBoxFont, text: pdf.tags, preferredSize: [DEFAULT_GUI_WIDTH, 90], maximumSize: [DEFAULT_GUI_WIDTH, 90], border: lineBorder(color: Color.GRAY, thickness: 1))
                    }
                    hbox(alignmentX: CENTER_ALIGNMENT, border: emptyBorder(COMPONENT_SPACING), maximumSize: [DEFAULT_GUI_WIDTH * 2, STANDARD_HBOX_HEIGHT]) {
                        gui.cancelButton = button(new Button('Cancel'), actionPerformed: { closeDialog(gui.editDialog) })
                        glue()
                        gui.saveButton = button(new Button('Save'), actionPerformed: {
                            Map attributesToSave = [
                                    'descriptiveName': gui.displayNameField.getText(),
                                    'type': gui.typeField.getSelectedItem(),
                                    'isbn': gui.isbnField.getText(),
                                    'category':gui.categoryField.getSelectedItem(),
                                    'author': gui.authorField.getText(),
                                    'publisher': gui.publisherField.getText(),
                                    'year': gui.yearField.getText(),
                                    'tags': gui.tagField.getText(),
                            ]
                            saveAttributeChanges(pdf, attributesToSave)
                        })
                    }
                }
            }

        }

        // Main Window is disabled when dialog is open, and re-enabled when dialog is closed through closeDialog
        // Closing via the System Menu (X) is supported here, so that we are not locked out of the main window.
        gui.editDialog.addWindowListener(new WindowAdapter() {
            @Override
            void windowClosing(WindowEvent e) {
                closeDialog(gui.editDialog)
            }
        })

        gui.mainWindow.setEnabled(false)
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
            gui.mainWindow.setEnabled(true)
        }
    }

    def saveAttributeChanges(pdf, attributes) {
        pdfmController.savePdfAttributeChanges(pdf, attributes)
        closeDialog(gui.editDialog)
        refreshFileList()
    }

    static void main(String[] args) {
        new PdfmGui()
    }
}
