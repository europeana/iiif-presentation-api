package eu.europeana.api.record.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class JsonUtils {

	public static Integer asInteger(Object obj) {
	    return (obj != null && obj instanceof Integer ? (Integer)obj : null );
    }

	public static Long asLong(Object obj) {
	    if (obj == null ) { return null; }
	    if ( obj instanceof Long ) { return (Long)obj; }
	    if ( obj instanceof String ) { return Long.valueOf((String)obj); }
	    return null;
    }

	public static String asString(Object obj) {
		if ( obj == null ) { return null; }
		if ( obj instanceof String ) {
			String s = (String)obj;
			return ( StringUtils.isBlank(s) ? null : s );
		}
		if ( obj instanceof List ) {
			return asString(((List)obj).get(0));
		}
		if ( obj instanceof Map ) {
			return asString(((Map)obj).values().iterator().next());
		}
		return null;
	}

	public static Collection<Object> asCollection(Object obj) {
		if ( obj == null ) { return Collections.emptySet(); }
	
		if ( obj instanceof Collection ) { return (Collection<Object>)obj; }
		if ( obj instanceof Map        ) { return Collections.singleton(obj); }
		return Collections.emptySet();
	}

}
