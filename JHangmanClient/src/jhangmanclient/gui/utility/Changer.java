package jhangmanclient.gui.utility;

import utility.observer.JHObservable;

@Deprecated
public interface Changer extends JHObservable {

    public void changeFrame(String id);

}