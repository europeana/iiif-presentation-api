package eu.europeana.api.iiif.utils;

import eu.europeana.api.commons_sb3.definitions.format.RdfFormat;
import eu.europeana.api.commons_sb3.definitions.iiif.IIIFDefinitions;
import eu.europeana.api.commons_sb3.error.exceptions.InvalidIdException;
import eu.europeana.api.iiif.model.IIIFResource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;

import java.util.Arrays;
import static eu.europeana.api.iiif.utils.IIIFConstants.V2;

public class IIIFUtils {

    /**
     * Returns RdfFormat if the ACCEPT header is provided
     *
     * @param request
     * @return
     */
    public static RdfFormat getRDFFormatFromHeader(HttpServletRequest request) {
        RdfFormat format = null;
        for (String header : Arrays.asList(request.getHeader(HttpHeaders.ACCEPT).split(";"))) {
            format = RdfFormat.getFormatByMediaType(header);
            if (format != null) {
                break;
            }
        }
        if (isValidFormat(format)) {
            return format;
        }
        // if accept header was empty default to JSONLD
        else if (format == null) {
            return RdfFormat.JSONLD;
        }
        return null;
    }

    /**
     * Retuns RDF Format if the extension id either json or jsonld
     *
     * @param setId
     * @return
     */
    public static RdfFormat getRDFFormatFromId(String setId) throws InvalidIdException {
        if (StringUtils.contains(setId, ".")) {
            RdfFormat format = RdfFormat.getFormatByExtension(StringUtils.substringAfter(setId, "."));
            if (isValidFormat(format)) {
                return format;
            } else {
                throw new InvalidIdException(IIIFResource.class, Arrays.asList(RdfFormat.JSONLD  + " OR " + RdfFormat.JSON));
            }
        }
        return null;
    }

    /**
     * In IIIF presentaion api we only support json OR jsonld formats
     *
     * @param format
     * @return
     */
    private static boolean isValidFormat(RdfFormat format) {
        return RdfFormat.JSONLD.equals(format) || RdfFormat.JSON.equals(format);
    }

    public static HttpHeaders addContentType(RdfFormat format, String iiifVersion) {
        HttpHeaders headers = new HttpHeaders();
        if (format.equals(RdfFormat.JSON)) {
            if (StringUtils.equals(iiifVersion, V2)) {
                headers.add(HttpHeaders.CONTENT_TYPE, IIIFDefinitions.MEDIA_TYPE_IIIF_JSON_V2);
            } else {
                headers.add(HttpHeaders.CONTENT_TYPE, IIIFDefinitions.MEDIA_TYPE_IIIF_JSON_V3);

            }
        } else {
            if (StringUtils.equals(iiifVersion, V2)) {
                headers.add(HttpHeaders.CONTENT_TYPE, IIIFDefinitions.MEDIA_TYPE_IIIF_JSONLD_V2);
            } else {
                headers.add(HttpHeaders.CONTENT_TYPE, IIIFDefinitions.MEDIA_TYPE_IIIF_JSONLD_V3);

            }
        }
        return headers;
    }
}
