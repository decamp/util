package bits.util.event;

import java.util.*;
import org.junit.Test;

import bits.util.event.EventCaster;
import static org.junit.Assert.*;

/**
 * @author Philip DeCamp
 */
public class EventCasterTest {

    @Test
    public void checkWeakVacuum() throws Exception {
        EventCaster<Notmuch> caster = EventCaster.create( Notmuch.class );
        List<Bar> list = new ArrayList<Bar>();
        for( int i = 0; i < 1000; i++ ) {
            list.add( new Bar() );
            caster.addListenerWeakly( list.get( i ) );
        }
        
        assertTrue( caster.listenerCount() == 1000 );
        
        Random rand = new Random( 12 );
        for( int i = 0; i < 100 ; i++ ) {
            //caster.removeListener( list.remove( rand.nextInt( list.size() ) ) );
            Bar bar = list.remove( rand.nextInt( list.size() ) );
        }
        
        Thread.sleep( 50 );
        System.gc();
        Thread.sleep( 50 );
        
        caster.vacuum();
        assertTrue( caster.listenerCount() == 900 );
    }

    
    @Test
    public void checkStrongRemoval() throws Exception {
        EventCaster<Notmuch> caster = EventCaster.create( Notmuch.class );
        List<Bar> list = new ArrayList<Bar>();
        for( int i = 0; i < 1000; i++ ) {
            list.add( new Bar() );
            caster.addListener( list.get( i ) );
        }
        
        assertTrue( caster.listenerCount() == 1000 );
        
        Random rand = new Random( 12 );
        for( int i = 0; i < 100 ; i++ ) {
            caster.removeListener( list.remove( rand.nextInt( list.size() ) ) );
        }
        
        caster.vacuum();
        assertTrue( caster.listenerCount() == 900 );
    }
    

    static interface Notmuch {}


    static class Bar implements Notmuch {}

}
