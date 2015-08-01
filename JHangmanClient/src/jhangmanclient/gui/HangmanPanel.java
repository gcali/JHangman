package jhangmanclient.gui;

import java.awt.LayoutManager;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class HangmanPanel extends JPanel {

    public HangmanPanel() {
        super();
    }

    public HangmanPanel(LayoutManager layout) {
        super(layout);
    }

    public HangmanPanel(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
    }

    public HangmanPanel(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
    }

    protected void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, 
                                      message, 
                                      "Error", 
                                      JOptionPane.ERROR_MESSAGE); 
    }

    protected void showMessageDialog(String message) {
        JOptionPane.showMessageDialog(this, 
                                      message, 
                                      "Error", 
                                      JOptionPane.INFORMATION_MESSAGE); 
    }

    protected int showQuestionDialog(String message, String yes, String no) {
        Object [] options = {yes, no};
        return JOptionPane.showOptionDialog(
                this, 
                message, 
                "", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.QUESTION_MESSAGE, 
                null, 
                options, 
                options[0]);
    }

}