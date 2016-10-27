package jhangmanserver.remote.rmi;

/**
 * Interfaccia per astrarre la funzionalità di controllo dello stato
 * di connessione di un utente
 * @author gcali
 *
 */
public interface LoggedInChecker {

    /**
     * Restituisce true se e solo se l'utente identificato è connesso,
     * e la sessione attiva è quella identificata dal {@code cookie}
     * @param nick nome utente dell'utente connesso
     * @param cookie identificatore di sessione
     * @return true se l'utente è connesso, false altrimenti
     */
    public boolean isLoggedIn(String nick, int cookie);
}
