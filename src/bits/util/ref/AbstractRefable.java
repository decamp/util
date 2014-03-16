/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.util.ref;

/**
 * @author Philip DeCamp
 */
@SuppressWarnings( { "unchecked", "rawtypes" } )
public abstract class AbstractRefable implements Refable {

    protected final ObjectPool mPool;
    protected final Object mData;
    private int mRefCount = 1;

    
    public AbstractRefable() {
        mPool = null;
        mData = null;
    }

    public AbstractRefable( ObjectPool pool ) {
        mPool = pool;
        mData = this;
    }

    public AbstractRefable( ObjectPool pool, Object data ) {
        mPool = pool;
        mData = data;
    }
    
    
    public synchronized boolean ref() {
        mRefCount++;
        return true;
    }

    public void deref() {
        synchronized( this ) {
            if( --mRefCount > 0 ) {
                return;
            }
        
            if( mPool == null ) {
                mRefCount = 0;
            } else {
                mRefCount = 1;
            }
        }
        
        if( mPool == null || !mPool.offer( mData ) ) {
            mRefCount = 0;
            freeObject();
            return;
        }
    }

    public int refCount() {
        return mRefCount;
    }
    
    /**
     * Called from deref() when refCount reaches <= 0 and object is placed in pool.
     */
    protected abstract void freeObject();
    
}
