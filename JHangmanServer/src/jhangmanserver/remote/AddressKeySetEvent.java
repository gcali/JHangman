package jhangmanserver.remote;

import java.net.InetAddress;

import utility.observer.JHEvent;

public class AddressKeySetEvent implements JHEvent {

    private InetAddress address;
    private String key;

    public AddressKeySetEvent(InetAddress address, String key) {
        this.address = address;
        this.key = key;
    }

    public InetAddress getAddress() {
        return address;
    }

    public String getKey() {
        return key;
    }
}
