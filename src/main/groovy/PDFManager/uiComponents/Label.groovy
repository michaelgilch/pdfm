package PDFManager.uiComponents

import PDFManager.utils.PdfConfig

import javax.swing.JLabel
import java.awt.Dimension
import java.awt.Font
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

class Label extends JLabel implements PropertyChangeListener {

    static int STANDARD_WIDTH = 200
    static int STANDARD_HEIGHT = 30
    static Dimension LABEL_SIZE = new Dimension(STANDARD_WIDTH, STANDARD_HEIGHT)

    static PdfConfig config
    static Properties configProperties
    static Font labelFont

    static {
        config = new PdfConfig().getInstance()
        configProperties = config.getConfigProperties()
        labelFont = new Font(configProperties.getProperty('fontFace'), Font.PLAIN, configProperties.getProperty('fontSize').toInteger())
    }

    Label(String text) {
        super(text)
        addPropertyChangeListener(this)
        setMinimumSize(LABEL_SIZE)
        setFont(labelFont)
        //setPreferredSize(LABEL_SIZE)
        //setMaximumSize(LABEL_SIZE)
    }

    void propertyChange(PropertyChangeEvent evt) {
        // no-op
    }
}
