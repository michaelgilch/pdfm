package PDFManager.uiComponents

import javax.swing.JButton
import java.awt.Dimension
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

class Button extends JButton implements PropertyChangeListener {

    static int STANDARD_WIDTH = 100
    static int STANDARD_HEIGHT = 30
    static Dimension BUTTON_SIZE = new Dimension(STANDARD_WIDTH, STANDARD_HEIGHT)

    Button() { this("")}

    Button(String text) {
        super(text)
        addPropertyChangeListener(this)
        setMinimumSize(BUTTON_SIZE)
        setPreferredSize(BUTTON_SIZE)
        setMaximumSize(BUTTON_SIZE)
    }

    void propertyChange(PropertyChangeEvent evt) {

    }
}
