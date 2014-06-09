/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */
package bits.util;

import java.io.*;
import java.net.*;


/**
 * @author Philip DeCamp
 */
public final class Resources {
    
    public static URL find( String path ) throws IOException {
        File file = new File( path );
        if( file.exists() ) {
            return file.toURI().toURL();
        }
        return Resources.class.getClassLoader().getResource( path );
    }

    
    public static String readString( String path ) throws IOException {
        URL url = find( path );
        if( url == null ) {
            return null;
        }
        return Streams.readString( url );
    }
    
    
    private Resources() {}
    
}
