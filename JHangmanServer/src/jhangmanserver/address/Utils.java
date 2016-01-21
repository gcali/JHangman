package jhangmanserver.address;

public class Utils {

    public static int byteToInt(byte[] bytes) {
        int result = 0;
        for (byte b : bytes) {
            result <<= 8;
            result |= (b & 0xFF);
        }
        
        return result;
    }
    
    public static byte[] intToBytes(int i) {
        return new byte[] {
            (byte)(i >>> 24),
            (byte)(i >>> 16),
            (byte)(i >>> 8),
            (byte)(i)
        }; 
    }
    
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
    
    public static int addressToInt(String address) {
        return byteToInt(addressToBytes(address));
    }

}
