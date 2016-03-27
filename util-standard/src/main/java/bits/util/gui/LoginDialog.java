/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */
package bits.util.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Handy dialog for getting login info.
 * 
 * @author Philip DeCamp
 */
public class LoginDialog extends JDialog {
    
    public static final int OPTION_CONNECT = 0;
    public static final int OPTION_CANCEL  = 1;   
    
    private static final int WINDOW_WIDTH  = 400;
    private static final int WINDOW_HEIGHT = 80; //184;
    private static final int FIELD_HEIGHT  = 26;
    private static final int MARGIN = 4;
    private static final float LEFT_COLUMN_WIDTH = 0.2f;
    
    private static final Font LABEL_FONT = new Font( "Helvetica", Font.PLAIN, 12 );
    private static final Font FIELD_FONT = new Font( "Helvetica", Font.PLAIN, 12 );
    
    private static final String PREFERENCE_KEY_HOST  = "host";
    private static final String PREFERENCE_KEY_DB    = "database";
    private static final String PREFERENCE_KEY_USER  = "user";
    
    private final JLabel mHostLabel;
    private final JLabel mDbLabel;
    private final JLabel mUserLabel;
    private final JLabel mPasswordLabel;
    private final JFormattedTextField mHostField;
    private final JFormattedTextField mDbField;
    private final JFormattedTextField mUserField;
    private final JPasswordField mPasswordField;
    private final JComponent[] mLabels;
    private final JComponent[] mFields;

    private final JButton mCancelButton;
    private final JButton mConnectButton; 
    
    private int mSelectedOption = OPTION_CANCEL;
    

    public LoginDialog( Frame parent, String title, boolean modal ) {
        super( parent, title, modal );
        
        setResizable( false );
        setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
        setLayout( new Layout() );

        Container contentPane = getContentPane();
                
        mHostLabel     = new JLabel( "Host:",     SwingConstants.TRAILING );
        mDbLabel       = new JLabel( "Database:", SwingConstants.TRAILING );
        mUserLabel     = new JLabel( "User:",     SwingConstants.TRAILING );
        mPasswordLabel = new JLabel( "Password:", SwingConstants.TRAILING );
        mHostField     = new JFormattedTextField();
        mDbField       = new JFormattedTextField();
        mUserField     = new JFormattedTextField();
        mPasswordField = new JPasswordField();
        
        mLabels = new JComponent[] {mHostLabel, mDbLabel, mUserLabel, mPasswordLabel};
        mFields = new JComponent[] {mHostField, mDbField, mUserField, mPasswordField};

        for( int i = 0; i < mLabels.length; i++ ) {
            contentPane.add( mLabels[i] );
            contentPane.add( mFields[i] );
            mLabels[i].setFont( LABEL_FONT );
            mFields[i].setFont( FIELD_FONT );
            mFields[i].addKeyListener( new KeyAdapter() {
                public void keyPressed( KeyEvent e ) {
                    if( e.getKeyCode() == KeyEvent.VK_ENTER ) {
                        connectButtonPressed();
                    }
                }
            } );
        }
        
        mCancelButton = new JButton( "Cancel" );
        mConnectButton = new JButton( "Connect" );
        contentPane.add( mCancelButton );
        contentPane.add( mConnectButton );
        
        loadPreferences();
        
        mCancelButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                cancelButtonPressed();
            }
        } );
        
        mConnectButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                connectButtonPressed();
            }
        } );
    
        addComponentListener( new ComponentAdapter() {
            public void componentShown( ComponentEvent e ) {
                mPasswordField.requestFocus();
            }
        } );
    
    }
    
    
    @Override
    public void setVisible( boolean visible ) {
        if( visible ) {
            int vis = 0;
            for( int i = 0; i < mFields.length; i++ ) {
                if( mFields[i].isVisible() ) {
                    vis++;
                }
            }
            
            int w = WINDOW_WIDTH;
            int h = WINDOW_HEIGHT + FIELD_HEIGHT * vis - MARGIN;
            int x;
            int y;
            
            Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
            Window owner = getOwner();
            
            if( owner == null || !owner.isVisible() || owner.getWidth() < 20 && owner.getHeight() < 20 ) {
                x = (int)screenDim.getWidth()  / 2 - w / 2;
                y = (int)screenDim.getHeight() / 2 - h / 2;
            } else {
                Rectangle rect = owner.getBounds();
                x = (int)rect.getCenterX() - w / 2;
                y = (int)rect.getCenterY() - h / 2;
            }
            
            x = Math.max( 0, Math.min( (int)screenDim.getWidth() - w, x ) );
            y = Math.max( 0, Math.min( (int)screenDim.getHeight() - h, y ) );
            
            setBounds( x, y, w, h );
        }

        mPasswordField.setText( "" );
        mPasswordField.requestFocus();
        
        super.setVisible( visible );
    }
    
    
    public int showPasswordLogin() {
        mHostField.setVisible( false );
        mDbField.setVisible( false );
        setVisible( true );
        return mSelectedOption;
    }
    
    public int showDatabaseLogin() {
        mHostField.setVisible( true );
        mDbField.setVisible( true );
        setVisible( true );
        return mSelectedOption;
    }
    
    
    public int selectedOption() {
        return mSelectedOption;
    }

    public String host() {
        return mHostField.getText();
    }
    
    public void host( String host ) {
        mHostField.setText( host );
    }
    
    public String database() {
        return mDbField.getText();
    }
    
    public void database( String db ) {
        mDbField.setText( db );
    }
    
    public String user() {
        return mUserField.getText();
    }
   
    public void user( String user ) {
        mUserField.setText( user );
    }
    
    public String password() {
        return new String( mPasswordField.getPassword() );
    }
    
    public void password( String password ) {
        mPasswordField.setText( password );
    }
    
    
    private void cancelButtonPressed() {
        mSelectedOption = OPTION_CANCEL;
        super.setVisible( false );
    }
    
    private void connectButtonPressed() {
        mSelectedOption = OPTION_CONNECT;
        savePreferences();
        super.setVisible( false );
    }
        
    private void loadPreferences() {
        Preferences p = Preferences.userNodeForPackage( getClass() );
        try {
            p.sync();
        } catch( BackingStoreException ex ) {
            //This is non-critical.
        }
        
        mHostField.setText( p.get( PREFERENCE_KEY_HOST, "" ) );
        mDbField.setText(   p.get( PREFERENCE_KEY_DB,   "" ) );
        mUserField.setText( p.get( PREFERENCE_KEY_USER, System.getProperty( "user.name" ) ) );
    }
        
    private void savePreferences() {
        Preferences p = Preferences.userNodeForPackage(getClass());
        p.put(PREFERENCE_KEY_HOST, mHostField.getText());
        p.put(PREFERENCE_KEY_DB, mDbField.getText());
        p.put(PREFERENCE_KEY_USER, mUserField.getText());
        
        try{
            p.flush();
        }catch(BackingStoreException ex) {}
    }

    
    private class Layout implements LayoutManager {

        public void layoutContainer( Container parent ) {
            int x = MARGIN / 2;
            int y = MARGIN;
            int height      = FIELD_HEIGHT - MARGIN;
            int leftWidth   = (int)((WINDOW_WIDTH - MARGIN * 3) * LEFT_COLUMN_WIDTH);
            int rightWidth  = (WINDOW_WIDTH - MARGIN * 3) - leftWidth;
            int buttonWidth = 100;
            
            for(int i = 0; i < mLabels.length; i++) {
                if( !mFields[i].isVisible() ) {
                    continue;
                }
                
                mLabels[i].setBounds( x, y, leftWidth, height );
                mFields[i].setBounds( x + leftWidth + MARGIN, y, rightWidth, height );
                y += FIELD_HEIGHT;
            }
            
            y += height;
            x = WINDOW_WIDTH - buttonWidth - MARGIN * 2;
            mConnectButton.setBounds( x, y, buttonWidth, 24 );
            x -= buttonWidth + MARGIN;
            mCancelButton.setBounds( x, y, buttonWidth, 24 );
        }

        public void addLayoutComponent( String name, Component comp ) {}

        public void removeLayoutComponent( Component comp ) {}

        public Dimension minimumLayoutSize( Container parent ) { 
            return null;
        }

        public Dimension preferredLayoutSize( Container parent ) {
            return null;
        }

    }
    
}
