package eu.europeana.api.iiif.exceptions;

import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import org.springframework.http.HttpStatus;

public class CollectionException extends EuropeanaApiException {

    private int remoteStatusCode;

    public CollectionException(String msg) {
        super(msg);
    }

    public CollectionException(String msg, Throwable t) {
        super(msg, t);
    }

    public CollectionException(String msg, int remoteStatusCode, Throwable t) {
        super(msg, t);
        this.remoteStatusCode = remoteStatusCode;
    }

    @Override
    public boolean doLogStacktrace() {
        return false;
    }

    @Override
    public HttpStatus getResponseStatus() {
        return remoteStatusCode == 0 ? HttpStatus.SERVICE_UNAVAILABLE : HttpStatus.valueOf(remoteStatusCode);
    }
}

