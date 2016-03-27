/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */
package bits.util.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.swing.*;
import javax.imageio.ImageIO;



/**
 * @author Philip DeCamp
 */
public class ImagePanel extends JPanel {

    public static JFrame showImage( File file ) {
        return showImage( file, true );
    }

    public static JFrame showImage( File file, boolean exitOnClose ) {
        try {
            BufferedImage image = ImageIO.read( file );
            return showImage( image, exitOnClose );
        } catch( IOException ex ) {
            ex.printStackTrace();
            return null;
        }
    }

    public static JFrame showImage( BufferedImage image ) {
        return showImage( image, true );
    }

    public static JFrame showImage( BufferedImage image, boolean exitOnClose ) {
        JFrame frame = frameImagePanel( new ImagePanel( image ), exitOnClose );
        frame.setVisible( true );
        return frame;
    }

    public static JFrame frameImagePanel( ImagePanel panel ) {
        return frameImagePanel( panel, true );
    }

    public static JFrame frameImagePanel( ImagePanel panel, boolean exitOnClose ) {
        JFrame frame = new JFrame();
        if( exitOnClose ) {
            frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        } else {
            frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
        }

        BufferedImage image = panel.getImage();
        if( image == null ) {
            frame.setBounds( 100, 100, 500, 500 );
        } else {
            frame.setBounds( 100, 100, image.getWidth() + 10, image.getHeight() + 40 );
        }

        frame.setContentPane( panel );
        frame.pack();

        if( image != null ) {
            frame.setSize( frame.getWidth() + image.getWidth() - panel.getWidth(),
                           frame.getHeight() + image.getHeight() - panel.getHeight() );
        }

        return frame;
    }

    public static JFrame framePanel( JPanel panel ) {
        return framePanel( panel, true );
    }

    public static JFrame framePanel( JPanel panel, boolean exitOnClose ) {
        JFrame frame = new JFrame();

        if( exitOnClose ) {
            frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        } else {
            frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
        }

        frame.setBounds( 100, 100, 500, 500 );
        frame.setContentPane( panel );
        return frame;
    }


    private BufferedImage mImage;
    private boolean mResize        = true;
    private boolean mKeepAspect    = true;
    private boolean mInterpolation = false;
    
    
    public ImagePanel() {
        this( null );
    }
    
    public ImagePanel( BufferedImage image ) {
        mImage = image;

        addMouseListener( new MouseAdapter() {
            @Override
            public void mousePressed( MouseEvent e ) {
                switch( e.getButton() ) {
                case 2:
                    mInterpolation = !mInterpolation;
                    repaint();
                    break;
                case 3:
                    mResize = !mResize;
                    repaint();
                    break;
                }
            }
        } );
    }
    
    
    public BufferedImage getImage() {
        return mImage;
    }

    public boolean interpolation() {
        return mInterpolation;
    }
    
    public void interpolation( boolean v ) {
        mInterpolation = v;
    }
    
    public boolean maintainAspectRatio() {
        return mKeepAspect;
    }
    
    public void maintainAspectRatio( boolean v ) {
        mKeepAspect = v;
    }

    public boolean resizeToFit() {
        return mResize;
    }
    
    public void resizeToFit( boolean resize ) {
        mResize = resize;
    }

    public synchronized void setImage( BufferedImage image ) {
        mImage = image;
        repaint();
    }
    
    public synchronized void paintComponent( Graphics gg ) {
        Graphics2D g = (Graphics2D)gg;

        g.setColor( Color.GRAY );
        g.fillRect( 0, 0, getWidth(), getHeight() );
        Object interp = mInterpolation ? RenderingHints.VALUE_INTERPOLATION_BICUBIC : RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
        g.setRenderingHint( RenderingHints.KEY_INTERPOLATION, interp );
        
        if( mImage != null ) {
            if( mResize ) {
                if( mKeepAspect ) {
                    if( getWidth() * mImage.getHeight() < getHeight() * mImage.getWidth() ) {
                        int h = getWidth() * mImage.getHeight() / mImage.getWidth();
                        g.drawImage( mImage, 0, 0, getWidth(), h, null );

                    } else {
                        int w = getHeight() * mImage.getWidth() / mImage.getHeight();
                        g.drawImage( mImage, 0, 0, w, getHeight(), null );
                    }
                } else {
                    g.drawImage( mImage, 0, 0, getWidth(), getHeight(), null );
                }
            } else {
                g.drawImage( mImage, 0, 0, null );
            }
        }
    }
    
}
