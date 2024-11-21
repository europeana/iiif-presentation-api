package eu.europeana.api.iiif.utils;

import eu.europeana.api.commons_sb3.definitions.format.RdfFormat;
import eu.europeana.api.iiif.exceptions.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;

public class IIIFUtils {

    /**
     * Retuns RDF Format if the extension id either json or jsonld
     * @param setId
     * @return
     */
    public static RdfFormat getRDFFormatFromId(String setId) throws InvalidFormatException {
        if (StringUtils.contains(setId, ".")) {
            RdfFormat format = RdfFormat.getFormatByExtension(StringUtils.substringAfter(setId, "."));
            if (isValidFormat(format)) {
                return format;
            } else {
                throw new InvalidFormatException("Invalid format !! Valid extensions .json or .jsonld");
            }
        }
        return null;
    }

    /**
     * Returns RdfFormat if the ACCEPT header is provided
     * @param request
     * @return
     */
    public static RdfFormat getRDFFormatFromHeader(HttpServletRequest request) {
        RdfFormat format = RdfFormat.getFormatByMediaType(request.getHeader(HttpHeaders.ACCEPT));
        if (isValidFormat(format)) {
            return format;
        }
        return null;
    }

    /**
     * In IIIF presentaion api we only support json OR jsonld formats
     * @param format
     * @return
     */
    private static boolean isValidFormat(RdfFormat format) {
        return RdfFormat.JSONLD.equals(format) || RdfFormat.JSON.equals(format);
    }
}
