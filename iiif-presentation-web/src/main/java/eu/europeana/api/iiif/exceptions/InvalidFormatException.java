package eu.europeana.api.iiif.exceptions;

import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import org.springframework.http.HttpStatus;

public class InvalidFormatException extends EuropeanaApiException {

    public InvalidFormatException(String msg) {
        super(msg);
    }

    @Override
    public boolean doLogStacktrace() {
        return false;
    }

    @Override
    public HttpStatus getResponseStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
