/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.util.reflect;

@SuppressWarnings( "unused" )
public class Foo2 extends Foo1 {
    
    
    public Foo2( String name ) {
        super( name );
    }
    
    
    
    public String bar() {
        return "Foo2.bar";
    }
    
    
    public String bar( int a ) {
        return "Foo2.bar int";
    }

    
    public String barBar( Foo2 a ) {
        return "Foo2.barBar Foo2";
    }
    
    
    public String barBar( Foo1 b ) {
        return "Foo2.barBar Foo1";
    }
    
    
    public String bar( Foo1 a, Foo1 b, Foo1 c ) {
        return "Foo2.bar Foo1 Foo1 Foo1";
    }
    
    public String bar( Foo1 a, Foo1 b, Foo2 c ) {
        return "Foo2.bar Foo1 Foo1 Foo2";
    }
    
    
    public String bar( Foo1 a, Foo2 b, Foo1 c ) {
        return "Foo2.bar Foo1 Foo2 Foo1";
    }
    
    
    
    
    
    
    public static String staticBar() {
        return "static Foo2.staticBar";
    }
    

}
