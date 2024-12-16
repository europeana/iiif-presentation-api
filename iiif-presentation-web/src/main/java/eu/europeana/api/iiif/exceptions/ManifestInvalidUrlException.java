package eu.europeana.api.iiif.exceptions;

import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import org.springframework.http.HttpStatus;

public class ManifestInvalidUrlException extends EuropeanaApiException  {

        public ManifestInvalidUrlException(String message) {
            super(message);
        }

        @Override
        public boolean doLog() {
            return false;
        }

        @Override
        public HttpStatus getResponseStatus() {
            return HttpStatus.BAD_REQUEST;
        }
}
