/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.util.concurrent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.io.OutputStreamWriter;

/**
 * SafetyProcess is a light wrapper around Process that creates a shutdown hook
 * for each generated process to help ensure termination when JVM stops. 
 * For more information, check Runtime.addShutdownHook(), which
 * explains how and when shutdown hooks are executed.
 * <p>
 * SafetyProcess also allows the user to specify a kill command. If specified,
 * this kill command will be sent to the processe's StdIn instead of calling
 * process.destroy(). This may be useful if you want to make sure that external
 * processes are shut down safely.
 * 
 * @author Philip DeCamp
 * @see java.lang.Runtime
 */
public class SafetyProcess extends Process {
    
    public static SafetyProcess exec( String cmd ) throws IOException {
        return exec( cmd, null, null, null );
    }

    public static SafetyProcess exec( String cmd, String[] env ) throws IOException {
        return exec( cmd, env, null, null );
    }

    public static SafetyProcess exec( String cmd, String[] env, File dir ) throws IOException {
        return exec( cmd, env, dir, null );
    }

    public static SafetyProcess exec( String cmd, String[] env, File dir, String killCmd ) throws IOException {
        final Runtime rt = Runtime.getRuntime();
        final SafetyProcess proc = new SafetyProcess( killCmd );

        try {
            rt.addShutdownHook( proc.mHook );
            proc.mProcess = rt.exec( cmd, env, dir );
            return proc;

        } catch( IOException ex ) {
            rt.removeShutdownHook( proc.mHook );
            throw ex;
        }
    }

    public static SafetyProcess exec( String[] cmd ) throws IOException {
        return exec( cmd, null, null, null );
    }

    public static SafetyProcess exec( String[] cmd, String[] env ) throws IOException {
        return exec( cmd, env, null, null );
    }

    public static SafetyProcess exec( String[] cmd, String[] env, File dir ) throws IOException {
        return exec( cmd, env, dir, null );
    }

    public static SafetyProcess exec( String[] cmd, String[] env, File dir, String killCmd ) throws IOException {
        final Runtime rt = Runtime.getRuntime();
        final SafetyProcess proc = new SafetyProcess( killCmd );

        try {
            rt.addShutdownHook( proc.mHook );
            proc.mProcess = rt.exec( cmd, env, dir );
            return proc;

        } catch( IOException ex ) {
            rt.removeShutdownHook( proc.mHook );
            throw ex;
        }
    }


    private final String mKillCommand;
    private final Thread mHook;
    private boolean mHookEnabled = true;
    private Process mProcess = null;


    private SafetyProcess( String killCommand ) {
        mKillCommand = killCommand;
        mHook = new Hook();
    }
    

    public void destroy() {
        mProcess.destroy();
        disableHook();
    }

    
    public int exitValue() {
        int ret = mProcess.exitValue();
        disableHook();
        return ret;
    }

    
    public InputStream getErrorStream() {
        return mProcess.getErrorStream();
    }

    
    public InputStream getInputStream() {
        return mProcess.getInputStream();
    }

    
    public OutputStream getOutputStream() {
        return mProcess.getOutputStream();
    }

    
    public int waitFor() throws InterruptedException {
        int ret = mProcess.waitFor();
        disableHook();
        return ret;
    }



    private synchronized void disableHook() {
        if( mHookEnabled ) {
            mHookEnabled = false;
            try {
                Runtime.getRuntime().removeShutdownHook( mHook );
            } catch( IllegalStateException ex ) {}
        }
    }



    private class Hook extends Thread {
        public Hook() {
            super( "Process Destroyer" );
        }

        public void run() {
            if( mProcess != null ) {
                if( mKillCommand != null ) {
                    try {
                        Writer out = new OutputStreamWriter( mProcess.getOutputStream() );
                        out.write( mKillCommand );
                        out.flush();
                    } catch( Exception ex ) {}
                }

                mProcess.destroy();
            }
        }
    }

}
