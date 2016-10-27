package jhangmanserver.config;

import java.io.File;

import jhangmanserver.address.AddressRange;
import rmi_interface.RMIServer;
import utility.ConfigData;

/**
 * Struttura che si occupa del parsing del file di configurazione del server
 * @author gcali
 *
 */
public class ServerConfigData extends ConfigData {
    
//    private int rmiPort = RMIServer.defaultPort;
//    private int tcpPort = tcp_interface.Defaults.getPort(); 
//    private AddressRange addressRange = AddressRange.fromRange("239.255.0.0/16");
//    private int maxGames = 10;
//    private boolean shouldEncrypt = true;
//    private String hostName = RMIServer.defaultHost;
//    private String name = RMIServer.name;

    private int rmiPort;
    private int tcpPort; 
    private AddressRange addressRange;
    private int maxGames;
    private boolean shouldEncrypt;
    private String hostName;
    private String name;
    
    @Override
    protected void setDefaultValues() {
        rmiPort = RMIServer.defaultPort;
        tcpPort = tcp_interface.Defaults.getPort(); 
        addressRange = AddressRange.fromRange("239.255.0.0/16");
        maxGames = 10;
        shouldEncrypt = true;
        hostName = RMIServer.defaultHost;
        name = RMIServer.name;
    }

    @Override
    protected void handleEntry(String key, Object value) {
        switch (key) {

        case "multicastAddressRange":
            if (value instanceof String) {
                addressRange = AddressRange.fromRange((String) value);
                printDebugMessage("Set " + key + " to " + value);
            } else {
                printError("Invalid value for " + key);
            }
            break; 
            
        case "multicastAddressMin":
            if (value instanceof String) {
                addressRange.setMinAddress((String) value);
                printDebugMessage("Set " + key + " to " + value);
            } else {
                printError("Invalid value for " + key);
            }
            break;

        case "multicastAddressMax":
            if (value instanceof String) {
                addressRange.setMaxAddress((String) value);
                printDebugMessage("Set " + key + " to " + value);
            } else {
                printError("Invalid value for " + key);
            }
            break;

        case "RMIport":
            if (value instanceof Number) {
                rmiPort = ((Number) value).intValue();
                printDebugMessage("Set " + key + " to " + value);
            } else {
                printError("Invalid value for " + key);
            }
            break;
            
        case "TCPport":
            if (value instanceof Number) {
                tcpPort = ((Number) value).intValue();
                printDebugMessage("Set " + key + " to " + value);
            } else {
                printError("Invalid value for " + key);
            }
            break;
            
        case "maxGames":
            if (value instanceof Number) {
                maxGames = ((Number)value).intValue();
                printDebugMessage("Set " + key + " to " + value);
            } else {
                printError("Invalid value for " + key);
            }
            break;
            
        case "shouldEncrypt":
            if (value instanceof Boolean) {
                shouldEncrypt = (boolean) value;
                printDebugMessage("Set " + key + " to " + value);
            } else {
                printError("Invalid value for " + key);
            }
            break;
            
        case "hostName":
            if (value instanceof String) {
                hostName = (String) value;
                printDebugMessage("Set " + key + " to " + value);
            } else {
                printError("Invalid value for " + key);
            }
            break;

        case "name":
            if (value instanceof String) {
                name = (String) value;
                printDebugMessage("Set " + key + " to " + value);
            } else {
                printError("Invalid value for " + key);
            }
            break;
            
        default:
            printDebugMessage(String.format(
                "Ignoring entry: (%s, %s)", key, value.toString()
            ));
                
        }
    }

    /**
     * Porta del server RMI
     * @return
     */
    public int getRMIPort() {
        return rmiPort;
    }
    
    public void setRMIPort(int port) {
        this.rmiPort = port;
    }
    
    /**
     * Porta del server TCP
     * @return
     */
    public int getTCPPort() {
        return tcpPort;
    }
    
    public void setTCPPort(int port) {
        this.tcpPort = port;
    }

    /**
     * Numero massimo di partite che possono essere in attesa di conferma
     * in contemporanea
     * @return
     */
    public int getMaxGames() {
        return maxGames;
    }
    
    public void setMaxGames(int maxGames) {
        this.maxGames = maxGames;
    }

    /**
     * Determina se procedere o meno alla cifratura dei dati sensibili
     * @return
     */
    public boolean getShouldEncrypt() {
        return shouldEncrypt;
    } 
    
    public void setShouldEncrypt(boolean shouldEncrypt) {
        this.shouldEncrypt = shouldEncrypt;
    }
    
    /**
     * Restituisce il nome del server RMI
     * @return
     */
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Restituisce l'hostname del server RMI
     * @return
     */
    public String getHostName() {
        return hostName;
    }
    
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * Determina il range valido di indirizzi multicast
     * @return
     */
    public AddressRange getAddressRange() {
        return addressRange;
    }

    @Override
    public String getLoggableId() {
        return "ServerConfigData";
    }

    @Override
    protected String getPropertyName() {
        return "server_config_file";
    }

    @Override
    protected String getDefaultConfigPath() {
        return System.getProperty("user.home") + File.separator + 
        ".jhangman_server.json";
    }

}