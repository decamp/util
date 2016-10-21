/*
 * Copyright (c) 2016. SocialEmergence.org
 * This code is released under the MIT License
 * https://opensource.org/licenses/MIT
 */
package bits.util;

import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;


/**
 * @author Philip DeCamp
 */
public class MimeTypeTests {

    @Test
    public void testParse() throws IOException {
        List<String> valid = Arrays.asList(
            "application/*",
            "ApplIcation/Thing",
            "ApplIcation/thing",
            "text/plain",
            "text/plain;",
            "text/plain;v=1",
            "text/plain; v=1;  other=happy;",
            "text/plain;v=\"\"",
            "text/plain;v=\"escaped ' with \\\" special\""
        );

        List<String> invalid = Arrays.asList(
                "application",
                "/thing",
                "text/plain;=1",
                "text/plain;v=;other=happy;",
                "text/plain;v=()",
                "text/plain;()=1",
                "()/plain;v=1",
                "text/();v=1"
        );

        for( String s: valid ) {
            MimeType a = MimeType.parse( s );
            MimeType b = MimeType.parse( a.toString() );
            assertEquals( a, b );
        }

        for( String s: invalid ) {
            try {
                MimeType a = MimeType.parse( s );
                fail();
            } catch( IOException ignore ) {}
        }
    }

}
