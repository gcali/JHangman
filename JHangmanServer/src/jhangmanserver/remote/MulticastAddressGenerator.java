package jhangmanserver.remote;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MulticastAddressGenerator {
    
    private final static Random randomGenerator = new Random(); 
    private final static Set<LastBytes> set = 
            Collections.newSetFromMap(new ConcurrentHashMap<LastBytes,Boolean>());
    private final static byte[] baseBytes = new byte[]{(byte)239,(byte)255};
    
    /**
     * Generates a random address not in use in the range 239.255.0.0/16 
     * conforming to RFC2365 for multicast local use; an address is considered
     * in use if it's been returned by {@link getAddress()} and not freed
     * by {@link freeAddress(InetAddress)}
     * 
     * @return an InetAddress in the range 239.255.0.0/16
     */
    public static InetAddress getAddress() {
        byte[] bytes = new byte[2];
        
        boolean found = false;
        String address;
        LastBytes lastBytes = null;
        while (!found) {
            randomGenerator.nextBytes(bytes); 
            lastBytes = new LastBytes(bytes);
            found = set.add(new LastBytes(bytes));
        }
        
        return lastBytes.formAddress(baseBytes);
    }
    
    public static void freeAddress(InetAddress address) {
        byte[] addressBytes = address.getAddress();
        set.remove(new LastBytes(addressBytes[2], addressBytes[3]));
    }
    
    
    private static class LastBytes {
        private byte[] bytes;

        public LastBytes(byte[] bytes) {
            this(bytes[0], bytes[1]);
        }
        
        public LastBytes(byte a, byte b) {
            this.bytes = new byte[]{a,b};
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof LastBytes) {
                if (Arrays.equals(this.bytes, ((LastBytes)obj).bytes)) {
                    return true;
                }
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            return Arrays.hashCode(this.bytes);
        }
        
        public String toString() {
            return new Integer(this.hashCode()).toString();
        } 
        
        public InetAddress formAddress(byte[] prefix) {
            byte[] total = new byte[4];
            System.arraycopy(prefix, 0, total, 0, 2);
            System.arraycopy(this.bytes, 0, total, 2, 2);
            try {
                return InetAddress.getByAddress(total);
            } catch (UnknownHostException e) {
                assert false;
                throw new RuntimeException(e);
            }
        }
    } 
    
    public static void main(String[] args) {
        
        int dim=10;
        InetAddress[] array = new InetAddress[10];
        
        for (int i=0; i < dim; i++) {
            array[i] = getAddress();
            System.out.println(array[i]);
        }
        System.out.println("----------");
        for (LastBytes b : set) {
            System.out.println(b.formAddress(baseBytes));
        }
        for (InetAddress a : array) {
            freeAddress(a);
        }

        System.out.println("----------"); 
        for (LastBytes b : set) {
            System.out.println(b.formAddress(baseBytes));
        } 
    }
}