package jhangmanserver.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

import jhangmanserver.address.AddressRange;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import rmi_interface.RMIServer;
import utility.Loggable;

public class ConfigData {
    
    private int rmiPort = RMIServer.defaultPort;
    private int tcpPort = tcp_interface.Defaults.getPort(); 
    private AddressRange addressRange = AddressRange.fromRange("239.255.0.0/16");
    private int maxGames = 10;
    private boolean shouldEncrypt = true;
    private String hostName = RMIServer.defaultHost;
    private String name = RMIServer.name;

    private final Loggable logger = new Loggable() { 
        @Override
        public String getLoggableId() {
            return "ConfigData";
        }
    };
    private final static String defaultConfigPath = 
        System.getProperty("user.home") + File.separator + 
        ".jhangman_server.json";

    public ConfigData(String ifAbsentPath) {
        initData(System.getProperty("server_config_file", ifAbsentPath)); 
    }
    
    public ConfigData() {
        this(defaultConfigPath);
    }

    private void initData(String fileName) {
        try {
            parseConfigString(readFile(fileName));
        } catch (IOException e) {
            logger.printDebugMessage(
                "Couldn't read config file, falling back on defaults"
            );
        }
    }
    
    private void parseConfigString(String content) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject parsedObject = (JSONObject) parser.parse(content);
            parseJSONObject(parsedObject);
        } catch (ParseException e) {
            logger.printError(
                "Couldn't parse config file, falling back on defaults"
            );
        }
    }
    
    @SuppressWarnings("unchecked")
    private void parseJSONObject(JSONObject parsedObject) {
        for (Map.Entry<String,Object> entry : 
                (Set<Map.Entry<String, Object>>)parsedObject.entrySet()) {
            handleEntry(entry.getKey(), entry.getValue());
        }
    }

    private void handleEntry(String key, Object value) {
        switch (key) {

        case "multicastAddressRange":
            if (value instanceof String) {
                addressRange = AddressRange.fromRange((String) value);
                logger.printDebugMessage("Set " + key + " to " + value);
            } else {
                logger.printError("Invalid value for " + key);
            }
            break; 
            
        case "multicastAddressMin":
            if (value instanceof String) {
                addressRange.setMinAddress((String) value);
                logger.printDebugMessage("Set " + key + " to " + value);
            } else {
                logger.printError("Invalid value for " + key);
            }

        case "multicastAddressMax":
            if (value instanceof String) {
                addressRange.setMaxAddress((String) value);
                logger.printDebugMessage("Set " + key + " to " + value);
            } else {
                logger.printError("Invalid value for " + key);
            }

        case "RMIport":
            if (value instanceof Number) {
                rmiPort = ((Number) value).intValue();
                logger.printDebugMessage("Set " + key + " to " + value);
            } else {
                logger.printError("Invalid value for " + key);
            }
            break;
            
        case "TCPport":
            if (value instanceof Number) {
                tcpPort = ((Number) value).intValue();
                logger.printDebugMessage("Set " + key + " to " + value);
            } else {
                logger.printError("Invalid value for " + key);
            }
            break;
            
        case "maxGames":
            if (value instanceof Number) {
                maxGames = ((Number)value).intValue();
                logger.printDebugMessage("Set " + key + " to " + value);
            } else {
                logger.printError("Invalid value for " + key);
            }
            break;
            
        case "shouldEncrypt":
            if (value instanceof Boolean) {
                shouldEncrypt = (boolean) value;
                logger.printDebugMessage("Set " + key + " to " + value);
            } else {
                logger.printError("Invalid value for " + key);
            }
            break;
            
        case "hostName":
            if (value instanceof String) {
                hostName = (String) value;
                logger.printDebugMessage("Set " + key + " to " + value);
            } else {
                logger.printError("Invalid value for " + key);
            }

        case "name":
            if (value instanceof String) {
                name = (String) value;
                logger.printDebugMessage("Set " + key + " to " + value);
            } else {
                logger.printError("Invalid value for " + key);
            }
            
        default:
            logger.printDebugMessage(String.format(
                "Ignoring entry: (%s, %s)", key, value.toString()
            ));
                
        }
    }

    private static String readFile(String fileName) throws IOException {
        byte[] fileBytes = Files.readAllBytes(Paths.get(fileName));
        return new String(fileBytes);
    }
    
    public static void main(String[] args) throws IOException {
        
    }

    public int getRMIPort() {
        return rmiPort;
    }
    
    public void setRMIPort(int port) {
        this.rmiPort = port;
    }
    
    public int getTCPPort() {
        return tcpPort;
    }
    
    public void setTCPPort(int port) {
        this.tcpPort = port;
    }

    public int getMaxGames() {
        return maxGames;
    }
    
    public void setMaxGames(int maxGames) {
        this.maxGames = maxGames;
    }

    public boolean getShouldEncrypt() {
        return shouldEncrypt;
    } 
    
    public void setShouldEncrypt(boolean shouldEncrypt) {
        this.shouldEncrypt = shouldEncrypt;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getHostName() {
        return hostName;
    }
    
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public AddressRange getAddressRange() {
        return addressRange;
    }
    
}