package jhangmanclient.client;

import java.rmi.RemoteException;
import java.util.Scanner;

import rmi_interface.RMIServer;
import rmi_interface.UserAlreadyLoggedInException;
import rmi_interface.UserAlreadyRegisteredException;
import rmi_interface.UserNotLoggedException;
import rmi_interface.WrongPasswordException;

public class ClientTask implements Runnable {

    private RMIServer server;
    private boolean logged;
    private String nick;
    private int cookie;

    private static final String [] options = {"Register", "Login", "Logout", "Exit"};

    public ClientTask(RMIServer server) {
        this.server = server;
    }

    private boolean handleOption(int index, RMIServer server) {
        if (index < 0 || index >= options.length) {
            throw new IllegalArgumentException("Received option " + index);
        }
        switch (options[index]) {
        case "Exit":
            return true;
        case "Login":
            return handleLogin();
        case "Logout":
            return handleLogout();
        case "Register":
            return handleRegister(server);
        default:
            return false;
        }
    }

    private boolean handleLogout() {
        if (!this.logged) {
            System.err.println("User not logged in");
        }
        try {
            this.server.logout(this.nick, this.cookie);
            this.logged = false;
        } catch (RemoteException e) {
            System.err.println("Connection error");
        } catch (UserNotLoggedException e) {
            System.err.println("User not logged in");
        }
        return false;
    }

    private boolean handleRegister(RMIServer server) {
        UserData data = getNickPassword();
        String localNick = data.getNick();
        String localPassword = data.getPassword();
        try {
            server.register(localNick, localPassword);
        } catch (RemoteException e) {
            System.err.println("Connecton error");
            throw new RuntimeException(e);
        } catch (UserAlreadyRegisteredException e) {
            System.err.println("User already registered");
        }
        return false;
    }
    
    private UserData getNickPassword() {
        Scanner inScanner = new Scanner(System.in);
        System.out.println("Insert your username");
        String localUser = inScanner.nextLine();
        System.out.println("Insert your password");
        String localPassword = inScanner.nextLine();
        return new UserData(localUser, localPassword);
    }

    private boolean handleLogin() {
        if (this.logged) {
            System.err.println("Already logged in!");
            return false;
        }
        UserData data = getNickPassword();
        String localNick = data.getNick();
        String localPassword = data.getPassword();
        try {
            this.cookie = this.server.login(localNick, localPassword, null);
            this.nick = localNick;
            this.logged = true;
        } catch (RemoteException e) {
            System.err.println("Connection error");
        } catch (UserAlreadyLoggedInException e) {
            System.err.println("User already logged in");
        } catch (WrongPasswordException e) {
            System.err.println("Wrong data");
        } 
        return false;
    }

    private int getOption() {
        Scanner scanner = new Scanner(System.in);
        int option = scanner.nextInt() - 1;
        if (option < 0 || option >= options.length) {
            throw new RuntimeException("Invalid character " + option);
        }
        return option;
    }

    private void printMenu() {
        System.out.print("Status: ");
        if (this.logged) {
            System.out.println("logged in, " + this.nick + ", session id: " + this.cookie);
        } else {
            System.out.println("not logged in");
        }
        System.out.println("Choose an option");
        for (int i=0;i<options.length;i++) {
            System.out.printf("%2d) %s\n", i+1, options[i]);
        } 
    } 

    @Override
    public void run() {
        boolean done = false;
        
        while (!done) {
            printMenu();
            int option = getOption();
            done = handleOption(option, server);
        }
        // TODO Auto-generated method stub

    }
    
    private class UserData {
        private final String nick;
        private final String password;
        
        public UserData(String nick, String password) {
            this.nick = nick;
            this.password = password;
        }

        public String getNick() {
            return nick;
        }

        public String getPassword() {
            return password;
        } 
        
    }

}
