/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.util.reflect;

import org.junit.Test;

import bits.util.reflect.Methods;
import static org.junit.Assert.*;


@SuppressWarnings( "all" )
public class TestMethods {
    
    @Test
    public void testCons() throws Exception {
        Foo1 a = Methods.construct( Foo1.class );
        Foo2 b = null;
        
        try {
            b = Methods.construct( Foo2.class );
        } catch( Exception ex ) {}
        
        assertTrue( b == null );
        
        b = Methods.construct( Foo2.class, "nom" );
        assertEquals( Methods.invoke( b, "name" ), "nom" );
    }
    
    
    @Test
    public void testInvoke() throws Exception {
        Foo1 a = new Foo1();
        Foo2 b = new Foo2( "nom" );
        
        String r = null;
        try {
            r = (String)Methods.invoke( a, "bar" );
        } catch( Exception ex ) {}
        
        assertTrue( r == null );
        assertEquals( Methods.invoke( a, "bar", 4 ), "Foo1.bar Integer" );
        
        Integer[] nums = { 3, 4 };
        assertEquals( Methods.invoke( a, "bar", new Object[]{ nums } ), "Foo1.bar Integer..." );
        
        assertEquals( Methods.invoke( a, "bar", a ), "Foo1.bar Foo1" );
        assertEquals( Methods.invoke( a, "bar", b ), "Foo1.bar Foo1" );
        assertEquals( Methods.invoke( b, "barBar", a ), "Foo2.barBar Foo1" );
        assertEquals( Methods.invoke( b, "barBar", b ), "Foo2.barBar Foo2" );
        assertEquals( Methods.invoke( b, "bar", b, a, a ), "Foo2.bar Foo1 Foo1 Foo1" );
        assertEquals( Methods.invoke( b, "bar", b, b, a ), "Foo2.bar Foo1 Foo2 Foo1" );
        assertEquals( Methods.invoke( b, "bar", b, a, b ), "Foo2.bar Foo1 Foo1 Foo2" );
    }
    
    
    @Test
    public void testStatic() throws Exception {
        Foo1 a = new Foo1();
        Foo2 b = new Foo2( "nom" );
        
        assertEquals( Methods.invoke( b, "bar" ), "Foo2.bar" );
        assertEquals( Methods.invoke( a, "staticBar" ), "static Foo1.staticBar" );
        assertEquals( Methods.invoke( b, "staticBar" ), "static Foo2.staticBar" );
        assertEquals( Methods.invokeStatic( Foo1.class, "staticBar" ), "static Foo1.staticBar" );
        assertEquals( Methods.invokeStatic( Foo2.class, "staticBar" ), "static Foo2.staticBar" );
    }
    
}
