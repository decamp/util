/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.util.reflect;


import java.lang.reflect.*;
    

public class Methods {

    private static final Object[] NO_ARGS    = {};
    private static final Class<?>[] NO_TYPES = {};
    
    
    /**
     * Finds method that may be invoked with the provided parameter types using
     * {@code Methods.invoke()}. 
     * 
     * @param target       Type of object that owns method.
     * @param methodName   Name of method to find.
     * @param params       Types of parameters to be passed to method.
     * @return best method found, or {@code null} if no invokable method found.
     */
    public static Method match( Class<?> target, String methodName, Class<?>... params ) {
        return findBestMethod( target, methodName, cleanParams( params ), false );
    }
    
    /**
     * Finsd static method that may be invoked with the provided parameter types.
     * 
     * @param target       Type of object that owns method.
     * @param methodName   Name of method to find.
     * @param params       Types of parameters to be passed to method.
     * @return best static method found, or {@code null} if no invokable method found.
     */
    public static Method matchStatic( Class<?> target, String methodName, Class<?>... params ) {
        return findBestMethod( target, methodName, cleanParams( params ), true );
    }
    
    /**
     * Finds method that may be invoked with the precise args provided.
     * 
     * @param target       Object that owns method.
     * @param methodName   Name of method to find.
     * @param args         Arguments to be passed to method.
     * @return best method found, or {@code null} if no invokable method found.
     */    
    public static Method matchArgs( Object target, String methodName, Object... args ) {
        return findBestMethod( target.getClass(), methodName, argTypes( args ), false );
    }
    
    /**
     * Finds static method that may be invoked with the precise args provided.
     * 
     * @param target       Object that owns method.
     * @param methodName   Name of method to find.
     * @param args         Arguments to be passed to method.
     * @return best method found, or {@code null} if no invokable method found.
     */
    public static Method matchStaticArgs( Class<?> target, String methodName, Object... args ) {
        return findBestMethod( target, methodName, argTypes( args ), true );
    }
    
    /**
     * Finds constructor that may be invoked with the provided parameter types.
     * 
     * @param target       Type of object that owns method.
     * @param params       Types of parameters to be passed to method.
     * @return best method found, or {@code null} if no invokable method found.
     */
    public static <T> Constructor<T> matchConstructorParams( Class<T> target, Class<?>... params ) {
        return findBestConstructor( target, cleanParams( params ) );
    }
    
    /**
     * Finds constructor that may be invoked with the precise arguments provided.
     * 
     * @param target       Type of object that owns method.
     * @param args         Arguments to be provided to constructor.
     * @return best method found, or {@code null} if no invokable method found.
     */
    public static Constructor<?> matchConstructorArgs( Class<?> target, Object... args ) {
        return findBestConstructor( target, argTypes( args ) );
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
    public static Object invoke( 
            Object target, 
            String methodName, 
            Object... args 
    ) 
            throws
            NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException 
    {
        args = cleanArgs( args );
        Method m = findBestMethod( target.getClass(), methodName, argTypes( args ), false );
        if( m == null ) {
            throw new NoSuchMethodException();
        }
        try {
            return m.invoke( target, args );
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
    public static Object invokeStatic( 
            Class<?> target,
            String methodName,
            Object... args 
    )
            throws
            NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException
    {
        args = cleanArgs( args );
        Method m = findBestMethod( target, methodName, argTypes( args ), true );
        if( m == null ) {
            throw new NoSuchMethodException();
        }
        
        try {
            return m.invoke( null, args );
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
    public static <T> T construct( 
            Class<T> type, 
            Object... args 
    )
            throws 
            NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException,
            InstantiationException
    {
        args = cleanArgs( args );
        Constructor<T> con = findBestConstructor( type, argTypes( args ) );
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
    
    /**
     * Computes cost of invoking a method with the specified parameters. This method
     * evaluates only the parameter mapping and does not account for scope.
     * 
     * @param method    Method to invoke.
     * @param argTypes  Arguments provided to method
     * @return associated cost with this cass, or Float.POSITIVE_INFINITY if invalid.
     */
    public static float invocationCost( Method method, Class<?>... argTypes ) {
        return invocationCost( method.getParameterTypes(), argTypes );
    }
    
    /**
     * Computes cost of invoking a method with the specified parameters. This method
     * evaluates only the parameter mapping and does not account for scope.
     * 
     * @param params     Parameter types for method.
     * @param args       Argemntst to be provided.
     * @return associated cost of this invocation.
     */
    public static float invocationCost( Class<?>[] params, Class<?>[] args ) {
        if( args.length != params.length ) {
            return Float.POSITIVE_INFINITY;
        }

        float cost = 0f;
        for( int i = 0; i < args.length; i++ ) {
            cost += Types.assignmentCost( args[i], params[i] );
            if( Float.isInfinite( cost ) ) {
                return cost;
            }
        }
        return cost;
    }
    
    /**
     * Converts array of arguments to array of classes for those args.
     * {@code null} objects are mapped to {@code void.class}.
     */
    public static Class<?>[] argTypes( Object[] args ) {
        if( args == null || args.length == 0 ) {
            return NO_TYPES;
        }
        
        Class<?>[] ret = new Class<?>[ args.length ];
        for( int i = 0; i < args.length; i++ ) {
            ret[i] = args[i] == null ? void.class : args[i].getClass();
        }
        
        return ret;
    }
    
    
    
    static Class<?>[] cleanParams( Class<?>[] args ) {
        return args == null ? NO_TYPES : args;
    }
    
    
    static Object[] cleanArgs( Object... args ) {
        return args == null ? NO_ARGS : args;
    }
    
    
    static Method findBestMethod( Class<?> target, String methodName, Class<?>[] argTypes, boolean staticOnly ) {
        try {
            Method m = target.getMethod( methodName, argTypes );
            return makeAccessible( m ) ? m : null;
        } catch( NoSuchMethodException ex ) {}
        
        // Find method that has lowest invocation cost.
        Method[] methods   = target.getMethods();
        Method bestMethod  = null;
        float bestCost     = Float.POSITIVE_INFINITY;
        
        for( int i = 0; i < methods.length; i++ ) {
            Method m = methods[i];
            if( !methodName.equals( m.getName() ) ){
                continue;
            }
            
            if( staticOnly && !Modifier.isStatic( m.getModifiers() ) ) {
                continue;
            }
            
            Class<?>[] params = m.getParameterTypes();
            float cost = invocationCost( params, argTypes );
            if( Float.isInfinite( cost ) ) {
                continue;
            }
            
            if( cost < bestCost ) {
                bestMethod = m;
                bestCost   = cost;
            }
        }
        
        return makeAccessible( bestMethod ) ? bestMethod : null;
    }
    
    
    @SuppressWarnings( "unchecked" )
    static <T> Constructor<T> findBestConstructor( Class<T> target, Class<?>[] argTypes ) {
        try {
            Constructor<T> con = target.getConstructor( argTypes );
            return makeAccessible( con ) ? con : null;
        } catch( NoSuchMethodException ex ) {}
        
        // Find constructor that has lowest invocation cost.
        Constructor<?>[] cons   = target.getConstructors();
        Constructor<?> bestCon  = null;
        float bestCost          = Float.POSITIVE_INFINITY;
        
        for( int i = 0; i < cons.length; i++ ) {
            Constructor<?> con = cons[i];
            
            Class<?>[] params = con.getParameterTypes();
            float cost = invocationCost( params, argTypes );
            if( Float.isInfinite( cost ) ) {
                continue;
            }
            
            if( cost < bestCost ) {
                bestCon  = con;
                bestCost = cost;
            }
        }
        
        return makeAccessible( bestCon ) ? (Constructor<T>)bestCon : null;
    }

    
    private static boolean makeAccessible( AccessibleObject obj ) {
        if( obj == null ) {
            return false;
        }
        
        try {
            obj.setAccessible( true );
            return true;
        } catch( SecurityException ex ) {
            return false;
        }
    }

}        

