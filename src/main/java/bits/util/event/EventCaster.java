/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */
package bits.util.event;

import java.io.*;
import java.lang.ref.*;
import java.lang.reflect.*;
import java.util.concurrent.*;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;


/**
 * Provides a flexible way to call interface methads asynchronously, on multiple
 * objects, using a specified threading strategy.
 * <p>
 * Example:<br>
 * <br>
 * <tt>
 * //You need some object that implements an interface.<br>
 * MouseListener foo = new MouseAdapter() {<br>
 * &nbsp public void mousePressed( MouseEvent e ) {<br>
 * &nbsp&nbsp System.out.println( "I've been accessed!" );<br>
 * &nbsp }<br>
 * };<br><br>
 * //Have the listener join a caster.  The class of the listener interface must be provided.<br>
 * EventCaster<MouseListener> group = EventCaster.create(MouseListener.class);
 * MouseListener groupCaster = group.cast;<br><br>
 * //Broadcast event to all listeners as if calling the method directly.<br>
 * MouseEvent e = new MouseEvent( foo, 0, 0, 0, 0, 0, 0, false );<br>
 * group.cast().mousePressed( e );<br><br>
 * //"I've been accessed!" should be printed to screen.<br>
 * </tt>
 * <p>
 * This cass uses reflection to dynamically generate proxy objects that
 * broadcast interface calls to multiple listeners. Casting may be performed by
 * any java.util.concurrent.Executor, allowing for different threading
 * strategies. By default, the AWT-Event thread is used so that this package
 * will play nicely with AWT and Swing components. For headless applications,
 * you may wish to use a SingleThreadExecutor (which creates a new thread), or
 * no executor (which broadcasts events synchronously by the calling thread).
 * <p>
 * However, AN INTERFACE WITH NON-VOID METHODS CANNOT BE USED FOR BROADCASTING.
 * When distributing events asynchronously, it does not make sense for the
 * caller to expect a synchronous return value. The <tt>EventCaster</tt> and
 * <tt>EventGroup</tt> classes will throw a RuntimeException with an insulting
 * message if you pass them a class to an interface with a non-void method.
 * 
 * @author Philip DeCamp
 * @param <T>
 *            Event interface
 */
public class EventCaster<T> implements EventSource<T>, Closeable {

    public static final int THREADING_AWT = 0;
    public static final int THREADING_DEDICATED = 1;
    public static final int THREADING_SYNCHRONOUS = 2;


    public static <T> EventCaster<T> create( Class<T> clazz ) {
        return new EventCaster<T>( clazz );
    }


    public static <T> EventCaster<T> create( Class<T> clazz, int threadingType ) {
        return new EventCaster<T>( clazz, threadingType );
    }


    public static <T> EventCaster<T> create( Class<T> clazz, Executor executor ) {
        return new EventCaster<T>( clazz, executor );
    }


    /**
     * @return casting object. Any method called on this object will be called
     *         on all listeners.
     */
    public T cast() {
        return mProxy;
    }


    public boolean isOpen() {
        return !mClosed;
    }


    @Override
    public void close() {
        Closeable exec = null;

        synchronized( this ) {
            if( mClosed ) {
                return;
            }
            mClosed = true;
            mListeners = null;
            mListenerCount = 0;
            if( mExecutor instanceof Closeable ) {
                exec = (Closeable)mExecutor;
            }
        }

        if( exec != null ) {
            try {
                exec.close();
            } catch( Exception ex ) {
                ex.printStackTrace();
            }
        }
    }


    @Override
    public synchronized void addListener( T listener ) {
        mListeners = new Node( new StrongRef<T>( listener ), mListeners );
        mListenerCount++;
    }


    public synchronized void addListenerWeakly( T listener ) {
        mListeners = new Node( new WeakReference<T>( listener ), mListeners );
        mListenerCount++;
    }


    @Override
    public synchronized void removeListener( T remove ) {
        // Find tail of list that starts after the last node to remove.
        Node list = null;
        int listSize = 0;

        for( Node node = mListeners; node != null; node = node.mNext ) {
            T item = node.mItem.get();
            if( item == null || remove != null && remove.equals( item ) ) {
                list = null;
                listSize = 0;
            } else if( listSize++ == 0 ) {
                list = node;
            }
        }

        // Check if we need to remove anything.
        if( list == mListeners ) {
            return;
        }

        // Reconstruct list up to tail.
        Node stop = list;
        for( Node node = mListeners; node != stop; node = node.mNext ) {
            T item = node.mItem.get();
            if( item != null && (remove == null || !remove.equals( item )) ) {
                list = new Node( node.mItem, list );
                listSize++;
            }
        }

        mListeners = list;
        mListenerCount = listSize;
    }


    public synchronized int listenerCount() {
        return mListenerCount;
    }


    public Class<T> eventClass() {
        return mEventClazz;
    }


    public Executor executor() {
        return mExecutor;
    }


    public void logExceptions( boolean logExceptions ) {
        mLogExceptions = logExceptions;
    }


    public boolean logExceptions() {
        return mLogExceptions;
    }


    public int vacuum() {
        removeListener( null );
        return mListenerCount;
    }



    // ////// Private ////////

    protected static final Logger sLog = Logger.getLogger( EventCaster.class.getName() );

    private final Class<T> mEventClazz;
    private final Executor mExecutor;
    private boolean mLogExceptions = true;
    private boolean mClosed = false;
    private final T mProxy;

    private Node mListeners = null;
    private int mListenerCount = 0;


    EventCaster( Class<T> clazz ) {
        this( clazz, AWT_EXECUTOR );
    }


    EventCaster( Class<T> clazz, int threadingType ) {
        this( clazz, createExecutor( threadingType ) );
    }


    @SuppressWarnings( "unchecked" )
    EventCaster( Class<T> clazz, Executor executor ) {
        if( !checkInterfaceValidity( clazz ) ) {
            throw new UnsupportedOperationException( "Non-interface or itnerface with non-void methods: " + clazz );
        }

        mEventClazz = clazz;
        mExecutor = executor;

        InvocationHandler handler;
        if( executor == null ) {
            handler = new SyncHandler();
        } else {
            handler = new AsyncHandler();
        }

        mProxy = (T)Proxy.newProxyInstance( mEventClazz.getClassLoader(),
                                            new Class[]{ mEventClazz },
                                            handler );
    }



    //////// Invocation ////////


    private void invoke( Method method, Object[] args ) {
        Node node = mListeners;
        boolean vacuum = false;

        while( node != null ) {
            Reference<T> ref = node.mItem;
            T target = ref.get();
            if( target == null ) {
                vacuum = true;
            } else {
                try {
                    method.invoke( target, args );
                } catch( IllegalAccessException ex ) {
                    ex.printStackTrace();
                    System.err.println( "Internal error within EventCaster. This line should never be reached." );
                } catch( InvocationTargetException ex ) {
                    if( mLogExceptions ) {
                        sLog.severe( "Exception thrown to EventCaster." );
                        ex.printStackTrace();
                    }
                }
            }

            node = node.mNext;
        }

        if( vacuum ) {
            vacuum();
        }
    }
    

    private class SyncHandler implements InvocationHandler {
        @Override
        public Object invoke( Object target, Method method, Object[] args ) {
            EventCaster.this.invoke( method, args );
            return null;
        }
    }


    private class AsyncHandler implements InvocationHandler {
        @Override
        public Object invoke( Object target, Method method, Object[] args ) {
            if( mClosed ) {
                return null;
            }
            mExecutor.execute( new CastAction( method, args ) );
            return null;
        }
    }


    private final class CastAction implements Runnable {
        private final Method mMethod;
        private final Object[] mArgs;

        public CastAction( Method method, Object[] args ) {
            mMethod = method;
            mArgs = args;
            mMethod.setAccessible( true );
        }

        @Override
        public void run() {
            invoke( mMethod, mArgs );
        }

    }



    //////// Construction ////////

    protected static boolean checkInterfaceValidity( Class<?> clazz ) {
        if( !clazz.isInterface() ) {
            return false;
        }

        for( Method m : clazz.getMethods() ) {
            if( m.getReturnType() != Void.TYPE ) {
                return false;
            }
            try {
                m.setAccessible( true );
            } catch( SecurityException ex ) {
                throw new RuntimeException( "EventCaster cannot get permission to access methods for interface " +
                                            clazz.getName() +
                                            ". You must either use a public interface, or change your java security policies." );
            }
        }

        return true;
    }


    //////// Execution ////////

    protected static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        @Override
        public Thread newThread( Runnable run ) {
            Thread ret = new Thread( run, "EventCaster" );
            ret.setDaemon( true );
            return ret;
        }
    };


    protected static final Executor AWT_EXECUTOR = new Executor() {
        @Override
        public void execute( Runnable run ) {
            SwingUtilities.invokeLater( run );
        }
    };


    protected static Executor createExecutor( int threadingType ) {
        switch( threadingType ) {
        case THREADING_AWT:
            return AWT_EXECUTOR;
        case THREADING_DEDICATED:
            return Executors.newSingleThreadExecutor( THREAD_FACTORY );
        case THREADING_SYNCHRONOUS:
            return null;
        default:
            throw new IllegalArgumentException( "Invalid threading type." );
        }
    }


    //////// Listener Collection ////////


    private static final class StrongRef<R> extends WeakReference<R> {

        private final R mObject;

        public StrongRef( R object ) {
            super( null );
            mObject = object;
        }

        @Override
        public R get() {
            return mObject;
        }

        @Override
        public boolean equals( Object obj ) {
            Object a = ((Reference<?>)obj).get();
            return mObject.equals( a );
        }

    }


    private final class Node {
        final Reference<T> mItem;
        final Node mNext;

        Node( Reference<T> item, Node next ) {
            mItem = item;
            mNext = next;
        }
    }



    //////// Deprecation Ghetto ////////

    @Deprecated
    public static <T> EventCaster<T> newInstance( Class<T> clazz ) {
        return create( clazz );
    }


    @Deprecated
    public static <T> EventCaster<T> newInstance( Class<T> clazz, int threadingType ) {
        return create( clazz, threadingType );
    }


    @Deprecated
    public static <T> EventCaster<T> newInstance( Class<T> clazz, Executor executor ) {
        return create( clazz, executor );
    }

}