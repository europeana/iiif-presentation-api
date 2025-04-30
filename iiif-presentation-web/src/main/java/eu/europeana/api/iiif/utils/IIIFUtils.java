package eu.europeana.api.iiif.utils;

import eu.europeana.api.commons_sb3.definitions.format.RdfFormat;
import eu.europeana.api.commons_sb3.definitions.iiif.IIIFDefinitions;
import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import eu.europeana.api.iiif.exceptions.InvalidFormatException;
import eu.europeana.api.iiif.service.IIIFVersionSupport;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;

import java.util.Arrays;
import java.util.Collection;

import static eu.europeana.api.iiif.utils.IIIFConstants.V2;

public class IIIFUtils {

    private static Collection<RdfFormat> validFormats 
        = Arrays.asList(RdfFormat.JSONLD, RdfFormat.JSON);

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
     * In IIIF presentaion api we only support json OR jsonld formats
     *
     * @param format
     * @return
     */
    private static boolean isValidFormat(RdfFormat format) {
        return validFormats.contains(format);
    }
}
