package users;

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
    
    public String getNick() {
        return this.nick;
    }
    
    public void setPassword(String password) {
        this.hashedString = encryptor.encryptPassword(password);
    }
    
    public boolean isPasswordCorrect(String password) {
        return this.encryptor.checkPassword(password, this.hashedString);
    }
    
    public void setCallback(ClientCallbackRMI callback) {
        this.callback = callback;
    }
    
    public void removeCallback() {
        this.callback = null;
    }
    
    public ClientCallbackRMI getCallback(ClientCallbackRMI callback) {
        return this.callback;
    }
    
    public void setCookie(int cookie) {
        this.cookie = cookie;
    }
    
    public boolean checkCookie(int cookie) {
        return this.cookie == cookie;
    }
    
    public void setLoggedIn(boolean b) {
        this.loggedIn = b;
    }
    
    public boolean isLoggedIn() {
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