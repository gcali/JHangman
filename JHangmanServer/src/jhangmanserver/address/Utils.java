package jhangmanserver.address;

/**
 * Piccole funzioni utili per manipolare gli indirizzi
 * @author gcali
 *
 */
public class Utils {

    /**
     * Converte un array di byte in un intero
     * @param bytes l'array da convertire
     * @return l'intero corrispondente
     */
    public static int byteToInt(byte[] bytes) {
        int result = 0;
        for (byte b : bytes) {
            result <<= 8;
            result |= (b & 0xFF);
        }
        
        return result;
    }
    
    /**
     * Costruisce un array di byte a partire da un intero
     * @param i l'intero da convertire
     * @return l'array di byte costruito
     */
    public static byte[] intToBytes(int i) {
        return new byte[] {
            (byte)(i >>> 24),
            (byte)(i >>> 16),
            (byte)(i >>> 8),
            (byte)(i)
        }; 
    }
    
    /**
     * Trasforma la rappresentazione testuale di un indirizzo ip
     * in un array di byte
     * @param address una stringa contenente un indirizzo IP
     * @return l'array corrispondente
     */
    public static byte [] addressToBytes(String address) {

        int length = 4;
        
        String [] bytesString = address.split("\\.");
        
        if (bytesString.length != length) {
            throw new IllegalArgumentException("Argument malformed");
        }
        
        byte [] bytes = new byte[length];
        
        
        for (int i=0; i < length; i++) {
            bytes[i] = (byte) Integer.parseInt(bytesString[i]);
        }
        
        return bytes;
        
    }
    
    /**
     * Trasforma la rappresentazione testuale di un indirizzo ip
     * in un intero
     * @param address l'indirizzo da trasformare
     * @return l'intero corrispondente
     */
    public static int addressToInt(String address) {
        return byteToInt(addressToBytes(address));
    }

}
