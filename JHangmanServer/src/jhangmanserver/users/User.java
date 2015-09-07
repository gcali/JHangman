package jhangmanserver.users;

import org.jasypt.util.password.StrongPasswordEncryptor;

import rmi_interface.ClientCallbackRMI;

public class User {
    
    private String nick;
    private String hashedPassword = null;
    private StrongPasswordEncryptor encryptor;
    private int cookie;
    private boolean loggedIn = false;
    
    public User(String nick, String password, boolean shouldEncrypt) {
        if (password == null) {
            throw new NullPointerException("password field expected to be not 'null'");
        }

        this.nick = nick;
        if (shouldEncrypt) {
            this.encryptor = new StrongPasswordEncryptor();
        } else {
            this.encryptor = null;
        }
        this.setPassword(password);
    }
    
    public User(String nick, String password) {
        this(nick, password, true);
    }
    
    public synchronized String getNick() {
        return this.nick;
    }
    
    public synchronized void setPassword(String password) { 
        if (this.encryptor == null) {
            this.hashedPassword = password;
        } else {
            this.hashedPassword = encryptor.encryptPassword(password);
        }
    }
    
    public synchronized boolean isPasswordCorrect(String password) {
        if (this.encryptor == null) {
            return this.hashedPassword.equals(password);
        } else {
            return this.encryptor.checkPassword(password, this.hashedPassword);
        }
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
        boolean shouldEncrypt = true;
        String nick = "testUser";
        String password = "testPassword";
        String wrongPassword = "testpassword";
        try {
            shouldEncrypt = Boolean.parseBoolean(args[0]);
            nick = args[1];
            password = args[2];
            wrongPassword = args[3];
        } catch (ArrayIndexOutOfBoundsException e) {
            
        }
        
        User user = new User(nick, password, shouldEncrypt);
        System.out.println("Testing right password. Expected: true");
        System.out.println(user.isPasswordCorrect(password));
        
        System.out.println("Testing wrong password. Expected: false");
        System.out.println(user.isPasswordCorrect(wrongPassword));
        
    } 
    
    public synchronized void logOut() {
        this.setLoggedIn(false);
//        this.removeCallback();
    }
}
