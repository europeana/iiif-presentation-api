package eu.europeana.api.iiif.exceptions;

import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import org.springframework.http.HttpStatus;


public class InvalidArgumentException extends EuropeanaApiException {

    private static final long serialVersionUID = 6920934255738422247L;

    public InvalidArgumentException(String msg) {
        super(msg);
    }
    public InvalidArgumentException(String msg, Throwable t) {
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
        return HttpStatus.BAD_REQUEST;
    }
}
