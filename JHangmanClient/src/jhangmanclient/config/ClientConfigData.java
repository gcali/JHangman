package jhangmanclient.config;

import java.io.File;

import rmi_interface.RMIServer;
import utility.ConfigData;

public class ClientConfigData extends ConfigData {

//    private String rmiAddress = RMIServer.defaultHost;
//    private Integer rmiPort = RMIServer.defaultPort;
//    private String tcpAddress = tcp_interface.Defaults.getAddress();
//    private Integer tcpPort = tcp_interface.Defaults.getPort();
//    private String rmiName = RMIServer.name;
//    private Long gameTimeout = 10000000;
//    private Integer lives = 4;
    private String rmiAddress;
    private Integer rmiPort;
    private String tcpAddress;
    private Integer tcpPort;
    private String rmiName;
    private Long gameTimeout;
    private Integer lives;

    public ClientConfigData(String ifAbsentPath) {
        super(ifAbsentPath);
    }

    public ClientConfigData() {
        super();
    }
    
    @Override
    protected void setDefaultValues() {
        rmiAddress = RMIServer.defaultHost; 
        rmiPort = RMIServer.defaultPort; 
        tcpAddress = tcp_interface.Defaults.getAddress(); 
        tcpPort = tcp_interface.Defaults.getPort(); 
        rmiName = RMIServer.name;
        gameTimeout = 10000000L;
        lives = 4; 
    }

    @Override
    public String getLoggableId() {
        return "ClientConfigData";
    }

    @Override
    protected String getPropertyName() {
        return "client_config_file";
    }

    @Override
    protected String getDefaultConfigPath() {
        String path = System.getProperty("user.home") + File.separator + 
        ".jhangman_client.json";
        return path;
    }

    @Override
    protected void handleEntry(String key, Object value) {
        switch (key) {
        
        case "lives":
            if (value instanceof Number) {
                lives = ((Number) value).intValue();
                printDebugMessage("Set " + key + " to " + value);
            } else {
                printError("Invalid value for " + key);
            }
            break;

        case "rmiAddress":
            if (value instanceof String) {
                rmiAddress = (String) value;
                printDebugMessage("Set " + key + " to " + value);
            } else {
                printError("Invalid value for " + key);
            }
            break;
            
        case "rmiPort":
            if (value instanceof Number) {
                rmiPort = ((Number) value).intValue();
                printDebugMessage("Set " + key + " to " + value);
            } else {
                printError("Invalid value for " + key);
            }
            break;
            
        case "rmiName":
            if (value instanceof String) {
                rmiName = (String) value;
                printDebugMessage("Set " + key + " to " + value);
            } else {
                printError("Invalid value for " + key);
            }
            break;
            
        case "tcpAddress":
            if (value instanceof String) {
                tcpAddress = (String) value;
                printDebugMessage("Set " + key + " to " + value);
            } else {
                printError("Invalid value for " + key);
            }
            break;

        case "tcpPort":
            if (value instanceof Number) {
                tcpPort = ((Number) value).intValue();
                printDebugMessage("Set " + key + " to " + value);
            } else {
                printError("Invalid value for " + key);
            }
            break;
            
        case "gameTimeout":
            if (value instanceof Number) {
                gameTimeout = ((Number) value).longValue();
                printDebugMessage("Set " + key + " to " + value);
            } else {
                printError("Invalid value for " + key);
            }
            break;
            
        default:
            printDebugMessage("Ignoring entry (" + key + ", " + value + ")");
        }
    }

    public String getRmiAddress() {
        return rmiAddress;
    }

    public int getRmiPort() {
        return rmiPort;
    }

    public String getTcpAddress() {
        return tcpAddress;
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public void setRmiAddress(String rmiAddress) {
        this.rmiAddress = rmiAddress;
    }

    public void setRmiPort(int rmiPort) {
        this.rmiPort = rmiPort;
    }

    public void setTcpAddress(String tcpAddress) {
        this.tcpAddress = tcpAddress;
    }

    public void setTcpPort(int tcpPort) {
        this.tcpPort = tcpPort;
    }

    public String getRmiName() {
        return rmiName;
    }

    public void setRmiName(String rmiName) {
        this.rmiName = rmiName;
    }

    public long getGameTimeout() {
        return gameTimeout;
    }

    public void setGameTimeout(long gameTimeout) {
        this.gameTimeout = gameTimeout;
    }

    public int getLives() {
        return lives;
    } 
}
