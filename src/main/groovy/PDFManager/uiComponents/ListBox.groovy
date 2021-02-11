package PDFManager.uiComponents

import PDFManager.utils.PdfConfig

import javax.swing.DefaultListModel
import javax.swing.JList
import java.awt.Dimension
import java.awt.Font
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

class ListBox extends JList implements PropertyChangeListener {

    static int STANDARD_WIDTH = 200
    static int STANDARD_HEIGHT = 200
    static Dimension LIST_BOX_SIZE = new Dimension(STANDARD_WIDTH, STANDARD_HEIGHT)

    static PdfConfig config
    static Properties configProperties
    static Font textBoxFont

    static {
        config = PdfConfig.getInstance()
        configProperties = config.getConfigProperties()
        textBoxFont = new Font(configProperties.getProperty('fontFace'), Font.BOLD, configProperties.getProperty('fontSize').toInteger())
    }

    ListBox(items) {
        //def listItems = new DefaultListModel()
        super(items)

//        items.each { item ->
//            listItems.addElement(item)
//        }
//
        //this.add('AGAIN')
        println items.toString()
//        items.each { item ->
//            this.add(item)
//        }
        addPropertyChangeListener(this)
        setFont(textBoxFont)
        setMinimumSize(LIST_BOX_SIZE)
    }

    void propertyChange(PropertyChangeEvent evt) {
        // no-op
    }
}
