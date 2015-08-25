package jhangmanclient.gui.utility;

import java.awt.Container;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.border.Border;

public class FrameAdapter {
    
    public static Border createEmptyBorder(int borderSize) {
        return BorderFactory.createEmptyBorder(borderSize, 
                                               borderSize, 
                                               borderSize, 
                                               borderSize); 
    }
    
    public static JFrame createFrameFromContainer(Container content) {
        JFrame frame = new JFrame("JHangman");
        frame.setContentPane(content);
        frame.pack();
        int minimumWidth, minimumHeight;
        minimumWidth = frame.getWidth();
        minimumHeight = frame.getHeight();
        frame.setMinimumSize(new Dimension(minimumWidth, minimumHeight));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        return frame;
    } 
}