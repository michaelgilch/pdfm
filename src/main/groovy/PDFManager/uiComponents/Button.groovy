package PDFManager.uiComponents

import PDFManager.utils.PdfConfig

import javax.swing.JButton
import java.awt.Dimension
import java.awt.Font
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

class Button extends JButton implements PropertyChangeListener {

    static int STANDARD_WIDTH = 75
    static int STANDARD_HEIGHT = 30
    static Dimension BUTTON_SIZE = new Dimension(STANDARD_WIDTH, STANDARD_HEIGHT)
    static PdfConfig config
    static Properties configProperties
    static Font buttonFont

    static {
        config = PdfConfig.getInstance()
        configProperties = config.getConfigProperties()
        buttonFont = new Font(configProperties.getProperty('fontFace'), Font.PLAIN, configProperties.getProperty('fontSize').toInteger()-2)
    }

    Button(String text) {
        super(text)
        addPropertyChangeListener(this)
        setFont(buttonFont)
        setMinimumSize(BUTTON_SIZE)
    }

    void propertyChange(PropertyChangeEvent evt) {

    }
}
