package PDFManager.uiComponents

import PDFManager.utils.PdfConfig

import javax.swing.DefaultListModel
import javax.swing.JList
import javax.swing.border.LineBorder
import java.awt.Dimension
import java.awt.Font
import java.awt.Color
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

class ListBox extends JList implements PropertyChangeListener {

    static int STANDARD_WIDTH = 180
    static int STANDARD_HEIGHT = 200
    static Dimension LIST_BOX_SIZE = new Dimension(STANDARD_WIDTH, STANDARD_HEIGHT)

    static PdfConfig config
    static Properties configProperties
    static Font textBoxFont

    static {
        config = PdfConfig.getInstance()
        configProperties = config.getConfigProperties()
        textBoxFont = new Font(configProperties.getProperty('fontFace'), Font.PLAIN, configProperties.getProperty('fontSize').toInteger() - 1)
    }

    ListBox(items) {
        super(items)
        addPropertyChangeListener(this)
        setFont(textBoxFont)
        setMinimumSize(LIST_BOX_SIZE)
        setPreferredSize(LIST_BOX_SIZE)
        setMaximumSize(LIST_BOX_SIZE)
        setBorder(new LineBorder(Color.GRAY, 1))
        setAlignmentX(LEFT_ALIGNMENT)
    }

    ListBox(items, newSize) {
        super(items)
        addPropertyChangeListener(this)
        setFont(textBoxFont)
        setMinimumSize(newSize)
        setPreferredSize(newSize)
        setMaximumSize(newSize)
        setBorder(new LineBorder(Color.GRAY, 1))
        setAlignmentX(LEFT_ALIGNMENT)
    }


    void propertyChange(PropertyChangeEvent evt) {
        // no-op
    }
}
