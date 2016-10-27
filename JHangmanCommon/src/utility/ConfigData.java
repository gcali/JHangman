package utility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Classe di utilità per aiutare nel parsing di file di configurazione
 * scritti in JSON; una volta implementati i metodi astratti richiesti,
 * il parsing viene eseguito in automatico.
 * @author gcali
 *
 */
public abstract class ConfigData implements Loggable {
    
    public ConfigData(String ifAbsentPath) {
        initData(System.getProperty(getPropertyName(), ifAbsentPath)); 
    }
    
    protected abstract String getPropertyName();
    
    protected abstract String getDefaultConfigPath();
    
    public ConfigData() {
        setDefaultValues();
        initData(System.getProperty(
            getPropertyName(), 
            getDefaultConfigPath()
        ));
    }

    protected abstract void setDefaultValues();

    private void initData(String fileName) {
        try {
            parseConfigString(readFile(fileName));
        } catch (IOException e) {
            printDebugMessage(
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
            printError(
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

    protected abstract void handleEntry(String key, Object value);

    private static String readFile(String fileName) throws IOException {
        byte[] fileBytes = Files.readAllBytes(Paths.get(fileName));
        return new String(fileBytes);
    }

    
}
