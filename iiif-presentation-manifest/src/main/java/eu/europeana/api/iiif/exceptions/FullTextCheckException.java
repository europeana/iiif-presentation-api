package eu.europeana.api.iiif.exceptions;

import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import org.springframework.http.HttpStatus;

/**
 * Exception that is thrown is there is a problem checking if a full-text exists (no 200 or 404 is returned)
 * @author Patrick Ehlert
 * Created on 15-08-2018
 */
public class FullTextCheckException extends EuropeanaApiException {

    private static final long serialVersionUID = 6496277864645695187L;

    public FullTextCheckException(String msg) {
        super(msg);
    }

    public FullTextCheckException(String msg, Throwable t) {
        super(msg, t);
    }

    /**
     * @return false because we don't want to explicitly log this type of exception
     */
    @Override
    public boolean doLog() {
        return false;
    }

    @Override
    public HttpStatus getResponseStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
