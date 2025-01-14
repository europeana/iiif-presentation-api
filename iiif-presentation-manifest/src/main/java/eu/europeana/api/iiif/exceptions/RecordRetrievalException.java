package eu.europeana.api.iiif.exceptions;

import eu.europeana.api.commons_sb3.error.EuropeanaApiException;

public class RecordRetrievalException extends EuropeanaApiException {

    private static final long serialVersionUID = 4916818362571684986L;

    public RecordRetrievalException(String msg, Throwable t) {
        super(msg, t);
    }

    public RecordRetrievalException(String msg) {
        super(msg);
    }
}
