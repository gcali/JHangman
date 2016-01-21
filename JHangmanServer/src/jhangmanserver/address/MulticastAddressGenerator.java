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
    private final static Set<List<Byte>> addressSet = 
            Collections.newSetFromMap(new ConcurrentHashMap<List<Byte>,Boolean>());
    
    private final static int minPortRange = 49125;
    private final static int maxPortRange = 65535;
    
    
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
    
    public static int getRandomPort() {
        return randomGenerator.nextInt(maxPortRange - minPortRange + 1) +
           minPortRange; 
    }
    
    
    public static void main(String[] args) {
        
//        int dim=20;
//        InetAddress[] array = new InetAddress[dim];
//        
//        for (int i=0; i < dim; i++) {
//            byte[] addressPrefix = new byte[]{(byte)239,(byte)254};
//            array[i] = getAddress(addressPrefix, 15);
//            System.out.println(array[i]);
//        }
//        System.out.println("----------");
//
//        for (InetAddress address: array) {
//            freeAddress(address);
//        }
//        System.out.println("----------");
//        for (int i=0; i < 16; i++) {
//            array[i] = getAddress("0.0.0.0/28");
//            System.out.println(array[i]);
//        }
//        System.out.println(addressSet.size());
        
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