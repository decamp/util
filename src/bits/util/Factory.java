/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */
package bits.util;

/**
 * @author Philip DeCamp
 */
public interface Factory<T> {
    public T create();
}
