package eu.europeana.api.iiif.exceptions;

import eu.europeana.api.commons_sb3.error.EuropeanaApiException;

/**
 * Error that is thrown if we found a data inconsistency (e.g. unexpected loops or items not found in the webresource isNextInSequence)
 * @author Patrick Ehlert
 * Created on 08-03-2018
 */
public class DataInconsistentException extends EuropeanaApiException {

    private static final long serialVersionUID = -3521815914068605324L;

    public DataInconsistentException(String msg) {
        super(msg);
    }

}
