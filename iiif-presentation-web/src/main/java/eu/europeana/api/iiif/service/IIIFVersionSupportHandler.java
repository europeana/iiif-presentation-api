/**
 * 
 */
package eu.europeana.api.iiif.service;

import static eu.europeana.api.commons_sb3.definitions.iiif.AcceptUtils.*;

import java.util.HashMap;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;

import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import eu.europeana.api.iiif.exceptions.InvalidIIIFVersionException;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Hugo
 * @since 7 Apr 2025
 */
public class IIIFVersionSupportHandler {

    private static String def = eu.europeana.api.iiif.v2.io.JsonConstants.CONTEXT_URI;

    private HashMap<String,IIIFVersionSupport> map = new HashMap<>();


    public void register(IIIFVersionSupport support) {
        map.put(support.getVersionId(), support);
    }

    public IIIFVersionSupport getDefaultVersionSupport() 
            throws EuropeanaApiException {
        return getVersionSupport(def);
    }


    public IIIFVersionSupport getVersionSupport(String iiifVersion) 
            throws EuropeanaApiException {
        IIIFVersionSupport support = map.get(iiifVersion);
        if ( support != null ) { return support; }

        throw new InvalidIIIFVersionException(iiifVersion);
    }

    /**
     * Retrieve the requested version from accept header if present
     * OR if not present, check the provided format parameter value.
     * If nothing is specified then 2 is returned as default
     * @param request the incoming httpservletrequest to process
     * @param format the format parameter value (if available, can be null is not provided)
     * @return either version 2, 3 or null (if invalid)
     */
    public IIIFVersionSupport getVersionSupport(HttpServletRequest request) 
            throws EuropeanaApiException {
        String accept = request.getHeader(ACCEPT);
        if (StringUtils.isNotEmpty(accept)) {
            Matcher m = ACCEPT_PROFILE_PATTERN.matcher(accept);
         // found a Profile parameter in the Accept header
            if (m.find()) { return getVersionSupport(m.group(1)); }
        }
        IIIFVersionSupport version = getVersionFromParameter(request);
        return ( version != null ? version : getDefaultVersionSupport() );
    }

    /**
     * Checks if the IIIF version is requested via the format parameter.
     *
     * @param request
     * @return
     */
    private IIIFVersionSupport getVersionFromParameter(HttpServletRequest request) 
            throws EuropeanaApiException {
        String version = request.getParameter("format");
        if (StringUtils.isBlank(version)) { return null; }

        for ( IIIFVersionSupport support : map.values() ) {
            if ( version.equals(support.getVersionNr()) ) { return support; }
        }
        
        throw new InvalidIIIFVersionException(version);
    }

}
