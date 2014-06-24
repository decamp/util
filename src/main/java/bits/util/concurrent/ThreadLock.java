package bits.util.concurrent;

import java.io.InterruptedIOException;


/**
 * Lock for a single thread that re-implements basic notify/interrupt stuff.
 * The issue with interrupting threads directly is that it
 * can mess up IO pretty bad, so I needed something to perform
 * interrupts at a higher level in a gentler manner.
 *
 * @author decamp
 */
public class ThreadLock {

    private boolean mInterrupted = false;


    /**
     * Equivalent to {@code wait()}.
     *
     * @throws InterruptedIOException
     */
    public synchronized void block() throws InterruptedIOException {
        check();
        try {
            wait();
        } catch( InterruptedException ex ) {
            throw new InterruptedIOException();
        }
        check();
    }

    /**
     * Equivalent to {@code wait()}.
     *
     * @param millis Max number of milliseconds to wait.
     * @throws InterruptedIOException
     */
    public synchronized void block( long millis ) throws InterruptedIOException {
        check();
        try {
            wait( millis );
        } catch( InterruptedException ex ) {
            throw new InterruptedIOException();
        }
        check();
    }

    /**
     * Equivalent to {@code notify()}.
     */
    public synchronized void unblock() {
        notifyAll();
    }

    /**
     * Interrupts any thread currently blocking on this lock.
     */
    public synchronized void interrupt() {
        mInterrupted = true;
        notifyAll();
    }

    /**
     * @return true iff this lock is in an interrupted state.
     */
    public synchronized boolean isInterrupted() {
        return mInterrupted;
    }

    /**
     * Like calling {@code block(0)}. Will clear and throw any interrupts
     * without blocking.
     */
    public synchronized void check() throws InterruptedIOException {
        if( mInterrupted ) {
            mInterrupted = false;
            throw new InterruptedIOException();
        }
    }

    /**
     * Resets the interrupted status of this lock.
     *
     * @return true iff this lock was in an interrupted state when this method was called.
     */
    public synchronized boolean reset() {
        boolean ret  = mInterrupted;
        mInterrupted = false;
        return ret;
    }

}
