/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.util.reflect;

import org.junit.Test;

import bits.util.reflect.*;
import static org.junit.Assert.*;

@SuppressWarnings( "all" )
public class TestMethodCache {
    
    final MethodCache mCache = MethodCache.getInstance();
    
    
    @Test
    public void testCons() throws Exception {
        Foo1 a = mCache.construct( Foo1.class );
        Foo2 b = null;
        
        try {
            b = mCache.construct( Foo2.class );
        } catch( Exception ex ) {}
        
        assertTrue( b == null );
        
        b = mCache.construct( Foo2.class, "nom" );
        assertEquals( mCache.invoke( b, "name" ), "nom" );
    }
    
    
    @Test
    public void testInvoke() throws Exception {
        Foo1 a = new Foo1();
        Foo2 b = new Foo2( "nom" );
        
        String r = null;
        try {
            r = (String)mCache.invoke( a, "bar" );
        } catch( Exception ex ) {}
        
        assertTrue( r == null );
        assertEquals( mCache.invoke( a, "bar", 4 ), "Foo1.bar Integer" );
        
        Integer[] nums = { 3, 4 };
        assertEquals( mCache.invoke( a, "bar", new Object[]{ nums } ), "Foo1.bar Integer..." );
        
        assertEquals( mCache.invoke( a, "bar", a ), "Foo1.bar Foo1" );
        assertEquals( mCache.invoke( a, "bar", b ), "Foo1.bar Foo1" );
        assertEquals( mCache.invoke( b, "barBar", a ), "Foo2.barBar Foo1" );
        assertEquals( mCache.invoke( b, "barBar", b ), "Foo2.barBar Foo2" );
        assertEquals( mCache.invoke( b, "bar", b, a, a ), "Foo2.bar Foo1 Foo1 Foo1" );
        assertEquals( mCache.invoke( b, "bar", b, b, a ), "Foo2.bar Foo1 Foo2 Foo1" );
        assertEquals( mCache.invoke( b, "bar", b, a, b ), "Foo2.bar Foo1 Foo1 Foo2" );
    }
    
    
    @Test
    public void testStatic() throws Exception {
        Foo1 a = new Foo1();
        Foo2 b = new Foo2( "nom" );
        
        assertEquals( mCache.invoke( b, "bar" ), "Foo2.bar" );
        assertEquals( mCache.invoke( a, "staticBar" ), "static Foo1.staticBar" );
        assertEquals( mCache.invoke( b, "staticBar" ), "static Foo2.staticBar" );
        assertEquals( mCache.invokeStatic( Foo1.class, "staticBar" ), "static Foo1.staticBar" );
        assertEquals( mCache.invokeStatic( Foo2.class, "staticBar" ), "static Foo2.staticBar" );
    }
 
    
    @Test
    public void testSpeed() throws Exception {
        
        long cachedNanos   = 0;
        long uncachedNanos = 0;
        
        Foo1 foo = new Foo1();
        
        for( int trial = 0; trial < 5000; trial++ ) {
            
            long t = System.nanoTime();
            for( int i = 0; i < 1000; i++ ) {
                Methods.invoke( foo, "bar", 3 );
            }
            uncachedNanos += System.nanoTime() - t;
            
            t = System.nanoTime();
            for( int i = 0; i < 1000; i++ ) {
                mCache.invoke( foo, "bar", 3 );
            }
            
            cachedNanos += System.nanoTime() - t;
        }
        
        System.out.println( "Uncached: " + ( uncachedNanos / 1000000000.0 ) );
        System.out.println( "Cached: " + ( cachedNanos / 1000000000.0 ) );
    }
    
    
    
}
