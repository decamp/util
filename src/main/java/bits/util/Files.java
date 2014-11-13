/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */
package bits.util;

import java.io.*;
import java.nio.channels.FileChannel;


/**
 * @author Philip DeCamp
 */
public final class Files {

    public static String baseName( File file ) {
        return baseName( file.getName() );
    }


    public static String baseName( String name ) {
        int idx = name.lastIndexOf( "." );
        return idx < 0 ? name : name.substring( 0, idx );
    }


    public static File withBaseName( File file, String newBaseName ) {
        File dir = file.getParentFile();
        String suffix = suffix( file );
        return new File( dir, newBaseName + "." + suffix );
    }


    public static String suffix( File file ) {
        return suffix( file.getName() );
    }
    
    
    public static String suffix( String name ) {
        int idx = name.lastIndexOf( "." );
        return idx <= 0 ? "" : name.substring(idx + 1).toLowerCase().intern();
    }


    public static File withSuffix( File file, String newSuffix ) {
        File parentFile = file.getParentFile();
        String name = baseName( file );
        return new File( parentFile, name + "." + newSuffix );
    }

    /**
     * Determines if a path is relative or absolute. If absolute,
     * it's returned as is. If relative, it's combined with the
     * provided base directory.
     */
    public static File resolve( File base, String path ) {
        if( path == null ) {
            return null;
        }
        
        StringBuilder s = new StringBuilder();
        if( base != null && !path.startsWith( File.separator ) ) {
            appendPath( base.getPath(), s );
        }
        
        appendPath( path, s );
        return new File( s.toString() );
    }
    
    /**
     * Computes the relative path between <i>file</i> and <i>oldBase</i> and
     * adds that path to <i>newBase</i>.
     */
    public static File rebase( File file, File oldBase, File newBase ) {
        if( file == null || file.equals( oldBase ) ) {
            return newBase;
        }
        return new File( rebase( file.getParentFile(), oldBase, newBase ), file.getName() );
    }


    public static void copy( File source, File dest ) throws IOException {
        if( dest.exists() ) {
            throw new IOException( dest.getPath() + " exists." );
        }
        FileChannel s = new FileInputStream( source ).getChannel();
        FileChannel d = new FileOutputStream( dest ).getChannel();
        d.transferFrom( s, 0, s.size() );
        s.close();
        d.close();
    }
    

    public static void move( File in, File out ) throws IOException {
        if( !in.renameTo( out ) ) {
            throw new IOException( "Failed to move " + in.getPath() + " to " + out.getPath() );
        }
    }
    
    
    public static FileFilter suffixFilter( String suffix ) {
        return suffixFilter( suffix, true, true );
    }

    
    public static FileFilter suffixFilter( String suffix, 
                                           final boolean acceptDir, 
                                           final boolean acceptHidden ) 
    {
        final String s = suffix.toLowerCase().intern();
        
        return new FileFilter() {
            public boolean accept( File file ) {
                if( suffix( file ) != s )
                    return false;
                
                if( !acceptDir && file.isDirectory() )
                    return false;
                
                if( !acceptHidden && file.isHidden() )
                    return false;
                
                return true;
            }
        };
    }


    
    private static void appendPath( String path, StringBuilder out ) {
        int i = 0;
        while( i < path.length() ) {
            int j = path.indexOf( File.separatorChar, i );
            switch( j ) {
            case -1:
                appendPathPart( path, i, path.length() - i, out );
                return;
            case 0:
                appendPathPart( path, 0, 1, out );
                break;
            default:
                appendPathPart( path, i, j - i, out );
            }
            i = j + 1;
        }
    }
    
    
    private static void appendPathPart( String name, int off, int len, StringBuilder out ) {
        switch( len ) {
        case 0:
            return;
            
        case 1:
            if( name.charAt( off ) == '.' ) {
                return;
            } else if( name.charAt( off ) == File.separatorChar ) {
                if( out.length() == 0 ) {
                    out.append( File.separatorChar );
                }
                return;
            }
            break;
                        
        case 2:
            if( name.charAt( off ) == '.' && name.charAt( off + 1 ) == '.' ) {
                int idx = out.lastIndexOf( File.separator );
                
                if( idx == 0 ) {
                    // Reached start of absolute path. Drop the ".." because it is unnecessary.
                    out.setLength( 1 );
                    return;
                }
                
                if( out.length() == 0 ) {
                    // Nothing in output path yet.
                    out.append( ".." );
                    return;
                }
                
                // Check if no previous name is ".." or undefined.
                if( out.length() - idx == 3 && out.charAt( idx + 1 ) == '.' && out.charAt( idx + 2 ) == '.' ) {
                    // Previous name is also "..", so we can append another.
                    out.append( File.separatorChar );
                    out.append( ".." );
                    return;
                }
                
                // Erase previous name and potential trailing slash.
                out.setLength( idx < 0 ? 0 : idx );
                return;
            }
            
            break;
        }
        
        if( out.length() > 0 && out.charAt( out.length() - 1 ) != File.separatorChar ) {
            out.append( File.separatorChar );
        }
        
        out.append( name, off, off + len );
    }
    
    
    private Files() {}



    @Deprecated public static File setSuffix( File file, String newSuffix ) {
        return withSuffix( file, newSuffix );
    }


    @Deprecated public static File setBaseName( File file, String newBaseName ) {
        return withBaseName( file, newBaseName );
    }


}
