package jhangmanclient.gui.frames;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;


public abstract class HangmanFrame extends JFrame {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private static final String defaultTitle = "JHangman";

    public HangmanFrame() {
        this(0);
    }
    
    public HangmanFrame(int borderSize) {
        super(defaultTitle);
        if (borderSize > 0) {
            JPanel panel = new JPanel();
            panel.setBorder(createEmptyBorder(borderSize));
            this.setContentPane(panel); 
        }
        initLayout();
        initComponents();
        this.pack();
        int minimumWidth, minimumHeight;
        minimumWidth = this.getWidth();
        minimumHeight = this.getHeight();
        this.setMinimumSize(new Dimension(minimumWidth, minimumHeight));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
    }
    
    protected abstract void initComponents();
    
    /**
     * Handle initializations regarding layout
     */

    protected void initLayout() {
        this.getContentPane().setLayout(
                new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS)
        ); 
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
                                      "Message", 
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

    public static Border createEmptyBorder(int borderSize) {
        return BorderFactory.createEmptyBorder(borderSize, 
                                               borderSize, 
                                               borderSize, 
                                               borderSize); 
    }
    
    protected String getHangmanTitle() {
        return defaultTitle;
    }
    
    public void updateTitle() {
        this.setTitle(this.getHangmanTitle());
    }
    

}