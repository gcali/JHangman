package jhangmanclient.gui.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ConfirmGameDialog<T> extends JDialog {
    
    private final Callable<T> confirm;
    private final Consumer<T> showScreen;
    private final JFrame parent;
    
    public ConfirmGameDialog(
        JFrame frame, 
        Callable<T> confirm,
        Runnable abort,
        Consumer<T> showScreen
    ) {
        super(frame, "Waiting");
        
        this.parent = frame;
        this.confirm = confirm;
        this.showScreen = showScreen;
        
        setModal(false);
        
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Waiting for confirmation");
        JButton button = new JButton("Abort");
        
        button.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                abort.run();
                ConfirmGameDialog.this.dispose();
            }
        });
        
        panel.add(label);
        panel.add(button);
        
        setContentPane(panel);
    }
    
    public void start() {
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                T data;
                try {
                    data = confirm.call();
                    if (data != null) {
                        showScreen.accept(data);
                    } 
                } catch (Exception e) { 
                } finally {
                    ConfirmGameDialog.this.dispose();
                }
            }
        });
        thread.start();
    } 
}