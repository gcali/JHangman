package utility;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Classi di utilità per facilitare l'esecuzione di azioni
 * all'interno dell'Event Dispatch Thread, in modo da potere rispettare
 * i vincoli sull'utilizzo di Swing in caso di multithreading
 * @author gcali
 *
 */
public class GUIUtils {
    
    public static final Loggable log = new Loggable() {
        
        @Override
        public String getLoggableId() {
            return "GUIUtils";
        }
    };
    
    public static void invokeAndWait(Runnable run) {
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                run.run();
            } else {
                SwingUtilities.invokeAndWait(run); 
            }
        } catch (InterruptedException | InvocationTargetException e) {
            log.printDebugMessage(e.toString());
        }
    }
    
    public static void invokeLater(Runnable run) {
        if (SwingUtilities.isEventDispatchThread()) {
            run.run();
        } else {
            SwingUtilities.invokeLater(run);
        }
    }
    
    public static void quietlySetLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException | UnsupportedLookAndFeelException e1) {
            System.err.println("Couldn't set look and feel");
        }
    }

}