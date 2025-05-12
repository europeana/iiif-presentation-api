package eu.europeana.api.iiif.utils;

import eu.europeana.api.commons_sb3.definitions.format.RdfFormat;
import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import eu.europeana.api.iiif.exceptions.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;

import java.util.Arrays;
import java.util.Collection;

public class IIIFUtils {

    public static final String IIIF_VERSION_RDF_FORMAT = "IIIF Version {} , RDF format {}";

    private static Collection<RdfFormat> validFormats = Arrays.asList(RdfFormat.JSONLD, RdfFormat.JSON);

    private IIIFUtils() {
        // private to hide implicit one
    }

    public static String getId(String path) {
        int i = path.indexOf(".");
        return ( i < 0 ? path : path.substring(0,i) );
    }

    public static RdfFormat getFormat(HttpServletRequest request) 
            throws EuropeanaApiException {
        String path = request.getRequestURI();

        int i = path.indexOf(".");
        if ( i < 0 ) { return getFormatFromHeader(request, RdfFormat.JSONLD); }
        
        
        RdfFormat format = RdfFormat.getFormatByExtension(path.substring(i+1));
        if ( isValidFormat(format) ) { return format; }

        throw new InvalidFormatException("Invalid format !! Valid extensions .json or .jsonld");
    }


    /**
     * Returns RdfFormat if the ACCEPT header is provided
     * by default returns JSONLD RDF Format (even if the format provided is invalid)
     *
     * @param request
     * @return
     */
    public static RdfFormat getFormatFromHeader(HttpServletRequest request
                                              , RdfFormat def) {
        RdfFormat format = null;
        for (String header : Arrays.asList(request.getHeader(HttpHeaders.ACCEPT).split(";"))) {
            format = RdfFormat.getFormatByMediaType(header);
            if (format != null) { break; }
        }
        if ( format == null ) { return def; }

        return (isValidFormat(format) ? format : def);
    }

    /**
     * IIIF presentation api we only support json OR jsonld formats
     *
     * @param format
     * @return
     */
    private static boolean isValidFormat(RdfFormat format) {
        return validFormats.contains(format);
    }
}
