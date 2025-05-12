package eu.europeana.api.iiif.exceptions;

import eu.europeana.api.commons_sb3.error.EuropeanaApiException;

public class ManifestException extends EuropeanaApiException {

    private static final long serialVersionUID = 4916818362571684986L;

    /**
     * Initialise a new exception
     * @param msg error message
     * @param t root cause exception
     */
    public ManifestException(String msg, Throwable t) {
        this(msg, null, t);
    }

    /**
     * Initialise a new exception with error code
     * @param msg error message
     * @param errorCode error code (optional)
     * @param t root cause exception
     */
    public ManifestException(String msg, String errorCode, Throwable t) {
        super(msg, null, errorCode, t);
    }

    /**
     * Initialise a new exception for which there is no root cause
     * @param msg error message
     */
    public ManifestException(String msg) {
        super(msg);
    }

    /**
     * Initialise a new exception with error code for which there is no root cause
     * @param msg error message
     * @param errorCode error code (optional)
     */
    public ManifestException(String msg, String errorCode) {
        super(msg, null, errorCode);
    }
}
