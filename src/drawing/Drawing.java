package drawing;

/**
 * FIXME: STUB
 * Parent class of all drawing types, subdivided into free line, text, shape.
 * Should inherit various properties from Java 2D API
 * @author Alex Epstein
 */
public abstract class Drawing {

    String artist;

    /**
     * Create a new drawing with the provided artist's signature
     * @param artist
     */
    public Drawing(String artist) {
        this.artist = artist;
    }

}
