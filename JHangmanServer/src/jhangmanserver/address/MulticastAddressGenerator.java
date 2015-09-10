package jhangmanserver.address;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MulticastAddressGenerator {
    
    private final static Random randomGenerator = new Random(); 
    private final static Set<List<Byte>> set = 
            Collections.newSetFromMap(new ConcurrentHashMap<List<Byte>,Boolean>());
    
    /**
     * Generates a random address not in use in the range 239.255.0.0/16 
     * conforming to RFC2365 for multicast local use; an address is considered
     * in use if it's been returned by {@link getAddress()} and not freed
     * by {@link freeAddress(InetAddress)}
     * 
     * @return an InetAddress in the range 239.255.0.0/16
     */
    private static byte[] catByteArray(byte[] a, int lengthA, byte[] b) {
        byte[] total = new byte[lengthA + b.length];
        System.arraycopy(a, 0, total, 0, lengthA);
        System.arraycopy(b, 0, total, lengthA, b.length);
        return total;
    }
    
    public static InetAddress getAddress(String address) {
        if (address == null) {
            throw new IllegalArgumentException("Argument was null");
        }
        String [] addressFixed = address.split("/");
        if (addressFixed.length != 2) {
            throw new IllegalArgumentException("Argument malformed: " + address);
        }
        String [] stringAddressBytes = addressFixed[0].split("\\.");
        if (stringAddressBytes.length != 4) {
            throw new IllegalArgumentException("Argument malformed: "+ address);
        }
        byte[] byteAddress = new byte[stringAddressBytes.length];
        for (int i=0; i < stringAddressBytes.length; i++) {
            byteAddress[i] = (byte) (Integer.parseInt(stringAddressBytes[i]));
        }
        int fixed = Integer.parseInt(addressFixed[1]);
        return getAddress(byteAddress, fixed);
    }

    public static InetAddress getAddress(byte[] prefix, int fixed) {
        if (fixed > 32 || fixed < 0) {
            throw new IllegalArgumentException("Can't have " + fixed + " fixed bits");
        }
        if (prefix.length * 8 < fixed) {
            throw new IllegalArgumentException("Prefix isn't long enough");
        }
        
        System.out.println("[Generator] " + toStringAsAddress(prefix));
        System.out.println("[Generator] " + fixed);
        
        int total = 32;
        int needed = total - fixed;
        int dimSuffix = needed/8;
        int dimPrefix = total/8 - dimSuffix;
        
        System.out.println(String.format("[Generator] %d %d %d %d", total, needed, dimSuffix, dimPrefix));

        byte[] bytes = new byte[dimSuffix];
        
        boolean found = false;
        byte[] address = null;
        while (!found) {
            randomGenerator.nextBytes(bytes); 
            address = catByteArray(prefix, dimPrefix, bytes);
            if (fixed % 8 != 0) {
                byte [] singleByte = new byte[1];
                randomGenerator.nextBytes(singleByte);
//                byte mask = (byte) 0xFF;
//                mask = (byte)((mask & 0xFF) >>> (fixed % 8));
//                System.out.println("S: " + (fixed % 8));
//                System.out.println("M: " + Integer.toBinaryString((mask & 0xFF)));
//                System.out.println("B: " + (singleByte[0] & 0xFF));
                address[fixed/8] = 
                    (byte)((singleByte[0] & (0xFF >>> (fixed % 8))) | address[fixed/8]);
            }
//            System.out.println("Orig -> " + toStringAsAddress(address));
            found = set.add(arrayToList(address));
        }
        
        try {
            System.out.println("[Generator] Generated: " + toStringAsAddress(address));
            return InetAddress.getByAddress(address);
        } catch (UnknownHostException e) {
            assert false;
            return null;
        }
    }
    
    private static List<Byte> arrayToList(byte[] array) {
        List<Byte> l = new ArrayList<Byte>();
        for (byte b : array) {
            l.add(b);
        }
        return l;
    }
    
    public static void freeAddress(InetAddress address) {
        System.out.println(toStringAsAddress(address.getAddress()));
        set.remove(arrayToList(address.getAddress()));
    }
    
    private static String toStringAsAddress(byte[] array) {
        if (array.length < 1) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        int i = 0;
        while (i < array.length-1) {
            builder.append(String.format("%d", array[i] & 0xFF));
            builder.append(".");
            i++; 
        }
        builder.append(String.format("%d", array[i] & 0xFF));
        return builder.toString();
    }
    
    
    public static void main(String[] args) {
        
        int dim=20;
        InetAddress[] array = new InetAddress[dim];
        
        for (int i=0; i < dim; i++) {
            byte[] addressPrefix = new byte[]{(byte)239,(byte)254};
            array[i] = getAddress(addressPrefix, 15);
            System.out.println(array[i]);
        }
        System.out.println("----------");

        for (InetAddress address: array) {
            freeAddress(address);
        }
        System.out.println("----------");
        for (int i=0; i < 16; i++) {
            array[i] = getAddress("0.0.0.0/28");
            System.out.println(array[i]);
        }
        System.out.println(set.size());
    }
}