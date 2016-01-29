package jhangmanclient.gui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import utility.GUIUtils;

public class WordPlayerDisplay extends JPanel {
    
    private int length;
    private char [] letters;
    private JLabel wordLabel;
    
    private final Object lock = new Object();

    public WordPlayerDisplay(int length) {
        super();
        this.length = length;
        initLayout();
        initComponents();
        setWordLength(length);
    }
    
    private void initComponents() {
        this.wordLabel = new JLabel(); 
        Font font = wordLabel.getFont();
        wordLabel.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
//        wordLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        this.add(wordLabel);
    }

    private void initLayout() {
        this.setLayout(new GridBagLayout());
        this.setBorder(BorderFactory.createLoweredBevelBorder());
        this.setBackground(Color.WHITE);
    }
    
    public void setFontSize(int size) {
        Font oldFont = wordLabel.getFont();
        wordLabel.setFont(new Font(oldFont.getName(),oldFont.getStyle(), size)); 
    }

    public void setWordLength(int length) {
        synchronized(lock) {
            this.length = length;
            letters = new char[length];
            Arrays.fill(letters, '_'); 
            updateLabel(); 
        }
    }
    
    private void updateLabel() {
        wordLabel.setText(null);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < letters.length; i++) {
            builder.append(letters[i]);
            if (i < letters.length - 1) {
                builder.append(" ");
            }
        }
        wordLabel.setText(builder.toString());
    }

    public void setWord(String word) {
        synchronized(lock) {
            setWordLength(word.length());
            for (int i = 0; i < word.length() && i < letters.length; i++) {
                letters[i] = word.charAt(i);
            }
            updateLabel(); 
        }
    } 
    
    public String getWord() {
        synchronized(lock) {
            return new String(letters);
        }
    }
    
    @Override
    public Dimension getMaximumSize() {
        Dimension defaultDimension = super.getMaximumSize();
        return new Dimension(defaultDimension.width,
                             wordLabel.getPreferredSize().height);
    }
    
    @Override
    public Dimension getMinimumSize() {
        Dimension d = super.getMinimumSize();
        return new Dimension(d.width, d.height + 100);
    }
    
    public boolean isUpdate(String eventWord) {
        if (eventWord.length() != letters.length){
            return true;
        } else {
            for (int i = 0; i < letters.length; i++) {
                if (letters[i] == '_' && eventWord.charAt(i) != '_') {
                    return true;
                }
            }
            return false;
        }
    }
    
    public void setWinner() {
        setBackground(new Color(0, 153, 76));
    }
    
    public void setLoser() {
        setBackground(new Color(227, 29, 29));
    }
    
    public static void p(Object o) {
        System.out.println(o == null? "null" : o.toString());
    }
    
    public static void main(String[] args) {
        
       GUIUtils.quietlySetLookAndFeel();
        
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            
            WordPlayerDisplay bugged = new WordPlayerDisplay(5);
            p(bugged.getFont());
            bugged.setWord("quasi");
            bugged.setFontSize(40);

            JPanel display = new JPanel();
            JLabel label = new JLabel("quasi");
            Font font = label.getFont();
            label.setFont(new Font(font.getName(), Font.BOLD, 40));
            display.add(label);

            JButton buttonW = new JButton("Winner");
            JButton buttonL = new JButton("Loser");
            JPanel buttonPanel = new JPanel();

            buttonPanel.add(buttonW);
            buttonPanel.add(buttonL);
            
            buttonW.addActionListener(l -> bugged.setWinner());
            buttonL.addActionListener(l -> bugged.setLoser());
            
            JPanel panel = new JPanel();
            
            panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
            
            panel.add(bugged);
            panel.add(display);
            panel.add(buttonPanel);
            
            frame.add(panel); 
            frame.pack(); 
            frame.setVisible(true);
        });
    }
}
