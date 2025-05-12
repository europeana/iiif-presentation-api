package eu.europeana.api.iiif.exceptions;

import java.io.IOException;

import eu.europeana.api.commons_sb3.error.EuropeanaApiException;

/**
 * Exception that is thrown is there is a problem parsing or serializing an exception
 * @author Patrick Ehlert
 * Created on 26-01-2018
 */
public class RecordParseException extends IOException {

    private static final long serialVersionUID = 1007865165313316802L;

    public RecordParseException(String msg, Throwable t) {
        super(msg, t);
    }

}
