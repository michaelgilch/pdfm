package PDFManager.uiComponents

import PDFManager.utils.PdfConfig

import javax.swing.JTextField
import java.awt.Dimension
import java.awt.Font
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

class TextField extends JTextField implements PropertyChangeListener {

    static int STANDARD_WIDTH = 100
    static int STANDARD_HEIGHT = 25
    static Dimension TEXT_FIELD_SIZE = new Dimension(STANDARD_WIDTH, STANDARD_HEIGHT)
    static PdfConfig config
    static Properties configProperties
    static Font textBoxFont

    static {
        config = PdfConfig.getInstance()
        configProperties = config.getConfigProperties()
        textBoxFont = new Font(configProperties.getProperty('fontFace'), Font.BOLD, configProperties.getProperty('fontSize').toInteger()-2)
    }

    TextField(String text) {
        super(text)
        addPropertyChangeListener(this)
        setFont(textBoxFont)
        setMinimumSize(TEXT_FIELD_SIZE)
    }

    TextField(String text, java.awt.Dimension newSize) {
        super(text)
        addPropertyChangeListener(this)
        setFont(textBoxFont)
        setMinimumSize(newSize)
        setPreferredSize(newSize)
        setMaximumSize(newSize)
    }

    void propertyChange(PropertyChangeEvent evt) {

    }
}