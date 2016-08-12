/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */
package bits.util;

import java.util.Arrays;
import java.util.UUID;


/**
 * Converts UUID to a String using a variant of Base64 that is safe for URLs without additional encoding.
 * Specifically, the Base64 chars '+' and '/' are replaced with '-' and '_' respectively, and the trailing
 * "==" is dropped.   
 *
 * @author Philip DeCamp
 */
public final class UrlSafeUuids {

    public static final String REGEX = "[0-9A-Za-z-_]{22}";
    
    /**
     * @param s UUID data in url-safe Base64 format
     * @return equivalent UUID object
     * @throws IllegalArgumentException if {@code s} is not a valid URL-Base64 representation.
     */
    public static UUID parse( String s ) {
        assertParse( s.length() == 22, s );

        //char[] c = s.toCharArray();
        long msb = 0;
        long lsb = 0;

        try {
            for( int i = 0; i < 10; i++ ) {
                int v = TABLE_FROM_URL[ s.charAt( i ) ];
                assertParse( v >= 0, s );
                msb <<= 6;
                msb |= v;
            }

            {
                lsb = TABLE_FROM_URL[ s.charAt( 10 ) ];
                assertParse( lsb >= 0, s );
                msb <<= 4;
                msb |= (lsb >> 2);
            }

            for( int i = 11; i < 21; i++ ) {
                int v = TABLE_FROM_URL[ s.charAt( i ) ];
                assertParse( v >= 0, s );
                lsb <<= 6;
                lsb |= v;
            }

            {
                int v = TABLE_FROM_URL[ s.charAt( 21 ) ];
                assertParse( v >= 0, s );
                lsb <<= 2;
                lsb |= ( v >> 4 );
            }
        } catch( Exception ex ) {
            if( ex instanceof IllegalArgumentException ) {
                throw (IllegalArgumentException)ex;
            } else {
                assertParse( false, s );
            }
        }

        return new UUID( msb, lsb );
    }


    public static String format( UUID u ) {
        long mostSig = u.getMostSignificantBits();
        long leastSig = u.getLeastSignificantBits();

        char[] c = new char[22];
        int shift = 58;

        for( int i = 0; i < 10; i++, shift -= 6 ) {
            c[i] = TABLE_TO_URL[ (int)( ( mostSig >> shift ) & 0x3F )];
        }

        c[10] = TABLE_TO_URL[ (int)( ( mostSig << 2 ) & 0x3C | ( ( leastSig >>> 62 ) & 0x3 ) )];

        shift = 56;
        for( int i = 11; i < 21; i++, shift -= 6 ) {
            c[i] = TABLE_TO_URL[ (int)( ( leastSig >> shift ) & 0x3F) ];
        }

        c[21] = TABLE_TO_URL[ (int)( ( leastSig << 4 ) & 0x30) ];

        return new String( c );
    }

    

    private static void assertParse( boolean b, String s ) {
        if( !b ) {
            throw new IllegalArgumentException( "Invalid UUID string: " + s );
        }
    }

    /**
     * The time difference between the JVM epoch: 01 Jan 1970 00:00:00 UTC and
     * the RFC-4122 UUID epoch: 15 Oct 1582 00:00:00 UTC
     *
     * Note that this number is negative.
     */
    private static final char[] TABLE_TO_URL   = new char[64];
    private static final int[]  TABLE_FROM_URL = new int[128];


    static {
        // Initialize Base64 conversion tables.
        // Table was found as be faster in unit tests compared as if-thens or switches.
        // While this may be partially due as these tables staying in cache,
        // the branching version of the code was probably not much smaller than
        // these tables anyway.
        for( int i = 0; i < 26; i++ ) {
            TABLE_TO_URL[i] = (char)('A' + i);
        }
        for( int i = 26; i < 52; i++ ) {
            TABLE_TO_URL[i] = (char)('a' + (i - 26));
        }
        for( int i = 52; i < 62; i++ ) {
            TABLE_TO_URL[i] = (char)('0' + (i - 52));
        }

        TABLE_TO_URL[62] = '-';
        TABLE_TO_URL[63] = '_';

        Arrays.fill( TABLE_FROM_URL, -1 );
        for( int i = 0; i < 64; i++ ) {
            TABLE_FROM_URL[TABLE_TO_URL[i]] = i;
        }
    }

}
