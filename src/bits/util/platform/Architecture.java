/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.util.platform;

import java.nio.ByteOrder;

/**
 * Enum for system architectures.
 *  
 * @author Philip DeCamp  
 */
public enum Architecture {
    I386    ( 32, ByteOrder.LITTLE_ENDIAN ),
    X86     ( 32, ByteOrder.LITTLE_ENDIAN ),
    X86_64  ( 64, ByteOrder.LITTLE_ENDIAN ),
    PPC     ( 32, ByteOrder.BIG_ENDIAN ),
    PPC64   ( 64, ByteOrder.BIG_ENDIAN ),
    OTHER   (  0, ByteOrder.BIG_ENDIAN );
    

    public static Architecture local() {
        return LOCAL_ARCH;
    }
    
    public int bits() {
        return mBits;
    }
    
    public ByteOrder byteOrder() {
        return mByteOrder;
    }
    
    
    
    private int mBits;
    private ByteOrder mByteOrder;


    Architecture( int bits, ByteOrder byteOrder ) {
        mBits = bits;
        mByteOrder = byteOrder;
    }

    
    private static final Architecture LOCAL_ARCH;


    static {
        String osArch = System.getProperty( "os.arch" );
        
        if( osArch == null ) {
            LOCAL_ARCH = OTHER;
        } else if( osArch.equals( "x86_64" ) ) {
            LOCAL_ARCH = X86_64;
        } else if( osArch.equals( "x86" ) ) {
            LOCAL_ARCH = X86;
        } else if( osArch.equals( "i386" ) ) {
            LOCAL_ARCH = I386;
        } else if( osArch.equals( "ppc" ) ) {
            LOCAL_ARCH = PPC;
        } else if( osArch.equals( "ppc64" ) ) {
            LOCAL_ARCH = PPC64;
        } else {
            LOCAL_ARCH = OTHER;
        }

    }

    
    @Deprecated public static Architecture localArch() {
        return LOCAL_ARCH;
    }
    
}
