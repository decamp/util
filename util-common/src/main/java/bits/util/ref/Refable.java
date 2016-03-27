/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.util.ref;


/**
 * Interface for counting references to a given object. Refable objects are 
 * useful for providing high efficiency memory reuse when used with a RefPool,
 * at the cost of implementation overhead for explicitly updating reference
 * counts when passing or nullifying pointers to the object.
 * <p>
 * Objects that implement Refable should begin with a reference count of 1 at
 * initialization. Any thread/object that receives a reference to the Refable
 * SHOULD call "ref()" to increment the ref count. 
 * When the thread/object is finished with the refObject, it SHOULD call
 * {@code deref()} to decrement the ref count.  Once the refCount reaches zero, the
 * Refable object is disposed accordingly.
 * <p>
 * Example:
 * <br><pre>{@code
 * Pool<Thingy> pool = new RefPool<Thingy>();
 * Refable r = new Thingy(pool); <br>
 * <br>
 * //Increase reference count to 2 <br>
 * r.ref(); <br>
 * <br>
 * //Remove all references. <br>
 * r.deref(); <br>
 * r.deref(); <br>
 * }</pre>
 * 
 * @author Philip DeCamp  
 */
public interface Refable {
    
    /**
     * Increments the refCount of this object.
     * 
     * @return true if refCount increased. False if object is no longer valid.
     */
    public boolean ref();
    
    /**
     * Decrements the refCount of this object.
     */
    public void deref();

    /**
     * @return the refCount for this object.
     */
    public int refCount();

}
