package eu.europeana.api.iiif.exceptions;

import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import eu.europeana.api.commons_sb3.error.EuropeanaI18nApiException;
import org.springframework.http.HttpStatus;

public class CollectionException extends EuropeanaI18nApiException {

    public CollectionException(String msg) {
        super(msg, "errorcode", "error", "error.token_param", null);
    }

   // public CollectionException(String msg, Throwable t) {
//        super(msg, t);
//    }

    @Override
    public boolean doLogStacktrace() {
        return false;
    }

    @Override
    public HttpStatus getResponseStatus() {
        return HttpStatus.SERVICE_UNAVAILABLE;
    }
}

