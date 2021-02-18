package PDFManager.uiComponents

import PDFManager.utils.PdfConfig

import javax.swing.JComboBox
import java.awt.Dimension
import java.awt.Font
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

class ComboBox extends JComboBox implements PropertyChangeListener {

    static int STANDARD_WIDTH = 200
    static int STANDARD_HEIGHT = 25
    static Dimension COMBO_BOX_SIZE = new Dimension(STANDARD_WIDTH, STANDARD_HEIGHT)

    static PdfConfig config
    static Properties configProperties
    static Font textBoxFont

    static {
        config = PdfConfig.getInstance()
        configProperties = config.getConfigProperties()
        textBoxFont = new Font(configProperties.getProperty('fontFace'), Font.PLAIN, configProperties.getProperty('fontSize').toInteger())
    }

    ComboBox(items) {
        super()
        items.each {
            this.addItem(it)
        }
        addPropertyChangeListener(this)
        setFont(textBoxFont)
        setMinimumSize(COMBO_BOX_SIZE)
    }

    void propertyChange(PropertyChangeEvent evt) {
        // no-op
    }
}
