package bits.util.event;

/** 
 * @author Philip DeCamp  
 */
public interface EventSource<T> {
    public void addListener( T listener );
    public void removeListener( T listener );
}
