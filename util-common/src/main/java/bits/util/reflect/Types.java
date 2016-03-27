/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.util.reflect;


public class Types {
    
    private static final float COST_BOXING     = 1000f;
    private static final float COST_SUPERCLASS = 1f;
    private static final float COST_INTERFACE  = 0.25f;
    
    
    public static Class<?> toNonPrimitive( Class<?> clazz ) {
        if( !clazz.isPrimitive() ) {
            return clazz;
        }
        Class<?> primClazz = box( clazz );
        return primClazz != null ? primClazz : clazz;
    }
    
    
    public static Class<?> box( Class<?> primType ) {
        if( primType == int.class ) {
            return Integer.class;
        } else if( primType == boolean.class ) {
            return Boolean.class;
        } else if( primType == float.class ) {
            return Float.class;
        } else if( primType == double.class ) {
            return Double.class;
        } else if( primType == long.class ) {
            return Long.class;
        } else if( primType == char.class ) {
            return Character.class;
        } else if( primType == byte.class ) {
            return Byte.class;
        } else if( primType == short.class ) {
            return Short.class;
        } else {
            return null;
        }
    }

    
    public static Class<?> unbox( Class<?> wrapperType ) {
        if( wrapperType == Integer.class ) {
            return int.class;
        } else if( wrapperType == Boolean.class ) {
            return boolean.class;
        } else if( wrapperType == Float.class ) {
            return float.class;
        } else if( wrapperType == Double.class ) {
            return double.class;
        } else if( wrapperType == Long.class ) {
            return long.class;
        } else if( wrapperType == Character.class ) {
            return char.class;
        } else if( wrapperType == Byte.class ) {
            return byte.class;
        } else if( wrapperType == Short.class ) {
            return short.class;
        } else {
            return null;
        }
    }
    

    public static boolean canAssign( Class<?> src, Class<?> dst ) {
        return !Float.isInfinite( assignmentCost( src, dst ) );
    }
    
    
    public static float assignmentCost( Class<?> src, Class<?> dst ) {
        if( src == dst ) {
            return 0f;
        }
        
        float cost = 0.0f;
        
        // Assignment from null.
        if( src == void.class ) {
            if( dst.isPrimitive() ) {
                return Float.POSITIVE_INFINITY;
            }
            if( dst.isInterface() ) {
                cost += COST_INTERFACE;
            }
            while( dst != null ) {
                dst = dst.getSuperclass();
                cost += COST_SUPERCLASS;
            }
            return cost;
        }
        
        // Assignment to primitive.
        if( dst.isPrimitive() ) {
            if( !src.isPrimitive() ) {
                src = unbox( src );
                if( src == null ) {
                    return Float.POSITIVE_INFINITY;
                }                
                cost += COST_BOXING;
            }
            
            if( src == int.class ) {
                return cost + ( 
                       dst == int.class    ? 0.00f :
                       dst == long.class   ? 0.01f :
                       dst == float.class  ? 0.02f :
                       dst == double.class ? 0.03f : 
                       Float.POSITIVE_INFINITY );
            }

            if( src == boolean.class ) {
                return dst == boolean.class ? cost : Float.POSITIVE_INFINITY;
            }

            if( src == float.class ) {
                return cost + (
                       dst == float.class  ? 0.00f :
                       dst == double.class ? 0.01f : 
                       Float.POSITIVE_INFINITY );
            }

            if( src == double.class ) {
                return dst == double.class ? cost : Float.POSITIVE_INFINITY;
            }

            if( src == long.class ) {
                return cost + (
                       dst == long.class   ? 0.00f :
                       dst == float.class  ? 0.01f :
                       dst == double.class ? 0.02f : 
                       Float.POSITIVE_INFINITY );
            }

            if( src == char.class ) {
                return cost + (
                       dst == char.class   ? 0.00f :
                       dst == int.class    ? 0.01f :
                       dst == long.class   ? 0.02f :
                       dst == float.class  ? 0.03f :
                       dst == double.class ? 0.04f :
                       Float.POSITIVE_INFINITY );
            }

            if( src == byte.class ) {
                return cost + (
                       dst == byte.class   ? 0.00f :
                       dst == short.class  ? 0.01f :
                       dst == int.class    ? 0.02f :
                       dst == long.class   ? 0.03f :
                       dst == float.class  ? 0.04f :
                       dst == double.class ? 0.05f :
                       Float.POSITIVE_INFINITY );
            }

            if( src == short.class ) {
                return cost + (
                       dst == short.class  ? 0.00f :
                       dst == int.class    ? 0.01f :
                       dst == long.class   ? 0.02f :
                       dst == float.class  ? 0.03f :
                       dst == double.class ? 0.04f :
                       Float.POSITIVE_INFINITY );
            }
            
            // Should never get here.
            return Float.POSITIVE_INFINITY;
        } // dst.isPrimitive()
        
        if( src.isPrimitive() ) {
            // Widenening conversions do not occur on boxing.
            src = box( src );
            cost += COST_BOXING;
        }
        
        if( !dst.isAssignableFrom( src ) ) {
            return Float.POSITIVE_INFINITY;
        }
        
        if( dst.isArray() ) {
            // If classes are arrays, determine cost
            // by component type of array.
            src = src.getComponentType();
            dst = dst.getComponentType();
        }
        
        if( dst.isInterface() ) {
            return cost + COST_INTERFACE;
        }
        
        while( src != null && src != dst ) {
            cost += COST_SUPERCLASS;
            src = src.getSuperclass();
        }
        
        return cost;
    }
    
}
