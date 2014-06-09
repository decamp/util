/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.util.ref;

import java.util.*;


public class HardPool<T> implements ObjectPool<T> {

    private final List<T> mQueue = new ArrayList<T>();
    private int mCapacity;

    public HardPool() {
        mCapacity = -1;
    }

    public HardPool( int capacity ) {
        mCapacity = capacity;
    }


    public synchronized boolean offer( T obj ) {
        if( mCapacity >= 0 && mQueue.size() >= mCapacity ) {
            return false;
        }
        mQueue.add( obj );
        return true;
    }

    public synchronized T poll() {
        int s = mQueue.size();
        if( s == 0 ) {
            return null;
        }
        return mQueue.remove( s - 1 );
    }

    public synchronized void close() {
        if( mCapacity == 0 ) {
            return;
        }
        mCapacity = 0;
        mQueue.clear();
    }

}
