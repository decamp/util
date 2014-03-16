/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.util.platform;

import java.awt.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.URI;
import java.util.List;

import javax.swing.JMenuBar;


/**
 * Provides a way to access Mac application extensions ( "com.apple.eawt" )
 * that will compile on any platform. All Mac-only libraries are accessed 
 * only through reflection.
 * <p> 
 * In order to access the application Events, the client should extend this 
 * class and override any of the "handleX" methods to be addressed. The 
 * AppHanlder must then be registered for those events using one of the
 * statically defined "setX" methods. 
 * 
 * @author Philip DeCamp
 */
@SuppressWarnings( {"rawtypes","unused"} )
public class AppleAppHandler implements InvocationHandler {

    
    public enum QuitStrategy {
        SYSTEM_EXIT_0,
        CLOSE_ALL_WINDOWS;
    }
    
    
    public enum QuitAction {
        NONE,
        CANCEL,
        PERFORM
    }
    

    /**
     * @return true iff Apple extensions are available. If not, do not use this class.
     */
    public static boolean isAvailable() {
        try {
            return getClass( "com.apple.eawt.Application" ) != null;
        } catch( ClassNotFoundException ex ) {}
        return false;
    }

    /**
     * @return true iff JVM user has requested use of the screen menu bar via -Dapple.laf.useScreenMenuBar=true"
     */
    public static boolean usingScreenMenuBar() {
        String s = System.getProperty( "apple.laf.useScreenMenuBar" );
        return "true".equalsIgnoreCase( s ); 
    }
    
    /**
     * Probably throws an error.
     */
    public static void openHelpViewer() {
        try {
            Object app  = application();
            Method meth = app.getClass().getMethod( "openHelpViewer" );
            meth.invoke( app );
        } catch( Exception ex ) {
            fail( ex );
        }
    }
    
    
    public static void setAboutHandler( AppleAppHandler handler ) {
        try {
            Class handlerClass = getClass( "com.apple.eawt.AboutHandler" );
            //Class eventClass   = getNestedClass( "com.apple.eawt.AppEvent", "AboutEvent"   );
            Object proxy       = Proxy.newProxyInstance( classLoader(), new Class[]{ handlerClass }, handler );
            Object app         = application();
            Method method      = app.getClass().getMethod( "setAboutHandler", handlerClass );
            method.invoke( app, proxy );
        } catch( Exception ex ) {
            fail( ex );
        }
    }

    
    public static void setDefaultMenuBar( JMenuBar menu ) {
        try {
            Object app    = application();
            Method method = app.getClass().getMethod( "setDefaultMenuBar", JMenuBar.class );
            method.invoke( app, menu );
        } catch( Exception ex ) {
            fail( ex );
        }
    }
    
    
    public static void setDockIconBadge( String badge ) {
        try {
            Object app    = application();
            Method method = app.getClass().getMethod( "setDockIconBadge", String.class );
            method.invoke( app, badge );
        } catch( Exception ex ) {
            fail( ex );
        }
    }
    
    
    public static void setDockIconImage( Image image ) {
        try {
            Object app    = application();
            Method method = app.getClass().getMethod( "setDockIconImage", Image.class );
            method.invoke( app, image );
        } catch( Exception ex ) {
            fail( ex );
        }
    }
    
    
    public static void setDockMenu( PopupMenu menu ) {
        try {
            Object app    = application();
            Method method = app.getClass().getMethod( "setDockMenu", PopupMenu.class );
            method.invoke( app, menu );
        } catch( Exception ex ) {
            fail( ex );
        }
    }
    
    
    public static void setOpenFileHandler( AppleAppHandler handler ) {
        try {
            Class handlerClass = getClass( "com.apple.eawt.OpenFilesHandler" );
            //Class eventClass   = getNestedClass( "com.apple.eawt.AppEvent", "OpenFilesEvent" );
            Object proxy       = Proxy.newProxyInstance( classLoader(), new Class[]{ handlerClass }, handler );
            Object app         = application();
            Method method      = app.getClass().getMethod( "setOpenFileHandler", handlerClass );
            method.invoke( app, proxy );
        } catch( Exception ex ) {
            fail( ex );
        } 
    }
    
    
    public static void setOpenURIHandler( AppleAppHandler handler ) {
        try {
            Class handlerClass = getClass( "com.apple.eawt.OpenURIHandler" );
            //Class eventClass   = getNestedClass( "com.apple.eawt.AppEvent", "OpenURIEvent" );
            Object proxy       = Proxy.newProxyInstance( classLoader(), new Class[]{ handlerClass }, handler );
            Object app         = application();
            Method method      = app.getClass().getMethod( "setOpenURIHandler", handlerClass );
            method.invoke( app, proxy );
        } catch( Exception ex ) {
            fail( ex );
        } 
    }

    
    public static void setPreferencesHandler( AppleAppHandler handler ) {
        try {
            Class handlerClass = getClass( "com.apple.eawt.PreferencesHandler" );
            //Class eventClass   = getNestedClass( "com.apple.eawt.AppEvent" , "PreferencesEvent" );
            Object proxy       = Proxy.newProxyInstance( classLoader(), new Class[]{ handlerClass }, handler );
            Object app         = application();
            Method method      = app.getClass().getMethod( "setPreferencesHandler", handlerClass );
            method.invoke( app, proxy );
        } catch( Exception ex ) {
            fail( ex );
        } 
    }
    
    
    public static void setPrintFilesHandler( AppleAppHandler handler ) {
        try {
            Class handlerClass = getClass( "com.apple.eawt.PrintFilesHandler" );
            //Class eventClass   = getNestedClass( "com.apple.eawt.AppEvent", "PrintFilesEvent" );
            Object proxy       = Proxy.newProxyInstance( classLoader(), new Class[]{ handlerClass }, handler );
            Object app         = application();
            Method method      = app.getClass().getMethod( "setPrintFileHandler", handlerClass );
            method.invoke( app, proxy );
        } catch( Exception ex ) {
            fail( ex );
        } 
    }
    
    
    public static void setQuitHandler( AppleAppHandler handler ) {
        try {
            Class handlerClass  = getClass( "com.apple.eawt.QuitHandler" );
            //Class eventClass    = getNestedClass( "com.apple.eawt.AppEvent", "QuitEvent" );
            //Class responseClass = getClass( "com.apple.eawt.QuitResponse" ); 
            Object proxy        = Proxy.newProxyInstance( classLoader(), new Class[]{ handlerClass }, handler );
            Object app          = application();
            Method method       = app.getClass().getMethod( "setQuitHandler", handlerClass );
            method.invoke( app, proxy );
        } catch( Exception ex ) {
            fail( ex );
        }
    }

    
    @SuppressWarnings( "unchecked" )
    public static void setQuitStrategy( QuitStrategy q ) {
        try {
            Class argClass = getClass( "com.apple.eawt.QuitStrategy" );
            Method meth    = argClass.getMethod( "name" );
            Object[] arr   = argClass.getEnumConstants();
            
            for( int i = 0; i < arr.length; i++ ) {
                String name = (String)meth.invoke( arr[i] );
                if( q.name().equals( name ) ) {
                    Object app    = application();
                    Method method = app.getClass().getMethod( "setQuitStrategy", argClass );
                    method.invoke( app, arr[i] );
                    return;
                }
            }
            
            throw new LinkageError( "Could not find corresponding enum entry for: " + q.name() );
        } catch( Exception ex ) {
            fail( ex );
        }
    }
    
    
    
    public void handleAbout( Object event ) {}
    
    
    public void handleOpenFiles( Object event, List<File> files ) {}
    
    
    public void handleOpenURI( Object event, URI uri ) {}
    
    
    public void handlePreferences( Object event ) {}
    
    
    public void handlePrintFiles( Object event, List<File> files ) {}
    
        
    public QuitAction handleQuit( Object event, Object response ) {
        return QuitAction.PERFORM;
    }
    
    
        
    @SuppressWarnings( "unchecked" )
    public final Object invoke( Object proxy, Method method, Object[] args ) {
        try {
            String name = method.getName().intern();
            
            if( name == "handleAbout" ) {
                handleAbout( args[0] );
                return null;
                
            } else if( name == "openFiles" ) {
                List<File> files = (List<File>)args[0].getClass().getMethod( "getFiles" ).invoke( args[0] );
                handleOpenFiles( args[0], files );
                return null;
            
            } else if( name == "openURI" ) {
                URI uri = (URI)args[0].getClass().getMethod( "getURI" ).invoke( args[0] );
                handleOpenURI( args[0], uri );
                return null;
            
            } else if( name == "handlePreferences" ) {
                handlePreferences( args[0] );
                return null;
            
            } else if( name == "printFiles" ) {
                List<File> files = (List<File>)args[0].getClass().getMethod( "getFiles" ).invoke( args[0] );
                handlePrintFiles( args[0], files );
                return null;
            
            } else if( name == "handleQuitRequestWith" ) {
                QuitAction action = handleQuit( args[0], args[1] );
                
                if( action == QuitAction.CANCEL ) {
                    args[1].getClass().getMethod( "cancelQuit" ).invoke( args[1] );
                } else if( action == QuitAction.PERFORM ) {
                    args[1].getClass().getMethod( "performQuit" ).invoke( args[1] );
                }
                
                return null;
            } 
            
            if( method.getDeclaringClass().equals( Object.class ) ) {
                try {
                    return method.invoke( this, args );
                } catch( IllegalAccessException ex ) {
                    ex.printStackTrace();
                } catch( InvocationTargetException ex ) {
                    ex.printStackTrace();
                }
            }
            
        } catch( Exception ex ) {
            fail( ex );
        }
        
        return null;
    }
    

    
    
    private static final ClassLoader classLoader() {
        return AppleAppHandler.class.getClassLoader();
    }
    
    
    private static final Object application() throws ClassNotFoundException {
        Class<?> clazz = getClass( "com.apple.eawt.Application" );
        try {
            Method m = clazz.getMethod( "getApplication" );
            return m.invoke( null );
        } catch( Exception ex ) {
            throw new ClassNotFoundException( ex.getMessage() );
        }
    }
    
    
    private static final Class getClass( String className ) throws ClassNotFoundException {
        return classLoader().loadClass( className );
    }
    

//    private static final Class getNestedClass( String outerName, String innerName ) throws ClassNotFoundException {
//        Class[] c = getClass( outerName ).getDeclaredClasses();
//        for( int i = 0; i < c.length; i++ ) {
//            if( c[i].getSimpleName().equals( innerName ) ) {
//                return c[i];
//            }
//        }
//        throw new ClassNotFoundException( outerName + "." + innerName );
//    }
    
    
    private static void fail( Exception ex ) throws RuntimeException {
        if( ex instanceof ClassNotFoundException ) {
            LinkageError err = new LinkageError( "Apple application extensions not available." );
            err.initCause( ex );
            throw err;
        }
        
        if( ex instanceof NoSuchMethodException ) {
            LinkageError err = new LinkageError( "Apple application extensions not recognized." );
            err.initCause( ex );
            throw err;
        }
        
        UnknownError err = new UnknownError( "Unknown error accessing Apple application extensions." );
        err.initCause( ex );
        throw err;
    }
    
}
