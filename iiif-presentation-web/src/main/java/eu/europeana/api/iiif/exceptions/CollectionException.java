package eu.europeana.api.iiif.exceptions;

import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import org.springframework.http.HttpStatus;

public class CollectionException extends EuropeanaApiException {

    public CollectionException(String msg, Throwable t) {
        super(msg, t);
    }

    @Override
    public boolean doLogStacktrace() {
        return false;
    }

    @Override
    public HttpStatus getResponseStatus() {
        return HttpStatus.SERVICE_UNAVAILABLE;
    }
}

