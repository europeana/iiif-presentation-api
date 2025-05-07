package eu.europeana.api.iiif.exceptions;

import eu.europeana.api.commons_sb3.error.EuropeanaGlobalExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler that catches all errors and logs the interesting ones
 *
 * @author Srishti Singh
 * Created on 20-11-2024
 */
@RestControllerAdvice
class GlobalExceptionHandler extends EuropeanaGlobalExceptionHandler {

}
