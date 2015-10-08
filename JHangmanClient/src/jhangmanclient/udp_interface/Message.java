package jhangmanclient.udp_interface;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import jhangmanclient.udp_interface.player.GuessWordMessage;

import org.jasypt.encryption.pbe.PBEByteEncryptor;
import org.jasypt.encryption.pbe.StandardPBEByteEncryptor;

public abstract class Message implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final MessageID id;

    protected Message(MessageID id) {
        this.id = id;
    }
    
    public MessageID getID() {
        return this.id;
    }
    
    public byte[] encode(String key) {
		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			 ObjectOutputStream objOut = new ObjectOutputStream(byteOut)) { 
		    objOut.writeObject(this);
		    byte[] data = byteOut.toByteArray();
		    PBEByteEncryptor encryptor = new StandardPBEByteEncryptor();
		    encryptor.setPassword(key);
		    return encryptor.encrypt(data);
		} catch (IOException e) {
		    assert false;
		    return null;
        }
    } 
    
    public static Message decode(byte[] encryptedData, String key) 
        throws IOException {
        PBEByteEncryptor decryptor = new StandardPBEByteEncryptor();
        decryptor.setPassword(key);
        byte[] data = decryptor.decrypt(encryptedData);
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
             ObjectInputStream objIn = new ObjectInputStream(byteIn)) {
            Message receivedMessage = (Message) objIn.readObject();
            return receivedMessage;
        } catch (IOException e) {
            throw e;
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }
    
    public static void main(String[] args) throws IOException {
        Message test = new GuessWordMessage("ciao", "mike");
        byte [] encrypted = test.encode("ciao");
        System.out.println("Byte array length: " + encrypted.length);
        printByteArray(encrypted);
        GuessWordMessage decryptedTest;
        decryptedTest = (GuessWordMessage) Message.decode(encrypted, "ciao");
        System.out.println(decryptedTest.getID());
        System.out.println("Word: " + decryptedTest.getWord() + 
                          " Nick: " + decryptedTest.getNick()); 
        System.out.println("------");
    }

    private static void printByteArray(byte[] byteArray) {
        System.out.print("0x");
        for (byte b : byteArray) {
            System.out.print(Integer.toHexString(b & 0xFF));
        }
        System.out.println();
    }
}