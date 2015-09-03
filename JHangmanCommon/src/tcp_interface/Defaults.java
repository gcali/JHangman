package tcp_interface;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Defaults {
    
    public static InetAddress getAddress() throws UnknownHostException {
        return InetAddress.getLocalHost();
    }
    
    public static int getPort() {
        return 10005;
    }

}
