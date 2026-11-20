package pokedex.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

/**
 * Holds the show GIFs preference for the current HTTP session.
 */
@Service
@SessionScope
public class GifService
{
    private static final Logger LOGGER = LogManager.getLogger(GifService.class);

    private boolean showGifs;

    /**
     * Set the dark mode state
     * @param showGifs - true for dark mode, false for light mode
     */
    public void setShowGifs(boolean showGifs) {
        this.showGifs = showGifs;
    }

    /**
     * Get the current dark mode state
     * @return current dark mode state
     */
    public boolean isShowGifs() {
        return showGifs;
    }

    /**
     * Toggle dark mode on/off
     */
    public void toggleShowGifs() {
        setShowGifs(!showGifs);
    }

}
