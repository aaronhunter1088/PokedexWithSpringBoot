package pokedex.services;

import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

/**
 * Holds the show GIFs preference for the current HTTP session.
 */
@Service
@SessionScope
public class GifService
{
    private boolean showGifs;

    /**
     * Set the gif state
     * @param showGifs - true for showing GIFs, false for not showing GIFs
     */
    public void setShowGifs(boolean showGifs) {
        this.showGifs = showGifs;
    }

    /**
     * Get the current gif state
     * @return current gif state
     */
    public boolean isShowGifs() {
        return showGifs;
    }

    /**
     * Toggle gif state on/off
     */
    public void toggleShowGifs() {
        setShowGifs(!showGifs);
    }

}
