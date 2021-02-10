package PDFManager.uiComponents

import javax.swing.JLabel
import java.awt.Dimension
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

class Label extends JLabel implements PropertyChangeListener {

    static int STANDARD_WIDTH = 200
    static int STANDARD_HEIGHT = 30
    static Dimension LABEL_SIZE = new Dimension(STANDARD_WIDTH, STANDARD_HEIGHT)

    //Button() { this("")}

    Label(String text) {
        super(text)
        addPropertyChangeListener(this)
        setMinimumSize(LABEL_SIZE)
        //setPreferredSize(LABEL_SIZE)
        //setMaximumSize(LABEL_SIZE)
    }

    void propertyChange(PropertyChangeEvent evt) {

    }
}
