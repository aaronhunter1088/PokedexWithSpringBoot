package pokedex.services;

import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

/**
 * Holds the darkmode preference for the current HTTP session.
 */
@Service
@SessionScope
public class DarkmodeService
{
    private boolean darkmode;

    /**
     * Set the darkmode state
     * @param darkmode - true for darkmode, false for lightmode
     */
    public void setDarkmode(boolean darkmode) { this.darkmode = darkmode; }

    /**
     * Get the current darkmode state
     * @return current darkmode state
     */
    public boolean isDarkmode() {
        return darkmode;
    }

    /**
     * Toggle darkmode on/off
     */
    public void toggleDarkmode() { setDarkmode(!darkmode); }

}
