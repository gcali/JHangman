package jhangmanclient.gui.components;

import java.awt.LayoutManager;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class ActionsPanel extends JPanel {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ActionsPanel(
            JComponent[] leftComponents, 
            JComponent[] rightComponents
    ) {
        super();
        initLayout();
        initComponents(leftComponents, rightComponents);
    }

    private void initComponents(JComponent[] leftComponents,
            JComponent[] rightComponents) {
        for (JComponent c : leftComponents) {
            this.add(c);
        }
        this.add(Box.createHorizontalGlue());
        for (JComponent c : rightComponents) {
            this.add(c);
        }
        
    }

    private void initLayout() {
        LayoutManager manager = new BoxLayout(this, BoxLayout.LINE_AXIS);
        this.setLayout(manager); 
    } 
}