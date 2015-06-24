package jhangmanserver.users;

import org.jasypt.util.password.StrongPasswordEncryptor;

import rmi_interface.ClientCallbackRMI;

public class User {
    
    private String nick;
    private String hashedString;
    private StrongPasswordEncryptor encryptor = new StrongPasswordEncryptor();
    private ClientCallbackRMI callback = null;
    private int cookie;
    private boolean loggedIn = false;
    
    public User(String nick) {
        this.nick = nick;
        this.hashedString = null;
    }
    
    public User(String nick, String password) {
        this.nick = nick;
        this.setPassword(password);
    }
    
    public synchronized String getNick() {
        return this.nick;
    }
    
    public synchronized void setPassword(String password) {

        this.hashedString = encryptor.encryptPassword(password);
    }
    
    public synchronized boolean isPasswordCorrect(String password) {
        return this.encryptor.checkPassword(password, this.hashedString);
    }
    
    public synchronized void setCallback(ClientCallbackRMI callback) {
        this.callback = callback;
    }
    
    public synchronized void removeCallback() {
        this.callback = null;
    }
    
    public synchronized ClientCallbackRMI getCallback(ClientCallbackRMI callback) {
        return this.callback;
    }
    
    public synchronized void setCookie(int cookie) {
        this.cookie = cookie;
    }
    
    public synchronized boolean checkCookie(int cookie) {
        return this.cookie == cookie;
    }
    
    public synchronized void setLoggedIn(boolean b) {
        this.loggedIn = b;
    }
    
    public synchronized boolean isLoggedIn() {
        return this.loggedIn;
    }
    
    public static void main(String[] args) {
        String nick = "testUser";
        String password = "testPassword";
        String wrongPassword = "testpassword";
        try {
            nick = args[0];
            password = args[1];
            wrongPassword = args[2];
        } catch (ArrayIndexOutOfBoundsException e) {
            
        }
        
        User user = new User(nick, password);
        System.out.println("Testing right password. Expected: true");
        System.out.println(user.isPasswordCorrect(password));
        
        System.out.println("Testing wrong password. Expected: false");
        System.out.println(user.isPasswordCorrect(wrongPassword));
        
    } 
}