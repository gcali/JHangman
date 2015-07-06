package jhangmanclient.gui;

import java.awt.LayoutManager;

import javax.swing.JButton;
import javax.swing.JPanel;

public class RegistrationPanel extends JPanel {
    
    Changer changer = null;

    public RegistrationPanel() {
        super();
        JButton button = new JButton("Cliccami anche te!");
        this.add(button);
        button.addActionListener(e -> this.changer.changePanel());
    }
    
    public void setChanger(Changer changer) {
        this.changer = changer;
    }

}