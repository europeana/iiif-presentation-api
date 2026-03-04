package eu.europeana.api.record.model;

import java.util.HashMap;


public class SvcsService extends HashMap<String, Object> implements RecordConstants {

	public SvcsService() {}

    /**
     * @return the id of the service (in 'about' value)
     */
    public String getId() {
        return JsonUtils.asString(get(ABOUT));
    }

	public String getImplements() { 
		return JsonUtils.asString(get(DOAP_IMPLEMENTS));
	}

	public String getConformsTo() { 
		return JsonUtils.asString(get(DCTERMS_CONFORMS_TO));
	}

	public String getLabel() {
		return JsonUtils.asString(get(RDFS_LABEL));
	}
}
