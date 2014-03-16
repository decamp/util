/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */
package bits.util;

import java.awt.Frame;
import java.io.*;
import java.net.InetAddress;
import java.sql.*;
import java.util.*;

import javax.swing.*;

import bits.util.gui.LoginDialog;


/**
 * Convenience functions for dealing with SQL databases.
 * 
 * @author Philip DeCamp
 */
public final class Sql {
    
    
    public enum Lang {
        MYSQL      ( "com.mysql.jdbc.Driver",  "jdbc:mysql:"     ),
        SQLITE     ( "org.sqlite.JDBC",        "jdbc:sqlite:"    ),
        SQLCIPHER  ( "org.sqlite.JDBC",        "jdbc:sqlcipher:" );
        
        
        private final String mDriver;
        private final String mProtocol;
        private int mState;
        
        Lang( String driver, String protocol ) {
            mDriver   = driver;
            mProtocol = protocol;
            mState    = NOT_LOADED;
        }
        
        synchronized boolean load() {
            if( mState == NOT_LOADED ) {
                try {
                    Class.forName( mDriver );
                    mState = LOADED;
                } catch( ClassNotFoundException ex ) {
                    mState = NOT_FOUND;
                }
            }
            
            return mState == LOADED;
        }

        String protocol() {
            return mProtocol;
        }
        
        void assertLoaded() throws SQLException {
            if( !load() ) {
                throw new SQLException( name() + " driver not found." );
            }
        }
        
    }   
    
    
    public static final String PROP_FORMAT       = "schema.type";
    public static final String PROP_HOST         = "user.host";
    public static final String PROP_USER         = "user.name";
    public static final String PROP_VERSION      = "schema.version";
    
    
    
    public static Connection open( Lang lang, String host, String db, String user, String password ) throws SQLException {
        lang.assertLoaded();
        
        String url = lang.protocol() + "//" + host + "/" + db;
        Properties props = new Properties();
        props.setProperty( "user", user );
        props.setProperty( "password", password );
        // props.setProperty( "ssl", "true" );
        
        Connection conn = DriverManager.getConnection( url, props );
        conn.setAutoCommit( false );
        return conn;
    }
    
    
    public static Connection openWithDialog( Lang lang, Frame optParent, String optTitle ) throws SQLException {
        lang.assertLoaded();
        
        if( optTitle == null ) {
            optTitle = "Login";
        }
        
        LoginDialog dialog = new LoginDialog( optParent, optTitle, true );

        while( true ) {
            int ret = dialog.showDatabaseLogin();
            if( ret != LoginDialog.OPTION_CONNECT ) {
                return null;
            }

            try {
                return open( lang, dialog.host(), dialog.database(), dialog.user(), dialog.password() );
            } catch( SQLException ex ) {
                JOptionPane.showMessageDialog( optParent, ex.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE );
            }
        }
    }
    
    
    public static Connection openWithPasswordDialog( Lang lang, Frame optParent, String optTitle, String host, String db ) throws SQLException {
        lang.assertLoaded();
        
        if( optTitle == null ) {
            optTitle = "Login";
        }
        
        LoginDialog dialog = new LoginDialog( optParent, optTitle, true );
        
        while( true ) {
            int ret = dialog.showPasswordLogin();
            if( ret != LoginDialog.OPTION_CONNECT ) {
                return null;
            }
            
            try {
                return open( lang, host, db, dialog.user(), dialog.password() );
            } catch( SQLException ex ) {
                JOptionPane.showMessageDialog( optParent, ex.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE );
            }
        }
    }

    
    public static Connection openFile( Lang lang, File file ) throws SQLException {
        lang.assertLoaded();
        String url = lang.protocol() + file.getPath();
        Connection conn = DriverManager.getConnection( url );
        conn.setAutoCommit( false );
        return conn; 
    }


    public static Connection openEncryptedFile( Lang lang, File file, byte[] key ) throws SQLException {
        lang.assertLoaded();
        
        char[] arr = new char[key.length];
        for( int i = 0; i < key.length; i++ ) {
            arr[i] = (char)key[i];
        }
        
        String url = lang.protocol() + file.getPath();
        Connection conn = DriverManager.getConnection( url, null, new String( arr ) );
        conn.setAutoCommit( false );
        return conn;
    }


    /**
     * I got sick of writing try/catch blocks.
     * 
     * @param st
     */
    public static void close( Statement st ) {
        if( st != null ) {
            try {
                st.close();
            } catch( Exception ex ) {}
        }
    }

    /**
     * I got sick of writing try/catch blocks.
     * 
     * @param st
     */
    public static void close( ResultSet st ) {
        if( st != null ) {
            try {
                st.close();
            } catch( Exception ex ) {}
        }
    }

    /**
     * I got sick of writing try/catch blocks.
     * 
     * @param st
     */
    public static void close( Connection c ) {
        if( c != null ) {
            try {
                c.close();
            } catch( Exception ex ) {}
        }
    }


    /**
     * Creates a default Properties object for a SQLite DB.
     * 
     * @param format
     *            Schema format
     * @param version
     *            Schema version
     * @return New, lightly-populated properties object.
     */
    public static Properties newProperties( String format, String version ) {
        Properties props = new Properties();
        props.put( PROP_FORMAT, format );
        props.put( PROP_VERSION, version );
        props.put( PROP_USER, System.getProperty( "user.name" ) );

        try {
            props.put( PROP_HOST, InetAddress.getLocalHost().getHostName() );
        } catch( Exception ex ) {}

        return props;
    }

    /**
     * Reads in the properties table of a DB using the default table name.
     * Equivalent to calling <code>readProperties(in, "properties");</code>
     * 
     * @param in
     * @return
     * @throws SQLException
     */
    public static Properties readProperties( Connection in ) throws SQLException {
        return readProperties( in, "properties" );
    }

    /**
     * Reads in a properties table of a DB. Properties tables have two text
     * columns: <i>name</i> and <i>value</i>.
     * 
     * @param in
     * @param table
     *            Name of properties table.
     * @return
     * @throws SQLException
     */
    public static Properties readProperties( Connection in, String table ) throws SQLException {
        Statement s = in.createStatement();
        ResultSet rs = null;
        Properties p = null;

        try {
            rs = s.executeQuery( "SELECT name, value FROM " + table );
            p = new Properties();

            while( rs.next() ) {
                p.setProperty( rs.getString( 1 ), rs.getString( 2 ) );
            }
        } finally {
            close( rs );
            close( s );
        }

        return p;
    }

    /**
     * Writes a Properties object to a database using the default table name.
     * Equivalent to calling
     * <code>writeProperties(props, out, "properties");</code> If a "properties"
     * table already exists in the DB, it will be overwritten.
     * 
     * @param props
     * @param out
     * @throws SQLException
     */
    public static void writeProperties( Properties props, Connection out ) throws SQLException {
        writeProperties( props, out, "properties" );
    }

    /**
     * Writes a Properties object to a database. If the table already exists, it
     * will be overwritten.
     * 
     * @param props
     * @param out
     * @param table
     * @throws SQLException
     */
    public static void writeProperties( Properties props, Connection out, String table ) throws SQLException {
        Statement s = out.createStatement();

        try {
            s.execute( "DROP TABLE IF EXISTS " + table );
            s.execute( "CREATE TABLE " + table + " (name TEXT, value TEXT)" );
            s.execute( "CREATE INDEX " + table + "_name ON " + table + "(name)" );
            s.close();
        } finally {
            close( s );
        }

        if( props == null )
            return;

        PreparedStatement ps = out.prepareStatement( "INSERT INTO " + table + "(name, value) VALUES (?,?)" );

        try {
            Enumeration<?> names = props.propertyNames();
            while( names.hasMoreElements() ) {
                String key = names.nextElement().toString();
                String value = props.getProperty( key );

                ps.setString( 1, key );
                ps.setString( 2, value );
                ps.executeUpdate();
            }
        } finally {
            close( ps );
        }
    }


    
    public static boolean loadDriver( Lang lang ) {
        return lang.load();
    }
    
    

    private Sql() {}
 
    private static int NOT_LOADED = 0;
    private static int NOT_FOUND  = 1;
    private static int LOADED     = 2;
    
}