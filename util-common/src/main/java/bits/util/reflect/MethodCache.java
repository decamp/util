/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.util.reflect;

import java.lang.reflect.*;
import java.util.*;


/**
 * Like Methods, but caches method and constructor matches for 
 * better speed. Cache currently uses a WeakHashMap, but this
 * may be changed.
 * <p>
 * Preliminary speed tests suggest using MethodCache is about twice 
 * as fast as calling Methods directly.
 * 
 * @author Philip DeCamp
 */
public class MethodCache {
    
    private static final String CONSTRUCTOR_NAME = "<init>";
    private static MethodCache sInstance = null;
    
    
    public static synchronized MethodCache getInstance() {
        if( sInstance != null ) {
            return sInstance;
        }
        sInstance = create();
        return sInstance;
    }
    
    
    public static MethodCache create() {
        return new MethodCache();
    }
    
    
    
    private final Map<MethodKey,Object> mMap;
    
    private MethodCache() {
        mMap = new WeakHashMap<>();
    }
    
    
    /**
     * Finds method that may be invoked with the provided parameter types using
     * {@code Methods.invoke()}. 
     * 
     * @param target       Type of object that owns method.
     * @param methodName   Name of method to find.
     * @param params       Types of parameters to be passed to method.
     * @return best method found, or {@code null} if no invokable method found.
     */
    public Method match( Class<?> target, String methodName, Class<?>... params ) {
        return findBestMethod(
                target,
                methodName,
                Methods.cleanParams( params ),
                false,
                false
        );
    }
    
    /**
     * Finsd static method that may be invoked with the provided parameter types.
     * 
     * @param target       Type of object that owns method.
     * @param methodName   Name of method to find.
     * @param params       Types of parameters to be passed to method.
     * @return best static method found, or {@code null} if no invokable method found.
     */
    public Method matchStatic( Class<?> target, String methodName, Class<?>... params ) {
        return findBestMethod(
                target,
                methodName,
                Methods.cleanParams( params ),
                true,
                false
        );
    }
    
    /**
     * Finds method that may be invoked with the precise args provided.
     * 
     * @param target      Object that owns method.
     * @param methodName  Name of method to find.
     * @param args        Arguments to be passed to method.
     * @return best method found, or {@code null} if no invokable method found.
     */    
    public Method matchArgs( Object target, String methodName, Object... args ) {
        return findBestMethod(
                target.getClass(),
                methodName,
                Methods.argTypes( args ),
                false,
                true
        );
    }
    
    /**
     * Finds static method that may be invoked with the precise args provided.
     * 
     * @param target      Object that owns method.
     * @param methodName  Name of method to find.
     * @param args        Arguments to be passed to method.
     * @return best method found, or {@code null} if no invokable method found.
     */
    public Method matchStaticArgs( Class<?> target, String methodName, Object... args ) {
        return findBestMethod(
                target,
                methodName,
                Methods.argTypes( args ),
                true,
                true
        );
    }
    
    /**
     * Finds constructor that may be invoked with the provided parameter types.
     * 
     * @param target       Type of object that owns method.
     * @param params       Types of parameters to be passed to method.
     * @return best method found, or {@code null} if no invokable method found.
     */
    public <T> Constructor<T> matchConstructorParams( Class<T> target, Class<?>... params ) {
        return findBestConstructor(
                target,
                Methods.cleanParams( params ),
                false
        );
    }
    
    /**
     * Finds constructor that may be invoked with the precise arguments provided.
     * 
     * @param target  Type of object that owns method.
     * @param args    Types of parameters to be passed to method.
     * @return best method found, or {@code null} if no invokable method found.
     */
    public Constructor<?> matchConstructorArgs( Class<?> target, Object... args ) {
        return findBestConstructor(
                target,
                Methods.argTypes( args ),
                true
        );
    }
    
    /**
     * Finds method that may be invoked using the arguments provided and 
     * invokes that method.
     * 
     * @param target        Object on which to operate.
     * @param methodName    Name of method to call.
     * @param args          Arguments to provide to method.
     * @return              Whatever the method returns. 
     * 
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public Object invoke(
            Object target,
            String methodName,
            Object... args
    )
            throws
            NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException
    {
        args = Methods.cleanArgs( args );
        Method method = findBestMethod(
                target.getClass(),
                methodName,
                Methods.argTypes( args ),
                false,
                true
        );
        
        if( method == null ) {
            throw new NoSuchMethodException();
        }
        
        try {
            return method.invoke( target, args );
        } catch( IllegalArgumentException ex ) {
            // This indicates in internal error with find,
            // so just wrap it as a NoSuchMethodException.
            NoSuchMethodException e = new NoSuchMethodException( "Internal method matching error." );
            e.initCause( ex );
            throw e;
        }
                                                
    }
    
    /**
     * Finds static method that may be invoked using the arguments provided and 
     * invokes that method.
     * 
     * @param target        Object on which to operate.
     * @param methodName    Name of method to call.
     * @param args          Arguments to provide to method.
     * @return              Whatever the method returns. 
     * 
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public Object invokeStatic(
            Class<?> target,
            String methodName,
            Object... args
    )
            throws
            NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException
    {
        args = Methods.cleanArgs( args );
        Method method = findBestMethod(
                target,
                methodName,
                Methods.argTypes( args ),
                true,
                true
        );
                
        if( method == null ) {
            throw new NoSuchMethodException();
        }
        
        try {
            return method.invoke( null, args );
        } catch( IllegalArgumentException ex ) {
            // This indicates in internal error with find,
            // so just wrap it as a NoSuchMethodException.
            NoSuchMethodException e = new NoSuchMethodException( "Internal method matching error." );
            e.initCause( ex );
            throw e;
        }
    }
    
    /**
     * Finds constructor that may be invoked using the arguments provided and
     * creates new object using that constructor. 
     * 
     * @param type   Type of object to create.
     * @param args   Arguments to provide to constructor.
     * @return       New instance of type {@code target}.
     * 
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    public <T> T construct( 
            Class<T> type, 
            Object... args 
    )
            throws 
            NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException,
            InstantiationException
                                   
    {
        args = Methods.cleanArgs( args );
        Constructor<T> con = findBestConstructor( type, 
                                                  Methods.argTypes( args ),
                                                  true );
        if( con == null ) {
            throw new NoSuchMethodException();
        }
        
        try {
            return con.newInstance( args );
        } catch( IllegalArgumentException ex ) {
            // This indicates in internal error with find,
            // so just wrap it as a NoSuchMethodException.
            NoSuchMethodException e = new NoSuchMethodException( "Internal method matching error." );
            e.initCause( ex );
            throw e;
        }
    }
    
    
    
    private synchronized Method findBestMethod(
            Class<?> target,
            String methodName,
            Class<?>[] argTypes,
            boolean staticOnly,
            boolean typeArrayIsSafe
    ) {
        MethodKey key = new MethodKey( target, methodName, argTypes );
        Method method = (Method)mMap.get( key );
        if( method != null ) {
            return method;
        }
                
        method = Methods.findBestMethod( target, methodName, argTypes, staticOnly );
        if( method != null ) {
            // Make sure key holds safe copy of arg types.
            if( !typeArrayIsSafe && argTypes.length > 0 ) {
                key.mParams = argTypes.clone();
            }
            mMap.put( key, method );
        }
        
        return method;
    }
    
    
    @SuppressWarnings( "unchecked" )
    private synchronized <T> Constructor<T> findBestConstructor(
            Class<T> target,
            Class<?>[] argTypes,
            boolean typeArrayIsSafe
    ) {
        MethodKey key = new MethodKey( target, CONSTRUCTOR_NAME, argTypes );
        Constructor<T> cons = (Constructor<T>)mMap.get( key );
        if( cons != null ) {
            return cons;
        }
        
        cons = Methods.findBestConstructor( target, argTypes );
        if( cons != null ) {
            if( !typeArrayIsSafe && argTypes.length > 0 ) {
                key.mParams = argTypes.clone();
            }
            mMap.put( key, cons );
        }
        
        return cons;
    }
    
    
    
    private static final class MethodKey {
        
        final Class<?> mClass;
        final String mName;
        Class<?>[] mParams;
        final int mHash;
        
        public MethodKey( Class<?> clazz, String name, Class<?>[] paramsRef ) {
            mClass  = clazz;
            mName   = name.intern();
            mParams = paramsRef;
            mHash   = clazz.hashCode() + name.length();
        }
        
        
        @Override
        public int hashCode() {
            return mHash;
        }
        
        @Override
        public boolean equals( Object obj ) {
            if( !( obj instanceof MethodKey ) ) {
                return false;
            }
            
            MethodKey m = (MethodKey)obj;
            if( mClass != m.mClass ||
                mName  != m.mName )
            {
                return false;
            }
            
            return Arrays.equals( mParams, m.mParams );
        }
        
    }

}
