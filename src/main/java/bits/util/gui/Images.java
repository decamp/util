/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */
package bits.util.gui;

import java.awt.*;
import java.awt.image.*;
import java.nio.*;


/**
 * @author Philip DeCamp
 */
public class Images {

    public static final int RED   = 0;
    public static final int GREEN = 1;
    public static final int BLUE  = 2;
    public static final int ALPHA = 3;


    public static void alphaToRgb( BufferedImage image, int[] optWork ) {
        final int w = image.getWidth();
        final int h = image.getHeight();
        if( optWork == null || optWork.length < w ) {
            optWork = new int[w];
        }

        for( int y = 0; y < h; y++ ) {
            image.getRGB( 0, y, w, 1, optWork, 0, w );
            for( int x = 0; x < w; x++ ) {
                optWork[x] = ( optWork[x] >>> 24 ) * 0x01010101;
            }
            image.setRGB( 0, y, w, 1, optWork, 0, w );
        }
    }


    public static BufferedImage copy( BufferedImage im ) {
        BufferedImage ret = new BufferedImage( im.getWidth(), im.getHeight(), im.getType() );
        Graphics2D g = (Graphics2D)ret.getGraphics();
        Composite c = g.getComposite();
        g.setComposite( AlphaComposite.Src );
        g.drawImage( im, 0, 0, null );
        g.setComposite( c );
        return ret;
    }

    /**
     * Converts a DataBuffer to a directly allocated java.nio.ByteBuffer.
     */
    public static ByteBuffer dataToByteBuffer( DataBuffer in ) {
        int type      = in.getDataType();
        int elSize    = DataBuffer.getDataTypeSize( type );
        int count     = in.getSize();

        ByteBuffer ret = ByteBuffer.allocateDirect( ( count * elSize + 7 ) / 8 );

        switch( type ) {
        case DataBuffer.TYPE_BYTE:
        {
            for( int i = 0; i < in.getNumBanks(); i++ ) {
                ret.put( ( (DataBufferByte)in ).getData( i ) );
            }
            ret.flip();
            break;
        }

        case DataBuffer.TYPE_INT:
        {
            IntBuffer b = ret.asIntBuffer();

            for( int i = 0; i < in.getNumBanks(); i++ ) {
                b.put( ( (DataBufferInt)in ).getData(i) );
            }

            break;
        }

        case DataBuffer.TYPE_FLOAT:
        {
            FloatBuffer b = ret.asFloatBuffer();

            for( int i = 0; i < in.getNumBanks(); i++ ) {
                b.put( ( (DataBufferFloat)in ).getData(i) );
            }

            break;
        }

        case DataBuffer.TYPE_DOUBLE:
        {
            DoubleBuffer b = ret.asDoubleBuffer();

            for( int i = 0; i < in.getNumBanks(); i++ ) {
                b.put( ( (DataBufferDouble)in ).getData( i ) );
            }

            break;
        }

        case DataBuffer.TYPE_SHORT:
        {
            ShortBuffer b = ret.asShortBuffer();

            for( int i = 0; i < in.getNumBanks(); i++ ) {
                b.put( ((DataBufferShort)in ).getData( i ) );
            }

            break;
        }

        case DataBuffer.TYPE_USHORT:
        {
            ShortBuffer b = ret.asShortBuffer();

            for( int i = 0; i < in.getNumBanks(); i++ ) {
                b.put( ( (DataBufferUShort)in ).getData( i ) );
            }

            break;
        }

        default:
            throw new IllegalArgumentException( "Unknown data buffer type: " + type );
        }

        return ret;
    }


    public static void fillAlpha( BufferedImage image, int alpha, int[] optWork ) {
        final int w = image.getWidth();
        final int h = image.getHeight();
        if( optWork == null || optWork.length < w ) {
            optWork = new int[w];
        }

        alpha = ( alpha & 0xFF ) << 24;

        for( int y = 0; y < h; y++ ) {
            image.getRGB( 0, y, w, 1, optWork, 0, w );
            for( int x = 0; x < w; x++ ) {
                optWork[x] = optWork[x] & 0x00FFFFFF | alpha;
            }
            image.setRGB( 0, y, w, 1, optWork, 0, w );
        }
    }


    public static void fillRgb( BufferedImage image, int rgb, int[] optWork ) {
        final int w = image.getWidth();
        final int h = image.getHeight();
        if( optWork == null || optWork.length < w ) {
            optWork = new int[w];
        }
        rgb &= 0x00FFFFFF;

        for( int y = 0; y < h; y++ ) {
            image.getRGB( 0, y, w, 1, optWork, 0, w );
            for( int x = 0; x < w; x++ ) {
                optWork[x] = optWork[x] & 0xFF000000 | rgb;
            }
            image.setRGB( 0, y, w, 1, optWork, 0, w );
        }
    }


    public static void fillTransparentRgb( BufferedImage image, int rgb, int[] optWork ) {
        final int w = image.getWidth();
        final int h = image.getHeight();
        if( optWork == null || optWork.length < w ) {
            optWork = new int[w];
        }
        rgb &= 0x00FFFFFF;

        for( int y = 0; y < h; y++ ) {
            image.getRGB( 0, y, w, 1, optWork, 0, w );
            for( int x = 0; x < w; x++ ) {
                if( ( optWork[x] & 0xFF000000 ) == 0 ) {
                    optWork[x] = optWork[x] & 0xFF000000 | rgb;
                }
            }
            image.setRGB( 0, y, w, 1, optWork, 0, w );
        }
    }

    /**
     * Converts a BufferedImage to a 32-bit BGRA format and places it into
     * a directly allocated java.nio.ByteBuffer.
     * 
     * @param image      Input image to convert.
     * @param optWorkSpace  Optional array that may be used if <code>workSpace.length &gt;= image.getWidth()</code>.
     * @return Directly allocated ByteBuffer containing pixels in BGRA format and sRGB color space.
     */
    public static ByteBuffer imageToBgraBuffer( BufferedImage image, int[] optWorkSpace ) {
        int w = image.getWidth();
        int h = image.getHeight();
        int[] row = optWorkSpace != null && optWorkSpace.length >= w ? optWorkSpace : new int[w];
        
        ByteBuffer ret = ByteBuffer.allocateDirect( (w * h) * 4 );
        ret.order( ByteOrder.LITTLE_ENDIAN );
        IntBuffer ib = ret.asIntBuffer();
        
        for( int i = 0; i < h; i++ ) {
            image.getRGB( 0, i, w, 1, row, 0, w );
            ib.put( row, 0, w );
        }
        
        return ret;
    }


    public static float[] imageToPlane( BufferedImage image, int component, int[] optWork, float[] optOut ) {
        final int w = image.getWidth();
        final int h = image.getHeight();
        if( optWork == null || optWork.length < w ) {
            optWork = new int[w];
        }
        if( optOut == null ) {
            optOut = new float[w*h];
        }

        final int shift;
        switch( component ) {
        case RED:
            shift = 16;
            break;
        case GREEN:
            shift = 8;
            break;
        case BLUE:
            shift = 0;
            break;
        default:
            shift = 24;
            break;
        }

        final float scale = 1f / 255f;

        for( int y = 0; y < h; y++ ) {
            image.getRGB( 0, y, w, 1, optWork, 0, w );
            for( int x = 0; x < w; x++ ) {
                optOut[ x + y * w ] = ( optWork[x] >> shift & 0xFF ) * scale;
            }
        }

        return optOut;
    }


    public static float[][] imageToRgbPlanes( BufferedImage image, int[] optWork, float[][] optOut ) {
        final int w = image.getWidth();
        final int h = image.getHeight();
        if( optWork == null || optWork.length < w ) {
            optWork = new int[w];
        }
        if( optOut == null ) {
            optOut = new float[3][w*h];
        }
        
        final float[] cr = optOut[0];
        final float[] cg = optOut[1];
        final float[] cb = optOut[2];
        final float scale = 1f / 255f;
        
        for( int y = 0; y < h; y++ ) {
            image.getRGB( 0, y, w, 1, optWork, 0, w );
            for( int x = 0; x < w; x++ ) {
                int i = x + y * w;
                int v = optWork[x];
                cr[i] = ( v >> 16 & 0xFF ) * scale;
                cg[i] = ( v >>  8 & 0xFF ) * scale;
                cb[i] = ( v       & 0xFF ) * scale;
            }
        }
        
        return optOut;
    }


    public static float[][] imageToRgbaPlanes( BufferedImage image, int[] optWork, float[][] optOut ) {
        final int w = image.getWidth();
        final int h = image.getHeight();
        if( optWork == null || optWork.length < w ) {
            optWork = new int[w];
        }
        if( optOut == null ) {
            optOut = new float[4][w*h];
        }
        
        final float[] cr = optOut[0];
        final float[] cg = optOut[1];
        final float[] cb = optOut[2];
        final float[] ca = optOut[3];
        final float scale = 1f / 255f;
        
        for( int y = 0; y < h; y++ ) {
            image.getRGB( 0, y, w, 1, optWork, 0, w );
            for( int x = 0; x < w; x++ ) {
                int i = x + y * w;
                int v = optWork[x];
                cr[i] = ( v >> 16 & 0xFF ) * scale;
                cg[i] = ( v >>  8 & 0xFF ) * scale;
                cb[i] = ( v       & 0xFF ) * scale;
                ca[i] = ( v >> 24 & 0xFF ) * scale;
            }
        }
        
        return optOut;
    }


    public static void invert( BufferedImage image, int[] optWork ) {
        final int w = image.getWidth();
        final int h = image.getHeight();
        if( optWork == null || optWork.length < w ) {
            optWork = new int[w];
        }

        for( int y = 0; y < h; y++ ) {
            image.getRGB( 0, y, w, 1, optWork, 0, w );
            for( int x = 0; x < w; x++ ) {
                int v = optWork[x];
                int a = 0xFF - ( v >>> 24        );
                int r = 0xFF - ( v >>  16 & 0xFF );
                int g = 0xFF - ( v >>   8 & 0xFF );
                int b = 0xFF - ( v        & 0xFF );
                optWork[x] = a | r << 16 | g << 8 | b;
            }
            image.setRGB( 0, y, w, 1, optWork, 0, w );
        }
    }


    public static void invertAlpha( BufferedImage image, int[] optWork ) {
        final int w = image.getWidth();
        final int h = image.getHeight();
        if( optWork == null || optWork.length < w ) {
            optWork = new int[w];
        }

        for( int y = 0; y < h; y++ ) {
            image.getRGB( 0, y, w, 1, optWork, 0, w );
            for( int x = 0; x < w; x++ ) {
                int v = optWork[x];
                int a = 0xFF - ( v >>> 24 );
                optWork[x] = v & 0x00FFFFFF | a << 24;
            }
            image.setRGB( 0, y, w, 1, optWork, 0, w );
        }
    }


    public static void invertRgb( BufferedImage image, int[] optWork ) {
        final int w = image.getWidth();
        final int h = image.getHeight();
        if( optWork == null || optWork.length < w ) {
            optWork = new int[w];
        }

        for( int y = 0; y < h; y++ ) {
            image.getRGB( 0, y, w, 1, optWork, 0, w );

            for( int x = 0; x < w; x++ ) {
                int v = optWork[x];
                int a = v & 0xFF000000;
                int r = 0xFF - ( v >> 16 & 0xFF);
                int g = 0xFF - ( v >>  8 & 0xFF);
                int b = 0xFF - ( v       & 0xFF);
                optWork[x] = a | r << 16 | g << 8 | b;
            }

            image.setRGB( 0, y, w, 1, optWork, 0, w );
        }
    }


    public static void meanRgbToAlpha( BufferedImage image, int[] optWork ) {
        final int w = image.getWidth();
        final int h = image.getHeight();
        if( optWork == null || optWork.length < w ) {
            optWork = new int[w];
        }

        for( int y = 0; y < h; y++ ) {
            image.getRGB( 0, y, w, 1, optWork, 0, w );

            for( int x = 0; x < w; x++ ) {
                int v = optWork[x];
                int r = v >> 16 & 0xFF;
                int g = v >>  8 & 0xFF;
                int b = v       & 0xFF;
                optWork[x] = v & 0x00FFFFFF | ( r + g + b ) / 3 << 24;
            }

            image.setRGB( 0, y, w, 1, optWork, 0, w );
        }
    }

    /**
     * Multiplies each pixel by another color value (normalized by 255).
     * For example, for a given pixel: <br>
     * {@code 0xAaRrGgBb * 0xFFFFFFFF = 0xAaRrGgBb} <br>
     * {@code 0xAaRrGgBb * 0xFF000000 = 0xAa000000}
     *
     * @param image   Image to mult.
     * @param argb    The 4 coefficients used to multiply each color component (a, r, g, b )
     * @param optWork Working memory. Can avoid a memory allocation if optWork != null and
     *                optWork.length >= image.getWidth().
     */
    public static void mult( BufferedImage image, int argb, int[] optWork ) {
        final int w = image.getWidth();
        final int h = image.getHeight();
        final int va = ( argb >>> 24        );
        final int vr = ( argb >>> 16 & 0xFF );
        final int vg = ( argb >>>  8 & 0xFF );
        final int vb = ( argb        & 0xFF );

        if( optWork == null || optWork.length < w ) {
            optWork = new int[w];
        }

        for( int y = 0; y < h; y++ ) {
            image.getRGB( 0, y, w, 1, optWork, 0, w );
            for( int x = 0; x < w; x++ ) {
                int v = optWork[x];
                int a = ( v >>> 24        ) * va / 0xFF;
                int r = ( v >>  16 & 0xFF ) * vr / 0xFF;
                int g = ( v >>   8 & 0xFF ) * vg / 0xFF;
                int b = ( v        & 0xFF ) * vb / 0xFF;
                optWork[x] = a << 24 | r << 16 | g << 8 | b;
            }
            image.setRGB( 0, y, w, 1, optWork, 0, w );
        }
    }


    public static void multRgbByAlpha( BufferedImage image, int[] optWork ) {
        final int w = image.getWidth();
        final int h = image.getHeight();
        if( optWork == null || optWork.length < w ) {
            optWork = new int[w];
        }

        for( int y = 0; y < h; y++ ) {
            image.getRGB( 0, y, w, 1, optWork, 0, w );
            for( int x = 0; x < w; x++ ) {
                int v = optWork[x];
                int a = v >>> 24;
                int r = ( v >> 16 & 0xFF ) * a / 0xFF;
                int g = ( v >>  8 & 0xFF ) * a / 0xFF;
                int b = ( v       & 0xFF ) * a / 0xFF;
                optWork[x] = a << 24 | r << 16 | g << 8 | b;
            }
            image.setRGB( 0, y, w, 1, optWork, 0, w );
        }
    }


    public static BufferedImage resizeBicubic( BufferedImage im, int w, int h ) {
        BufferedImage ret = new BufferedImage(w, h, im.getType());
        Graphics2D g = (Graphics2D)ret.getGraphics();
        g.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR );
        g.drawImage( im, 0, 0, w, h, null );
        return ret;
    }


    public static BufferedImage resizeBilinear( BufferedImage im, int w, int h ) {
        BufferedImage ret = new BufferedImage(w, h, im.getType());
        Graphics2D g = (Graphics2D)ret.getGraphics();
        g.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR );
        g.drawImage( im, 0, 0, w, h, null );
        return ret;
    }


    public static BufferedImage rgbPlanesToImage( float[][] planes, 
                                                  int w,
                                                  int h, 
                                                  int[] optWork, 
                                                  BufferedImage optOut ) 
    {
        if( optOut == null ) {
            optOut = new BufferedImage( w, h, BufferedImage.TYPE_INT_ARGB );
        }
        if( optWork == null || optWork.length < w ) {
            optWork = new int[w];
        }
        
        final float[] pr = planes[0];
        final float[] pg = planes[1];
        final float[] pb = planes[2];
        
        for( int y = 0; y < h; y++ ) {
            for( int x = 0; x < w; x++ ) {
                int i = x + y * w;
                int cr = (int)( pr[i] * 255f + 0.5f ) & 0xFF;
                int cg = (int)( pg[i] * 255f + 0.5f ) & 0xFF;
                int cb = (int)( pb[i] * 255f + 0.5f ) & 0xFF;
                optWork[x] = 0xFF000000 | cr << 16 | cg << 8 | cb;
            }
            optOut.setRGB( 0, y, w, 1, optWork, 0, w );
        }
        
        return optOut;
    }
    

    public static BufferedImage rgbaPlanesToImage( float[][] planes, 
                                                   int w,
                                                   int h, 
                                                   int[] optWork, 
                                                   BufferedImage optOut ) 
    {
        if( optOut == null ) {
            optOut = new BufferedImage( w, h, BufferedImage.TYPE_INT_ARGB );
        }
        if( optWork == null || optWork.length < w ) {
            optWork = new int[w];
        }

        final float[] pr = planes[0];
        final float[] pg = planes[1];
        final float[] pb = planes[2];
        final float[] pa = planes[3];

        for( int y = 0; y < h; y++ ) {
            for( int x = 0; x < w; x++ ) {
                int i = x + y * w;
                int cr = (int)( pr[i] * 255f + 0.5f ) & 0xFF;
                int cg = (int)( pg[i] * 255f + 0.5f ) & 0xFF;
                int cb = (int)( pb[i] * 255f + 0.5f ) & 0xFF;
                int ca = (int)( pa[i] * 255f + 0.5f ) & 0xFF;
                optWork[x] = ca << 24 | cr << 16 | cg << 8 | cb;
            }
            optOut.setRGB( 0, y, w, 1, optWork, 0, w );
        }

        return optOut;
    }


    public static void swapRedBlue( BufferedImage image, int[] optWork ) {
        final int w = image.getWidth();
        final int h = image.getHeight();
        if( optWork == null || optWork.length < w ) {
            optWork = new int[w];
        }
        
        for( int y = 0; y < h; y++ ) {
            image.getRGB( 0, y, w, 1, optWork, 0, w );
            for( int x = 0; x < w; x++ ) {
                int v = optWork[x];
                optWork[x] = v & 0xFF00FF00 | v >> 16 & 0xFF | v << 16 & 0x00FF0000;
            }
            image.setRGB( 0, y, w, 1, optWork, 0, w );
        }
    }
    
    
    public static BufferedImage toArgb( BufferedImage im ) {
        if( im.getType() == BufferedImage.TYPE_INT_ARGB ) {
            return im;
        }
        
        final int w = im.getWidth();
        final int h = im.getHeight();
        BufferedImage ret = new BufferedImage( w, h, BufferedImage.TYPE_INT_ARGB );
        Graphics2D g = (Graphics2D)ret.getGraphics();
        g.setComposite( AlphaComposite.Src );
        g.drawImage( im, 0, 0, null );
        return ret;
    }
    
    
    public static BufferedImage toGrayscale( BufferedImage im ) {
        if( im.getType() == BufferedImage.TYPE_BYTE_GRAY ) {
            return im;
        }
        
        final int w = im.getWidth();
        final int h = im.getHeight();
        BufferedImage ret = new BufferedImage( w, h, BufferedImage.TYPE_BYTE_GRAY );
        Graphics2D g = (Graphics2D)ret.getGraphics();
        g.setComposite( AlphaComposite.Src );
        g.drawImage( im, 0, 0, null );
        return ret;
    }

    /**
     * @deprecated use {@link #dataToByteBuffer( DataBuffer )}
     */
    public static ByteBuffer dataToByteBuffer( DataBuffer in, ByteOrder order ) {
        return dataToByteBuffer( in ).order( order );
    }

}
