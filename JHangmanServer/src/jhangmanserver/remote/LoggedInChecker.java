package jhangmanserver.remote;

public interface LoggedInChecker {

    public boolean isLoggedIn(String nick, int cookie);
}
