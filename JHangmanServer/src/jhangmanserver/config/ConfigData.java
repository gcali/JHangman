package jhangmanserver.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import rmi_interface.RMIServer;
import utility.Loggable;

public class ConfigData {
    
    private static String multicastAddressRange = "239.255.0.0/16";
    private static int port = RMIServer.defaultPort;
    private static int maxGames = 10;
    private static boolean shouldEncrypt = true;

    private static final Loggable logger = new Loggable() { 
        @Override
        public String getId() {
            return "ConfigData";
        }
    }; 
    private static final String defaultConfigPath = 
        System.getProperty("user.home") + File.separator + 
        ".jhangman_server.json";

    //static initializer
    static {
        initData(System.getProperty("server_config_file", defaultConfigPath));
    }
    
    private static void initData(String fileName) {
        try {
            parseConfigString(readFile(fileName));
        } catch (IOException e) {
            logger.printDebugMessage(
                "Couldn't read config file, falling back on defaults"
            );
        }
    }
    
    private static void parseConfigString(String content) {
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
    private static void parseJSONObject(JSONObject parsedObject) {
        for (Map.Entry<String,Object> entry : 
                (Set<Map.Entry<String, Object>>)parsedObject.entrySet()) {
            handleEntry(entry.getKey(), entry.getValue());
        }
    }

    private static void handleEntry(String key, Object value) {
        switch (key) {
        case "multicastAddressRange":
            if (value instanceof String) {
                multicastAddressRange = (String) value;
                logger.printDebugMessage("Set " + key + " to " + value);
            } else {
                logger.printError("Invalid value for " + key);
            }
            break; 
        case "port":
            if (value instanceof Number) {
                port = (int) port;
                logger.printDebugMessage("Set " + key + " to " + value);
            } else {
                logger.printError("Invalid value for " + key);
            }
            break;
            
        case "maxGames":
            if (value instanceof Number) {
                maxGames = (int) maxGames;
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

    public static String getMulticastAddressRange() {
        return multicastAddressRange;
    }

    public static int getPort() {
        return port;
    }

    public static int getMaxGames() {
        return maxGames;
    }

    public static boolean shouldEncrypt() {
        return shouldEncrypt;
    } 

}