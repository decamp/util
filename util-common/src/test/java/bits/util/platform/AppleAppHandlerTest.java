/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.util.platform;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.util.List;
import javax.swing.*;

import bits.util.platform.AppleAppHandler;


/**
 * @author Philip DeCamp
 */
public class AppleAppHandlerTest extends AppleAppHandler {
    
    
    public static void main( String[] args ) throws Exception {
        JFrame frame = new JFrame();
        AppleAppHandlerTest main = new AppleAppHandlerTest();
        frame.setSize( 1024, 1024 );
        frame.setContentPane( main.mContentPane );
        frame.setLocationRelativeTo( null );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        
        AppleAppHandler.setAboutHandler( main );
        AppleAppHandler.setOpenFileHandler( main );
        AppleAppHandler.setOpenURIHandler( main );
        AppleAppHandler.setPreferencesHandler( main );
        AppleAppHandler.setPrintFilesHandler( main );
        AppleAppHandler.setQuitHandler( main );
        AppleAppHandler.setQuitStrategy( QuitStrategy.SYSTEM_EXIT_0 );
        
        {
            BufferedImage im = new BufferedImage( 512, 512, BufferedImage.TYPE_INT_ARGB );
            Graphics2D g = (Graphics2D)im.getGraphics();
            g.setBackground( new Color( 0, 0, 0, 0 ) );
            g.clearRect( 0, 0, 512, 512 );
            g.setColor( Color.RED );
            g.fillOval( 100, 100, 312, 312 );
            
            AppleAppHandler.setDockIconImage( im );
            AppleAppHandler.setDockIconBadge( "!!!" );
        }
        
        {
            PopupMenu pop = new PopupMenu( "TEST" );
            pop.add( new MenuItem( "HOWDY" ) );
            AppleAppHandler.setDockMenu( pop );
        }
        
        {
            JMenuBar bar = new JMenuBar();
            JMenu menu   = new JMenu( "Antimatter" );
            menu.add( "Dark" );
            menu.add( "Sparse" );
            menu.add( "Dense" );
            
            bar.add( menu );
            
            if( AppleAppHandler.usingScreenMenuBar() ) {
                AppleAppHandler.setDefaultMenuBar( bar );
                main.log( "Added menu bar" );
            } else {
                main.log( "Menu bar not available." );
            }
        }
        
        frame.setVisible( true );
        
        //Thread.sleep( 1000L );
        //AppleAppHandler.openHelpViewer();
    }
    
    
    private final Container mContentPane;
    private final JTextArea mTextArea;

    
    AppleAppHandlerTest() {
        mTextArea    = new JTextArea();
        mContentPane = new JScrollPane( mTextArea );
    }
    
    
    
    @Override
    public QuitAction handleQuit( Object event, Object response ) {
        log( "HANDLE QUIT" );
        return QuitAction.PERFORM;
    }

    
    
    @Override
    public void handleOpenFiles( Object event, List<File> files ) {
        log( "HANDLE OPEN FILE" );
        log( "FILE: " + files.get( 0 ).getAbsolutePath() );
    }
    
    
    public void handleAbout( Object event ) {
        log( "HANDLE ABOUT" );
    }
    
    
    public void handleOpenURI( Object event, URI uri ) {
        log( "HANDLE OPEN URI" );
    }
    
    
    public void handlePreferences( Object event ) {
        log( "HANDLE PREFERENCES" );
    }
    
    
    public void handlePrintFiles( Object event, List<File> files ) {
        log( "HANDLE PRINT FILES" );
    }
    


    private void log( String msg ) {
        System.err.println( msg );
        mTextArea.append( msg + "\n" );
    }
    
}
