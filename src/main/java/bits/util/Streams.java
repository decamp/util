/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */
package bits.util;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;


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


    public static boolean isLocalFile( URL url ) {
        String host = url.getHost();
        if( host != null && !host.isEmpty() ) {
            return false;
        }
        String proto = url.getProtocol();
        return "file".equalsIgnoreCase( proto );
    }


    public static ByteBuffer readBytes( File file ) throws IOException {
        FileChannel chan = new FileInputStream( file ).getChannel();
        long length = chan.size();
        if( length > Integer.MAX_VALUE ) {
            throw new IOException( "File to large to buffer." );
        }
        ByteBuffer buf = ByteBuffer.allocateDirect( (int)length );
        while( buf.remaining() > 0 ) {
            chan.read( buf );
        }
        buf.flip();
        return buf;
    }


    public static ByteBuffer readBytes( URL url ) throws IOException {
        if( isLocalFile( url ) ) {
            return readBytes( new File( url.getFile() ) );
        }
        InputStream in = null;
        try {
            URLConnection conn = url.openConnection();
            in = new BufferedInputStream( conn.getInputStream() );
            return readBytes( in );
        } finally {
            close( in );
        }
    }


    public static ByteBuffer readBytes( InputStream in ) throws IOException {
        final int BLOCK_SIZE = 1024 * 8;
        List<byte[]> blocks = new ArrayList<byte[]>();
        int totalSize = 0;

        while( true ) {
            byte[] block = new byte[BLOCK_SIZE];
            int pos = 0;
            while( pos < block.length ) {
                int n = in.read( block );
                if( n <= 0 ) {
                    break;
                }
                pos += n;
            }

            blocks.add( block );
            totalSize += pos;
            if( pos < BLOCK_SIZE ) {
                break;
            }
        }

        ByteBuffer ret = ByteBuffer.allocateDirect( totalSize );
        for( byte[] block: blocks ) {
            ret.put( block, 0, Math.min( block.length, totalSize ) );
            totalSize -= block.length;
        }

        ret.flip();
        return ret;
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
