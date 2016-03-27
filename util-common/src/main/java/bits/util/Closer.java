package bits.util;

/**
 * @author Philip DeCamp
 */
public class Closer {

    /**
     * Closes an object, discarding any exceptions.
     */
    public static void close( AutoCloseable c ) {
        if( c != null ) {
            try {
                c.close();
            } catch( Exception ignore ) {}
        }
    }

}
