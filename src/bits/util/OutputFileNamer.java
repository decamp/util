/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */
package bits.util;

import java.io.File;

/** 
 * Generates sequential file names. Does not return files that already exist.
 *  
 * @author Philip DeCamp  
 */
public class OutputFileNamer{
    
    private File mDirectory;
    private String mNameStart;
    private String mNameEnd;
    private String mFormat;
    private int mNumber;
    
    
    public OutputFileNamer( File directory, String nameStart, String nameEnd ) {
        this( directory, nameStart, nameEnd, 3, 0 );
    }
    
    
    public OutputFileNamer( File directory, String nameStart, String nameEnd, int minDigits ) {
        this( directory, nameStart, nameEnd, minDigits, 0 );
    }
    
    
    public OutputFileNamer( File directory, String nameStart, String nameEnd, int minDigits, int numStart ) {
        mDirectory = directory;
        mNameStart = nameStart;
        mNameEnd   = nameEnd;
        mFormat    = "%s%0" + minDigits + "d%s";
        mNumber    = numStart;
    }
    
    
    
    public File next() {
        while( true ) {
            String name = String.format( mFormat, mNameStart, mNumber++, mNameEnd );
            File file = new File( mDirectory, name );
            if( !file.exists() ) {
                return file;
            }
        }
    }
    
    
    /**
     * @deprecated
     */
    public File getNextFile() {
        while( true ) {
            String name = String.format( mFormat, mNameStart, mNumber++, mNameEnd );
            File file = new File( mDirectory, name );
            if( !file.exists() ) {
                return file;
            }
        } 
    }

}
