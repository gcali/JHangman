package jhangmanclient.gui.components;

import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

public class LivesIndicatorPanel extends JPanel {
    
    private int lives;
    private LivesIndicator livesIndicator;
    private Dimension dimension;

    public LivesIndicatorPanel(int lives) {
        this(lives, new Dimension(100,100));
    }
    
    public LivesIndicatorPanel(int lives, Dimension dimension) {
        super();
        this.lives = lives; 
        this.dimension = dimension;
        initLayout();
        initComponents(); 
    }

    private void initComponents() {
        this.livesIndicator = new LivesIndicator(dimension, lives);
        this.add(livesIndicator);
    }

    private void initLayout() {
        this.setLayout(new GridBagLayout());
        Border border = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
        this.setBorder(border);
    } 
    
    @Override
    public Dimension getMinimumSize() {
        return new Dimension(dimension.width + 4, dimension.height + 4);
    }
    
    public void setIndicatorSize(Dimension dimension) {
        this.dimension = dimension;
        this.livesIndicator.setIndicatorSize(dimension);
    }
    
    public Dimension getIndicatorSize() {
        return this.dimension;
    }

    public Dimension getMaximumSize() {
        Dimension defaultDimension = super.getMaximumSize();
        return new Dimension(defaultDimension.width,
                             getPreferredSize().height);
    }
    
    public void setLives(int lives) {
        this.livesIndicator.setLives(lives);
    }

}
