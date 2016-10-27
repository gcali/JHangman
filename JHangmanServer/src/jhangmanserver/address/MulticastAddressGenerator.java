package jhangmanserver.address;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Raccolta di utility per la generazione (più o meno) casuale di indirizzi
 * di multicast validi. Utilizza in alcune sue funzioni il {@link AddressRange}
 * 
 * Un indirizzo già generato non può essere nuovamente prodotto, finché
 * non è stato rimosso
 * @author gcali
 *
 */
public class MulticastAddressGenerator {
    
    private final static Random randomGenerator = new Random(); 
    private final static Set<List<Byte>> addressSet = 
            Collections.newSetFromMap(new ConcurrentHashMap<List<Byte>,Boolean>());
    
    private final static int minPortRange = 49125;
    private final static int maxPortRange = 65535;
    
    
    /**
     * Restituisce un indirizzo compreso fra gli estremi
     * @param minAddress il minimo del range
     * @param maxAddress il massimo del range
     * @return l'indirizzo generato
     */
    public static InetAddress getAddress(int minAddress, int maxAddress) { 
        boolean found = false;
        
        do {
            byte[] b = Utils.intToBytes(minAddress);
            found = addressSet.add(arrayToList(b));
        } while (!found && minAddress++ != maxAddress);
        
        if (found) {
            try {
                return InetAddress.getByAddress(Utils.intToBytes(minAddress));
            } catch (UnknownHostException e) {
                assert false;
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }
    
    /**
     * Restituisce un indirizzo appartenente al range
     * @param range il range di indirizzi
     * @return l'indirizzo generato
     */
    public static InetAddress getAddress(AddressRange range) {
        return getAddress(range.getMinAddress(), range.getMaxAddress());
    }
    
    private static List<Byte> arrayToList(byte[] array) {
        List<Byte> l = new ArrayList<Byte>();
        for (byte b : array) {
            l.add(b);
        }
        return l;
    }
    
    /**
     * Libera un indirizzo, in modo che possa essere nuovamente generato
     * @param address l'indirizzo da liberare
     */
    public static void freeAddress(InetAddress address) {
        addressSet.remove(arrayToList(address.getAddress()));
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
    
    /**
     * Restituisce una porta compresa fra il range di porte accettabili
     * @return la porta generata
     */
    public static int getRandomPort() {
        return randomGenerator.nextInt(maxPortRange - minPortRange + 1) +
           minPortRange; 
    }
    
    
    public static void main(String[] args) {
        
        byte[] test = new byte[] {
            0x01,
            0x02,
            0x03,
            0x04 
        };
        
        for (byte b : test) {
            System.out.printf("0x%02X ", b & 0xFF);
        }
        System.out.println();
        
        int temp = Utils.byteToInt(test);
        
        test = Utils.intToBytes(temp);
        for (byte b : test) {
            System.out.printf("0x%02X ", b & 0xFF);
        }
        System.out.println(); 
        
        List<InetAddress> addresses = new ArrayList<InetAddress>();
        
//        AddressRange range = AddressRange.fromRange("192.168.0.1/29");
        AddressRange range = AddressRange.fromMin("192.168.0.1");
        range.setMaxAddress("192.168.0.9");
        for (int i=0; i < 10; i++) {
            InetAddress address = getAddress(range);
            addresses.add(address); 
        }
        
        for (InetAddress a : addresses) {
            System.out.println(a);
            if (a != null) {
                freeAddress(a); 
            }
        }
    }
}