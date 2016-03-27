/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.util.reflect;

@SuppressWarnings( "unused" )
class Foo1 {
    
    private final String mName;
    
    
    public Foo1() {
        mName = "";
    }
    
    
    public Foo1( String name ) {
        mName = name;
    }
    
    
    String bar() {
        return "Foo1.bar";
    }
    
    
    public String bar( int a ) {
        return "Foo1.bar int";
    }
    
    
    public String bar( Integer a ) {
        return "Foo1.bar Integer";
    }
    
    
    public String bar( Integer... a ) {
        return "Foo1.bar Integer...";
    }
    
    
    public String bar( Foo1 a ) {
        return "Foo1.bar Foo1";
    }
    

    public String name() {
        return mName;
    }
    
    
    public static String staticBar() {
        return "static Foo1.staticBar";
    }
    
}
