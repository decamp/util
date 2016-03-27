/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.util.platform;

/**
 * Enum for OS families.
 * 
 * @author Philip DeCamp
 */
public enum OperatingSystem {

    OSX,
    LINUX,
    WINDOWS,
    OTHER;


    public static OperatingSystem local() {
        return LOCAL_OS;
    }


    private static final OperatingSystem LOCAL_OS;

    static {
        String osName = System.getProperty( "os.name" );
        if( osName == null )
            osName = "";

        if( osName.startsWith( "Mac OS X" ) ) {
            LOCAL_OS = OperatingSystem.OSX;
        } else if( osName.startsWith( "Linux" ) ) {
            LOCAL_OS = OperatingSystem.LINUX;
        } else if( osName.startsWith( "Windows" ) ) {
            LOCAL_OS = OperatingSystem.WINDOWS;
        } else {
            LOCAL_OS = OperatingSystem.OTHER;
        }
    }

}
