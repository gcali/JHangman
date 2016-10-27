package tcp_interface;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Defaults {
    
    public static String getAddress() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }
    
    public static int getPort() {
        return 10005;
    }

}
