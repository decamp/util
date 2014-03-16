/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.util.reflect;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;
import java.util.logging.Logger;
import java.util.regex.*;


public class ClassFinder {
    
    private static final Pattern CLASS_PAT = Pattern.compile( "^[a-z]++[a-z0-9_]*+$", Pattern.CASE_INSENSITIVE );
    private static final Logger sLog = Logger.getLogger( ClassFinder.class.getName() );
    
    
    /**
     * Finds all the resources spanned by the provided packageName. Resources
     * are returned as URLs and may represent directories or jar files. 
     * <p>
     * Resources are found using the calling thread's context class loader:<br/>
     * <code>Thread.currentThread().getContextClassLoader()</code>
     * 
     * @param packageName A package to check for resources.
     * @return a list of all resources found.
     * @throws ClassNotFoundException if provided package could not be found or any other error occurs.
     */
    public static List<URL> findPackageResources( String packageName ) throws ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if( classLoader == null ) {
            throw new ClassNotFoundException( "Failed to retrieve context ClassLoader." );
        }
        return findPackageResources( packageName, classLoader );
    }
    
    /**
     * Finds all the resources spanned by the provided packageName. Resources
     * are returned as URLs and may represent directories or jar files. 
     * 
     * @param packageName A package to check for resources.
     * @param classLoader The ClassLoader to use to search for resources.
     * @return a list of all resources found.
     * @throws ClassNotFoundException if provided package could not be found or any other error occurs.
     */
    public static List<URL> findPackageResources( String packageName, ClassLoader classLoader ) throws ClassNotFoundException {
        String packagePath = packageName.replace( '.', '/' );
        Enumeration<URL> resourceList;

        try {
            resourceList = classLoader.getResources( packagePath );
        } catch( IOException ex ) {
            throw new ClassNotFoundException( packageName + " could not be found.", ex );
        }

        List<URL> outList = new ArrayList<URL>();
        while( resourceList.hasMoreElements() ) {
            outList.add( resourceList.nextElement() );
        }

        return outList;
    }

    /**
     * Finds all the roots of resources spanned by the packageName. The 
     * <i>resource root</i> means that the resource path is trimmed to 
     * encapsulate the <i>default package</i> instead of the requested 
     * package.
     * <p>
     * Resources are found using the calling thread's context class loader: <br/>
     * <code>Thread.currentThread().getClassLoader()</code>
     * 
     * @param packageName A package to check for resources.
     * @return a list of all resources found, trimmed to represent the default package.
     * @throws ClassNotFoundException if the provided package could not be found or any other error occurs.
     */
    public static List<URL> findPackageResourceRoots( String packageName ) throws ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if( classLoader == null ) {
            throw new ClassNotFoundException( "Failed to retrieve context ClassLoader." );
        }

        return findPackageResourceRoots( packageName, classLoader );
    }
    
    /**
     * Finds all the roots of resources spanned by the packageName. The 
     * <i>resource root</i> means that the resource path is trimmed to 
     * encapsulate the <i>default package</i> instead of the requested 
     * package.
     * <p>
     * Resources are found using the calling thread's context class loader: <br/>
     * <code>Thread.currentThread().getClassLoader()</code>
     * 
     * @param packageName A package to check for resources.
     * @param classLoader The ClassLoader to use to search for resources.
     * @return a list of all resources found, trimmed to represent the default package.
     * @throws ClassNotFoundException if the provided package could not be found or any other error occurs.
     */
    public static List<URL> findPackageResourceRoots( String packageName, ClassLoader classLoader ) throws ClassNotFoundException {
        List<URL> inList  = findPackageResources( packageName, classLoader );
        List<URL> outList = new ArrayList<URL>();

        for( URL url : inList ) {
            if( url.getProtocol().equalsIgnoreCase( "file" ) ) {
                File file;

                try {
                    file = new File( url.toURI() );
                } catch( URISyntaxException ex ) {
                    throw new ClassNotFoundException( "Could not create file from URL.", ex );
                }

                while( packageName != null && packageName.length() > 0 ) {
                    file = file.getParentFile();
                    if( file == null )
                        throw new ClassNotFoundException( "Could not traverse source tree for resource " + url.toString() );

                    int index = packageName.indexOf( "." );
                    if( index < 0 ) {
                        packageName = null;
                    } else {
                        packageName = packageName.substring( index + 1 );
                    }
                }

                try {
                    outList.add( new URL( "file:" + file.getPath() ) );
                } catch( MalformedURLException ex ) {
                    throw new ClassNotFoundException( "Could not create URL for file:" + file.getPath(), ex );
                }

            } else if( url.getProtocol().equalsIgnoreCase( "jar" ) ) {
                String path = url.toString();
                int i = path.lastIndexOf( "!" );

                if( i < 0 ) {
                    outList.add( url );
                } else {
                    path = path.substring( 0, i + 1 ) + "/";

                    try {
                        outList.add( new URL( path ) );
                    } catch( MalformedURLException ex ) {
                        throw new ClassNotFoundException( "Could not create URL for " + path, ex );
                    }
                }

            } else {
                throw new ClassNotFoundException( "Could not recognize protocol for resource " + url.toString() );
            }
        }

        return outList;
    }

    /**
     * Finds all the classes in a specified package.  This method scans both 
     * directories and/or jar files as necessary.
     * <p>
     * Classes are not initialized when loaded.  Loading is performed by the 
     * calling thread's context class loader:<br/>
     * <code>Thread.currentThread().getContextClassLoader()</code>
     * 
     * @param packageName The name of the package.
     * @param recurse Specifies whether class from subpackages should be included.
     * @return the list of classes in specified package.
     * @throws ClassNotFoundException if the package is not found or any other error occurs.
     */
    public static List<Class<?>> findClasses( String packageName, boolean recurse ) throws ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return findClasses( packageName, recurse, classLoader );
    }
    
    /**
     * Finds all the classes in a specified package.  This method scans both 
     * directories and/or jar files as necessary.  This method does not 
     * initialize the classes it loads.
     * <p>
     * Classes are not initialized when loaded.
     * 
     * @param packageName The name of the package.
     * @param recurse Specifies whether class from subpackages should be included.
     * @param classLoader The ClassLoader to use to search for and load classes.
     * @return the list of classes in specified package.
     * @throws ClassNotFoundException if the package is not found or any other error occurs.
     */
    public static List<Class<?>> findClasses( String packageName, boolean recurse, ClassLoader classLoader ) throws ClassNotFoundException {
        List<URL> list = findPackageResources( packageName, classLoader );
        List<Class<?>> outList = new ArrayList<Class<?>>();

        for( URL url : list ) {
            try {
                if( url.getProtocol().equalsIgnoreCase( "file" ) ) {
                    searchDirForClasses( classLoader, packageName, new File( url.toURI() ), recurse, outList );

                } else if( url.getProtocol().equalsIgnoreCase( "jar" ) ) {
                    File file = getJarResourceFile( url );
                    searchJarForClasses( classLoader, packageName, file, recurse, outList );

                } else {
                    throw new IOException( "Unknown protocol: " + url.getProtocol() );
                }

            } catch( URISyntaxException ex ) {
                throw new ClassNotFoundException( "Failed to search package resource " + url, ex );

            } catch( IOException ex ) {
                throw new ClassNotFoundException( "Failed to search package resource " + url, ex );

            } catch( NullPointerException ex ) {
                throw new ClassNotFoundException( "Failed to search package resource " + url, ex );
            }
        }

        return outList;
    }

    /**
     * Finds all the classes in a resource.
     * <p>
     * Classes are not initialized when loaded.  Loading is performed by the 
     * calling thread's context class loader:<br/>
     * <code>Thread.currentThread().getContextClassLoader()</code>
     * 
     * @param url URL of resource to scan.  URL must represent a local directory or jar file.
     * @return the list of classes in specified package.
     * @throws ClassNotFoundException if the package is not found or any other error occurs.  
     */
    public static List<Class<?>> findClasses( URL resource ) throws ClassNotFoundException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        return findClasses( resource, loader );
    }
    
    /**
     * Finds all the classes in a resource.
     * <p>
     * Classes are not initialized when loaded.  Loading is performed by the 
     * calling thread's context class loader:<br/>
     * <code>Thread.currentThread().getContextClassLoader()</code>
     * 
     * @param url URL of resource to scan.  URL must represent a local directory or jar file.
     * @param classLoader The ClassLoader to use to search for and load classes.
     * @return the list of classes in specified package.
     * @throws ClassNotFoundException if the package is not found or any other error occurs.  
     */
    public static List<Class<?>> findClasses( URL resource, ClassLoader classLoader ) throws ClassNotFoundException {
        List<Class<?>> outList = new ArrayList<Class<?>>();

        try {
            if( resource.getProtocol().equalsIgnoreCase( "file" ) ) {
                searchDirForClasses( classLoader, "", new File( resource.toURI() ), true, outList );

            } else if( resource.getProtocol().equalsIgnoreCase( "jar" ) ) {
                File file = getJarResourceFile( resource );
                searchJarForClasses( classLoader, "", file, true, outList );

            } else {
                throw new IOException( "Unknown protocol: " + resource.getProtocol() );
            }

        } catch( URISyntaxException ex ) {
            throw new ClassNotFoundException( "Failed to search package resource " + resource, ex );

        } catch( IOException ex ) {
            throw new ClassNotFoundException( "Failed to search package resource " + resource, ex );

        } catch( NullPointerException ex ) {
            throw new ClassNotFoundException( "Failed to search package resource " + resource, ex );
        }

        return outList;
    }    

   

    private static void searchDirForClasses( ClassLoader loader, 
                                             String packageName, 
                                             File dir, 
                                             boolean recurse, 
                                             List<Class<?>> outList ) 
                                             throws URISyntaxException 
    {
        File[] files = dir.listFiles();
        if( files == null )
            return;

        for( File f : files ) {
            if( f.isFile() && !f.isHidden() ) {
                String fileName = f.getName();
                if( !fileName.toLowerCase().endsWith( ".class" ) )
                    continue;

                String className = packageName + "." + fileName.substring( 0, fileName.length() - 6 );

                try {
                    outList.add( Class.forName( className, false, loader ) );
                } catch( NoClassDefFoundError ex ) {
                    sLog.warning( "Failed to load class: " + className );
                } catch( ClassNotFoundException ex ) {
                    sLog.warning( "Failed to load class: " + className );
                }
            }
        }

        if( recurse ) {
            for( File f : files ) {
                if( f.isDirectory() && !f.isHidden() ) {
                    Matcher m = CLASS_PAT.matcher( f.getName() );
                    if( !m.matches() ) {
                        continue;
                    }
                    
                    String p = packageName;
                    if( p.length() > 0 ) {
                        p += ".";
                    }
                    
                    p += f.getName();
                    searchDirForClasses( loader, p, f, true, outList );
                }
            }
        }
    }


    private static void searchJarForClasses( ClassLoader loader,
                                             String packageName,
                                             File file,
                                             boolean recurse,
                                             List<Class<?>> outList )
                                             throws IOException
    {
        String packagePath = packageName.replace( ".", File.separator );
        JarFile jar = new JarFile( file );
        Enumeration<JarEntry> entries = jar.entries();

        while( entries.hasMoreElements() ) {
            String className = entries.nextElement().getName();

            if( !className.startsWith( packagePath ) || !className.toLowerCase().endsWith( ".class" ) ) {
                continue;
            }

            if( !recurse && className.lastIndexOf( "/" ) > packagePath.length() ) {
                continue;
            }
            
            className = className.substring( 0, className.length() - 6 ).replace( File.separator, "." );

            try {
                outList.add( Class.forName( className, false, loader ) );
            } catch( NoClassDefFoundError ex ) {
                sLog.warning( "Failed to load class: " + className );
            } catch( ClassNotFoundException ex ) {
                sLog.warning( "Failed to load class: " + className );
            }
        }
    }
    
    
    private static File getJarResourceFile( URL url ) throws IOException {
        String jarPath = url.getPath();
        if( !jarPath.startsWith( "file:" ) ) {
            throw new IOException( "Could not parse jar URL: " + url.toString() );
        }

        jarPath = jarPath.substring( 5 );
        int index = jarPath.indexOf( "!" );
        if( index >= 0 ) {
            jarPath = jarPath.substring( 0, index );
        }

        File file = new File( jarPath );
        if( !file.exists() ) {
            throw new FileNotFoundException( "Could not parse jar URL: " + url.toString() );
        }

        return file;
    }

}
