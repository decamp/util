/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.util.ref;

import java.lang.ref.*;
import java.util.*;


public class SoftPool<T> implements ObjectPool<T> {

    private final List<Reference<T>> mQueue = new ArrayList<Reference<T>>();
    private int mCapacity;

    public SoftPool() {
        mCapacity = -1;
    }

    public SoftPool( int capacity ) {
        mCapacity = capacity;
    }


    public synchronized boolean offer( T obj ) {
        if( mCapacity >= 0 && mQueue.size() >= mCapacity ) {
            return false;
        }
        Reference<T> ref = new SoftReference<T>( obj );
        mQueue.add( ref );
        return true;
    }

    public synchronized T poll() {
        T ret = null;

        for( int s = mQueue.size() - 1; s >= 0; s-- ) {
            Reference<T> ref = mQueue.remove( s );
            ret = ref.get();
            if( ret != null ) {
                return ret;
            }
        }
        return null;
    }

    public synchronized void close() {
        if( mCapacity == 0 ) {
            return;
        }
        mCapacity = 0;
        mQueue.clear();
    }
}
