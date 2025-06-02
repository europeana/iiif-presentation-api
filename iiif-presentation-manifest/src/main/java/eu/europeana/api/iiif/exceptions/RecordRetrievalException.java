package eu.europeana.api.iiif.exceptions;

import org.springframework.http.HttpStatus;

import eu.europeana.api.commons_sb3.error.EuropeanaApiErrorResponse;
import eu.europeana.api.commons_sb3.error.EuropeanaApiException;

public class RecordRetrievalException extends EuropeanaApiException {

    private static final long serialVersionUID = 4916818362571684986L;

    private HttpStatus status;

    public RecordRetrievalException(EuropeanaApiErrorResponse rsp, int status) {
        super(rsp.getMessage(), rsp.getError(), rsp.getCode());
        this.status = HttpStatus.resolve(status);
    }

    public RecordRetrievalException(String msg, String error, String code, int status) {
        super(msg, error, code);
        this.status = HttpStatus.resolve(status);
    }

    public RecordRetrievalException(String msg, Throwable t) {
        super(msg, t);
    }

    public RecordRetrievalException(String msg) {
        super(msg);
    }

    @Override
    public boolean doLog() {
        return false;
    }

    @Override
    public HttpStatus getResponseStatus() {
        return status;
    }
}
