package bits.util;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * @author Philip DeCamp
 */
public class UuidsTest {
    @Test
    public void hex() {
        UUID a = Uuids.create();
        assertEquals( a, Uuids.fromHex( Uuids.toHex( a ) ) );
    }

    @Test
    public void base64() {
        UUID a = Uuids.create();
        assertEquals( a, Uuids.fromBase64( Uuids.toBase64( a ) ) );
    }

    @Test
    public void url() {
        UUID a = Uuids.create();
        assertEquals( a, Uuids.fromUrl( Uuids.toUrl( a ) ) );
    }

    @Test
    public void bytes() {
        UUID a = Uuids.create();
        assertEquals( a, Uuids.fromBytes( Uuids.toBytes( a ), 0 ) );
    }

    @Test
    public void nameSha256() {
        UUID domain  = Uuids.createRandom();
        UUID name1   = Uuids.hashSha256( domain, "testing" );
        UUID name2   = Uuids.hashSha256( domain, "testing" );
        assertEquals( name1, name2 );
    }

    @Test
    public void binString() {
        UUID a = Uuids.create();
        assertEquals( a, Uuids.fromBinString( Uuids.toBinString( a ) ) );
        String b = "\u0000\uFFFF\u00FF\uFF00\uF0F0\u0F0F\u8888\u0808";
        assertEquals( b, Uuids.toBinString( Uuids.fromBinString( b ) ) );
    }

    @Test
    public void createMd5() {
        UUID domain  = Uuids.createRandom();
        UUID name1   = Uuids.hashMd5( domain, "testing" );
        UUID name2   = Uuids.hashMd5( domain, "testing" );
        assertEquals( name1, name2 );
    }

    @Test
    public void hashSha1() {
        UUID domain  = Uuids.createRandom();
        UUID name1   = Uuids.hashSha1( domain, "testing" );
        UUID name2   = Uuids.hashSha1( domain, "testing" );
        assertEquals( name1, name2 );
    }

}


