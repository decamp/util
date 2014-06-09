/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */
package bits.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * A class for time utilities.
 * @author kubat
 *
 */
public class Dates {

    public static final TimeZone ZONE_UTC         = TimeZone.getTimeZone( "UTC" );
    public static final TimeZone ZONE_US_PACIFIC  = TimeZone.getTimeZone( "US/Pacific" );
    public static final TimeZone ZONE_US_MOUNTAIN = TimeZone.getTimeZone( "US/Mountain" );
    public static final TimeZone ZONE_US_CENTRAL  = TimeZone.getTimeZone( "US/Central" );
    public static final TimeZone ZONE_US_EASTERN  = TimeZone.getTimeZone( "US/Eastern" );
    
    public static final String FORMAT_SECOND               = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_MILLISECOND          = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String FORMAT_SECOND_TIMEZONE      = "yyyy-MM-dd HH:mm:ss z";
    public static final String FORMAT_MILLISECOND_TIMEZONE = "yyyy-MM-dd HH:mm:ss.SSS z";
    
    public static final DateFormat FORMAT_SECOND_UTC       = formatter( FORMAT_SECOND,      ZONE_UTC ); 
    public static final DateFormat FORMAT_MILLISECOND_UTC  = formatter( FORMAT_MILLISECOND, ZONE_UTC );
    
    
    public static DateFormat formatter( String format, TimeZone tz ) {
        SimpleDateFormat result = new SimpleDateFormat( format );
        result.setTimeZone( tz );
        return result;
    }
    
    public static DateFormat localFormatter( String format ) {
        return formatter( format, TimeZone.getDefault() );
    }
    
    public static DateFormat secondFormatter( TimeZone tz ) {
        return formatter( FORMAT_SECOND_TIMEZONE, tz );
    }
    
    public static DateFormat millisecondFormatter( TimeZone tz ) {
        return formatter( FORMAT_MILLISECOND_TIMEZONE, tz );
    }
    
}