package eu.europeana.api.record.serialization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class JsonUtils {
	private JsonUtils(){}
	public static Integer asInteger(Object obj) {
	    return (obj instanceof Integer value ? value : null );
    }

	public static Long asLong(Object obj) {
	    if (obj == null ) { return null; }
	    if ( obj instanceof Long val ) { return val; }
	    if ( obj instanceof String val ) { return Long.valueOf(val); }
	    return null;
    }

	public static String asString(Object obj) {
		if ( obj == null ) { return null; }
		if ( obj instanceof String s ) {
			return ( StringUtils.isBlank(s) ? null : s );
		}
		if ( obj instanceof List list) {
			return asString((list).get(0));
		}
		if ( obj instanceof Map map) {
			return asString((map).values().iterator().next());
		}
		return null;
	}

	public static List<String> asListString(Object obj) {
		if ( obj == null ) { return Collections.emptyList(); }

		if ( obj instanceof Collection ) { 
			Collection<Object> col = (Collection<Object>)obj; 
			List<String> ret = new ArrayList<>(col.size());
			for ( Object o : col ) { ret.add(asString(o)); }
			return ret;
		}
		if ( obj instanceof Map mapObj ) {
			return asListString(mapObj.values());
		}
		return Collections.emptyList();
	}

	public static Collection<Object> asCollection(Object obj) {
		if ( obj == null ) { return Collections.emptySet(); }
	
		if ( obj instanceof Collection ) { return (Collection<Object>)obj; }
		if ( obj instanceof Map        ) { return Collections.singleton(obj); }
		return Collections.emptySet();
	}

}