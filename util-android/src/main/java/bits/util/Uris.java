package bits.util;

import android.content.Context;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;


/**
 * @author Philip DeCamp
 */
public class Uris {

    public static boolean isLocalFile( Uri uri ) {
        return android.webkit.URLUtil.isFileUrl( uri.toString() );
    }


    public static ByteBuffer readBytes( Context context, Uri uri ) throws IOException {
        try( InputStream in = context.getContentResolver().openInputStream( uri ) ) {
            return Streams.readBytes( in );
        }
    }

}
