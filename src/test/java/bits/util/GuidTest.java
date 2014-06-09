package bits.util;
/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 


import static org.junit.Assert.*;

import org.junit.Test;

import bits.util.Guid;

/**
 * @author decamp
 */
public class GuidTest {

    @Test
    public void testEncodeDecode() {
        Guid g = Guid.createRandom();
        Guid a;
        
        a = Guid.fromHex( g.toHex() );
        assertTrue( a.equals( g ) );
        
        a = Guid.fromBase64( g.toBase64() );
        assertTrue( a.equals( g ) );
        
        a = Guid.fromBytes( g.toBytes(), 0 );
        assertTrue( a.equals( g ) );
        
        a = Guid.fromUUID( g.toUUID() );
        assertTrue( a.equals( g ) );
    }
    
}

