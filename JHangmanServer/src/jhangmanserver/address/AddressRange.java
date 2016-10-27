package jhangmanserver.address;

/**
 * Rappresenta un range di indirizzi, dove per range si intende un intervallo
 * chiuso e limitato da un indirizzo minimo e un indirizzo massimo
 * @author gcali
 *
 */
public class AddressRange {
    
    //One of the two should always be set
    private Integer minAddress = null;
    private Integer maxAddress = null;
    
    private AddressRange() { 
    }
    
    /**
     * Costruisce un range di indirizzi
     * 
     * Il range è specificato dal parametro {@code stringAddressRange}, che deve 
     * essere della forma
     * {@code
     *      a.b.c.d/e
     * }
     * 
     * dove {@code a, b, c, d} sono valori interi positivi fra 0 e 255 e
     * {@code e} è un valore intero positivo compreso fra 0 e 32.
     * 
     * 
     * @param stringAddressRange il range degli indirizzi da restituire
     * @return un {@code AddressRange} rappresentante il range specificato
     */
    public static AddressRange fromRange(String stringAddressRange) {
        return createRange(parseRange(stringAddressRange));
    }
    
    public static AddressRange fromMin(int minAddress) {
        AddressRange ar = new AddressRange();
        ar.minAddress = minAddress;
        return ar;
    }
    
    public static AddressRange fromMax(int maxAddress) {
        AddressRange ar = new AddressRange();
        ar.maxAddress = maxAddress;
        return ar;
    }

    public static AddressRange fromMin(String minAddress) {
        AddressRange ar = new AddressRange();
        ar.minAddress = Utils.addressToInt(minAddress);
        return ar;
    }
    
    public static AddressRange fromMax(String maxAddress) {
        AddressRange ar = new AddressRange();
        ar.maxAddress = Utils.addressToInt(maxAddress);
        return ar;
    }
    
    public void setMinAddress(String address) {
        minAddress = Utils.addressToInt(address); 
    }
    
    public void setMaxAddress(String address) {
        maxAddress = Utils.addressToInt(address); 
    }
    
    public void setMinAddress(int minAddress) {
        this.minAddress = minAddress;
    }
    
    public void setMaxAddress(int maxAddress) {
        this.maxAddress = maxAddress;
    }
    
    public int getMinAddress() {
        if (minAddress == null) {
            return maxAddress;
        } else {
            return minAddress;
        }
    }
    
    public int getMaxAddress() {
        if (maxAddress == null) {
            return minAddress;
        } else {
            return maxAddress;
        }
    }
    
    private static AddressRange createRange(PrefixFixed parseRange) {

        int fixed = parseRange.fixed;
        byte[] prefix = parseRange.prefix;

        if (fixed > 32 || fixed < 0) {
            throw new IllegalArgumentException("Can't have " + fixed + " fixed bits");
        }
        if (prefix.length * 8 < fixed) {
            throw new IllegalArgumentException("Prefix isn't long enough");
        }
        
        byte[] complete_base_address = new byte[4];
        for (int i=0; i < complete_base_address.length; i++) {
            if ( i < prefix.length) {
                complete_base_address[i] = prefix[i];
            } else {
                complete_base_address[i] = 0;
            }
        }
        
        int total = 32;
        int needed = total - fixed;

        int fixed_component = Utils.byteToInt(complete_base_address) >>> needed;
        
        int minAddress = fixed_component << needed;
        int maxAddress = 0;
        if (needed == 0) {
            maxAddress = minAddress ;
        } else {
            maxAddress = (int)(
                ((long)(
                    (fixed_component + 1) << needed & 0xFFFFFFFFL
                )) - 1L
            );
        }
        
        AddressRange addressRange = new AddressRange();
        addressRange.minAddress = minAddress;
        addressRange.maxAddress = maxAddress;
        
        return addressRange;
    }

    private static PrefixFixed parseRange(String stringAddressRange) {
        if (stringAddressRange == null) {
            throw new IllegalArgumentException("Argument was null");
        }
        String [] addressFixed = stringAddressRange.split("/");
        if (addressFixed.length != 2) {
            throw new IllegalArgumentException("Argument malformed: " + stringAddressRange);
        }
        String [] stringAddressBytes = addressFixed[0].split("\\.");
        if (stringAddressBytes.length != 4) {
            throw new IllegalArgumentException("Argument malformed: "+ stringAddressRange);
        }
        byte[] byteAddress = new byte[stringAddressBytes.length];
        for (int i=0; i < stringAddressBytes.length; i++) {
            byteAddress[i] = (byte) (Integer.parseInt(stringAddressBytes[i]));
        }
        int fixed = Integer.parseInt(addressFixed[1]);
        
        PrefixFixed pf = new PrefixFixed();
        pf.prefix = byteAddress;
        pf.fixed = fixed;
        
        return pf; 
    }


    private static class PrefixFixed {
        
        public byte[] prefix;
        public int fixed;
        
    }

}
