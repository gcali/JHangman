package jhangmanserver.remote.rmi;

public interface LoggedInChecker {

    public boolean isLoggedIn(String nick, int cookie);
}
