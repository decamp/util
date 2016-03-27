/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.util.ref;

/**
 * @author Philip DeCamp
 */
public interface ObjectPool<T> {
    public boolean offer( T obj );
    public T poll();
}
