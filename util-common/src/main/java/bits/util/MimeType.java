/*
 * Copyright (c) 2016. SocialEmergence.org
 * This code is released under the MIT License
 * https://opensource.org/licenses/MIT
 */

package bits.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * A Multipurpose Internet Mail Extension (MIME) type. Basice MIME types
 * are defined in RFC 2045. This implementation also includes wildcard
 * matching, even though I don't know where that protocol is defined.
 *
 * <pre>{@code
 * RFC 2045                Internet Message Bodies            November 1996
 *
 *  5.1.  Syntax of the Content-Type Header Field
 *
 *  In the Augmented BNF notation of RFC 822, a Content-Type header field
 *  value is defined as follows:
 *
 *  content := "Content-Type" ":" type "/" subtype
 *  *(";" parameter)
 *  ; Matching of content type and subtype
 *  ; is ALWAYS case-insensitive.
 *
 *  type := discrete-type / composite-type
 *
 *  discrete-type := "text" / "image" / "audio" / "video" /
 *  "application" / extension-token
 *
 *  composite-type := "message" / "multipart" / extension-token
 *
 *  extension-token := ietf-token / x-token
 *
 *  ietf-token := <An extension token defined by a
 *  standards-track RFC and registered
 *  with IANA.>
 *
 *  x-token := <The two characters "X-" or "x-" followed, with
 *  no intervening white space, by any token>
 *
 *  subtype := extension-token / iana-token
 *
 *  iana-token := <A publicly-defined extension token. Tokens
 *  of this form must be registered with IANA
 *  as specified in RFC 2048.>
 *
 *  parameter := attribute "=" value
 *
 *  attribute := token
 *  ; Matching of attributes
 *  ; is ALWAYS case-insensitive.
 *
 *  value := token / quoted-string
 *
 *  token := 1*<any (US-ASCII) CHAR except SPACE, CTLs,
 *  or tspecials>
 *
 *  tspecials :=  "(" / ")" / "<" / ">" / "@" /
 *  "," / ";" / ":" / "\" / <">
 *  "/" / "[" / "]" / "?" / "="
 *  ; Must be in quoted-string,
 *  ; to use within parameter values
 * }</pre>
 *
 * @author decamp
 */
public class MimeType {

    public static MimeType parse( String str ) throws IOException {
        int slashIndex = str.indexOf( '/' );
        int semIndex   = str.indexOf( ';' );

        if( slashIndex < 0 || semIndex >= 0 && semIndex < slashIndex ) {
            throw new IOException( "Unable to find a subtype type." );
        }

        // We have a type and subtype type but no parameter list
        String primary = str.substring( 0, slashIndex ).trim().toLowerCase( Locale.ENGLISH );
        String sub;
        Map<String,String> params = null;

        if( semIndex < 0 ) {
            sub = str.substring( slashIndex + 1 ).trim().toLowerCase( Locale.ENGLISH );
        } else {
            sub = str.substring( slashIndex + 1, semIndex ).trim().toLowerCase( Locale.ENGLISH );
            params = parseParams( str, semIndex );
        }

        if( !isValidToken( primary ) ) {
            throw new IOException( "Primary type is invalid." );
        }

        if( !isValidToken( sub ) ) {
            throw new IOException( "Sub type is invalid." );
        }

        return new MimeType( false, primary, sub, params );
    }


    private final String              mPrimary;
    private final String              mSub;
    private final Map<String, String> mParams;

    /**
     * Creates default mimetype: "application/*".
     */
    public MimeType() {
        mPrimary = "application";
        mSub = "*";
        mParams  = null;
    }

    /**
     * Constructor that builds a MimeType with the given type and subtype type.
     *
     * @param primary   the type MIME type
     * @param sub       the MIME subtype-type
     * @param optParams [optional] List of paramaters.
     */
    public MimeType( String primary, String sub, Map<String,String> optParams ) {
        if( !isValidToken( primary ) ) {
            throw new IllegalArgumentException( primary );
        }
        
        if( !isValidToken( sub ) ) {
            throw new IllegalArgumentException( sub );
        }

        mPrimary = lowerCase( primary );
        mSub     = lowerCase( sub );

        if( optParams == null ) {
            mParams = null;
        } else {
            mParams = new HashMap<>();
            for( Map.Entry<String,String> e: optParams.entrySet() ) {
                String s = lowerCase( e.getKey() );
                if( !isValidToken( s ) ) {
                    throw new IllegalArgumentException( s );
                }
                mParams.put( s, Objects.requireNonNull( e.getValue() ) );
            }
        }
    }

    /**
     * Constructor that builds a MimeType with the given type and subtype type.
     *
     * @param primary      the type MIME type
     * @param sub          the MIME subtype-type
     * @param paramKeyVals Array of parameters in the form {@code [key0, val0, key1, val1, ...]}
     */
    public MimeType( String primary, String sub, String... paramKeyVals ) {
        if( !isValidToken( primary ) ) {
            throw new IllegalArgumentException( primary );
        }
        if( !isValidToken( sub ) ) {
            throw new IllegalArgumentException( sub );
        }

        mPrimary = lowerCase( primary );
        mSub     = lowerCase( sub );
        int len  = paramKeyVals.length / 2;

        if( len < 1 ) {
            mParams = null;
        } else {
            mParams = new HashMap<>( len );
            for( int i = 0; i < len; i++ ) {
                String key = lowerCase( paramKeyVals[i*2] );
                if( !isValidToken( key ) ) {
                    throw new IllegalArgumentException( key );
                }
                mParams.put( key, Objects.requireNonNull( paramKeyVals[i*2+1] ) );
            }
        }
    }


    private MimeType( boolean ignore, String primary, String subtype, Map<String,String> params ) {
        mPrimary = primary;
        mSub = subtype;
        mParams  = params;
    }


    /**
     * @return the type MIME type
     */
    public String type() {
        return mPrimary;
    }

    /**
     * @return the MIME subtype
     */
    public String subtype() {
        return mSub;
    }

    /**
     * Retrieve the value associated with the given name, or null if there
     * is no current association.
     *
     * @param name the parameter name
     * @return the paramter's value
     */
    public String param( String name ) {
        return mParams != null ? mParams.get( name.toLowerCase() ) : null;
    }

    /**
     * @param out Receives parameters of this type object.
     */
    public void params( Map<? super String,? super String> out ) {
        if( mParams != null ) {
            out.putAll( mParams );
        }
    }

    /**
     * Determine if the type and subtype type of this object is
     * the same as what is in the given type.
     *
     * @param type the MimeType object to compare with
     * @return true if they match
     */
    public boolean matches( MimeType type ) {
        return partMatches( mPrimary, type.mPrimary ) &&
               partMatches( mSub, type.mSub );
    }


    /**
     * @return String representation of this object complient with RFC 2045.
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder( mPrimary );
        s.append( '/' );
        s.append( mSub );
        if( mParams != null ) {
            for( Map.Entry<String,String> e: mParams.entrySet() ) {
                s.append( ';' );
                s.append( e.getKey() );
                s.append( '=' );
                escape( e.getValue(), s );
            }
        }
        return s.toString();
    }

    @Override
    public boolean equals( Object o ) {
        if( !( o instanceof MimeType ) ) {
            return false;
        }
        MimeType m = (MimeType)o;
        return this == m ||
               mPrimary.equals( m.mPrimary ) &&
               mSub.equals( m.mSub ) &&
               Objects.equals( mParams, m.mParams );
    }


    /**
     * Determine whether or not a given character belongs to a legal token.
     */
    private static boolean isTokenChar( char c ) {
        if( c <= 0x20 || c >= 0x7f ) {
            return false;
        }

        switch( c ) {
        default:
            return true;
        case '(':
        case ')':
        case '<':
        case '>':
        case '@':
        case ',':
        case ';':
        case ':':
        case '\\':
        case '"':
        case '[':
        case ']':
        case '/':
        case '?':
        case '=':
            return false;
        }
    }

    /**
     * Determine whether or not a given string is a legal token.
     */
    private static boolean isValidToken( String s ) {
        int len = s.length();
        if( len == 0 ) {
            return false;
        }

        for( int i = 0; i < len; ++i ) {
            char c = s.charAt( i );
            if( !isTokenChar( c ) ) {
                return false;
            }
        }

        return true;
    }

    /**
     * A routine that knows how and when to escape and escape the given value.
     */
    private static void escape( String value, StringBuilder out ) {
        boolean needsQuotes = false;

        // Check if value must be quoted.
        int length = value.length();
        if( length == 0 ) {
            needsQuotes = true;
        } else {
            for( int i = 0; i < length; i++ ) {
                if( !isTokenChar( value.charAt( i ) ) ) {
                    needsQuotes = true;
                    break;
                }
            }
        }

        if( needsQuotes ) {
            out.ensureCapacity( length * 3 / 2 );

            // Starting escape
            out.append( '"' );

            // Add the escaped text
            for( int i = 0; i < length; ++i ) {
                char c = value.charAt( i );
                if( c == '\\' || c == '"' ) {
                    out.append( '\\' );
                }
                out.append( c );
            }

            // Closing escape.
            out.append( '"' );

        } else {
            out.append( value );
        }
    }


    private static String lowerCase( String s ) {
        return s.toLowerCase( Locale.ENGLISH );
    }

    /**
     * A routine for parsing the parameter list out of a String.
     *
     * @param str an RFC 2045, 2046 compliant parameter list.
     */
    private static Map<String,String> parseParams( String str, int off ) throws IOException {
        if( str == null ) {
            return null;
        }

        final int length = str.length();
        if( length <= off ) {
            return null;
        }

        Map<String,String> ret = new HashMap<>();

        while( off < length && str.charAt( off ) == ';' ) {
            // Skip semicolon and whitespace.
            off = skipWhiteSpace( str, off + 1 );

            // Tolerate trailing semicolon, even though it violates spec
            if( off >= length ) {
                return null;
            }

            // Find end of token.
            int lastIndex = off;
            while( off < length && isTokenChar( str.charAt( off ) ) ) {
                off++;
            }

            if( lastIndex == off ) {
                throw new IOException( "Invalid parameter name" );
            }

            String name = str.substring( lastIndex, off ).toLowerCase( Locale.ENGLISH );
            // Skip '=' that separates name from value
            off = skipWhiteSpace( str, off );
            if( off >= length || str.charAt( off ) != '=' ) {
                throw new IOException( "Missing '='" );
            }

            // Skip '=' and following spaces.
            off = skipWhiteSpace( str, off + 1 );
            if( off >= length ) {
                throw new IOException( "Missing value" );
            }

            // Determine if value is quoted.
            if( str.charAt( off ) == '"' ) {
                // Find end escape.
                StringBuilder sb = new StringBuilder( Math.min( 32, length - off - 2 ) );

UNQUOTE_LOOP:
                while( true ) {
                    if( ++off >= length ) {
                        throw new IOException( "Missing end escape" );
                    }

                    switch( str.charAt( off ) ) {
                    case '\\':
                        sb.append( str.charAt( ++off ) );
                        break;

                    case '"':
                        off++;
                        break UNQUOTE_LOOP;

                    default:
                        sb.append( str.charAt( off ) );
                        break;
                    }
                }

                ret.put( name, sb.toString() );
                // Skip escape and spaces.
                off = skipWhiteSpace( str, off + 1 );

            } else {
                lastIndex = off;
                while( off < length && isTokenChar( str.charAt( off ) ) ) {
                    off++;
                }

                if( lastIndex == off ) {
                    throw new IOException( "Unexpected character at position " + off );
                }

                ret.put( name, str.substring( lastIndex, off ) );
            }
        }

        if( off < length ) {
            throw new IOException( "Invalid input" );
        }

        return ret;
    }

    /**
     * @return index of the first non-white space character at or after index {@code i}
     */
    private static int skipWhiteSpace( String str, int i ) {
        final int len = str.length();
        while( i < len && Character.isWhitespace( str.charAt( i ) ) ) {
            i++;
        }
        return i;
    }


    private static boolean partMatches( String a, String b ) {
        return "*".equals( a ) ||
               "*".equals( b ) ||
               a.equals( b );
    }

}
