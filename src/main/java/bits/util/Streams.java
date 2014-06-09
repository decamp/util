/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */
package bits.util;

import java.io.*;
import java.net.*;

public final class Streams {
    
    
    public static void copy( URL url, File outFile ) throws IOException {
        InputStream in   = null;
        OutputStream out = null;
        
        try {
            in  = url.openConnection().getInputStream();
            out = new FileOutputStream( outFile );
            copy( in, new byte[1024*8], out );
        } finally {
            close( in );
            close( out );
        }
    }
    
    
    public static void copy( InputStream in, OutputStream out ) throws IOException {
        copy( in, new byte[1024*8], out );
    }
    
    
    public static void copy( InputStream in, byte[] work, OutputStream out ) throws IOException {
        while( true ) {
            int n = in.read( work );
            if( n <= 0 ) {
                break;
            }
            out.write( work, 0, n );
        }
    }   
    
    
    public static String readString( URL url ) throws IOException {
        InputStream in = null;
        try {
            URLConnection conn = url.openConnection();
            in = conn.getInputStream();
            return readString( in );
        } finally {
            close( in );
        }
    }
    
    
    public static String readString( File file ) throws IOException {
        InputStream in = new BufferedInputStream( new FileInputStream( file ) );
        try {
            return readString( in );
        } finally {
            close( in );
        }
    }
    
    
    public static String readString( InputStream in ) throws IOException {
        return readString( new InputStreamReader( in ) );
    }
    
    
    public static String readString( Reader in ) throws IOException {
        return readString( in, new char[8*1024] );
    }

    
    public static String readString( Reader in, char[] work ) throws IOException {
        StringBuilder s = new StringBuilder();
        while( true ) {
            int n = in.read( work );
            if( n <= 0 ) {
                break;
            }
            s.append( work, 0, n );
        }
        return s.toString();
    }
    
    
    public static void close( InputStream in ) {
        if( in == null ) {
            return;
        }
        try {
            in.close();
        } catch( IOException ex ) {}
    }
    
    
    public static void close( OutputStream out ) {
        if( out == null ) {
            return;
        }
        try {
            out.close();
        } catch( IOException ex ) {}
    }
    
    
    public static void close( Reader in ) {
        if( in == null ) {
            return;
        }
        try {
            in.close();
        } catch( IOException ex ) {}
    }
    
    
    public static void close( Writer w ) {
        if( w == null ) {
            return;
        }
        try {
            w.close();
        } catch( IOException ex ) {}
    }
    
    
    private Streams() {}
    
}
