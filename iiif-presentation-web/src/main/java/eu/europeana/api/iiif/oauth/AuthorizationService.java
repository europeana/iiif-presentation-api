package eu.europeana.api.iiif.oauth;

import eu.europeana.api.commons.auth.AuthenticationHandler;
import eu.europeana.api.commons.auth.apikey.ApikeyBasedAuthentication;
import eu.europeana.api.commons.auth.token.StaticTokenAuthentication;
import eu.europeana.api.iiif.exceptions.AuthorizationException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.hc.core5.http.HttpHeaders;

/**
 * Authorization Service for IIIF presentation API
 * Extracts wskey or token if provided in the IIIF Collection requests
 * and wraps them into a AuthenticationHandler object for the client calls
 *
 * TODO - this needs to be replaced by api-commons-sb3 full authentication functionality.
 *      As that is yet not developed in the new commons api, this is a temporary class to handle authentication
 *      In Future the new functionality to verify access will return an Authentication Handler object.
 * @author Srishti Singh
 * @since 5 march 2025
 */
public class AuthorizationService {

    private static  final String Bearer = "Bearer";
    private static  final String APIKEY = "APIKEY";

    /**
     * TODO - temp method to fetch the apikey or token (if provided).
     *        This doesn't validates the apikey or token. Will be replaced by oauth functionality developed by Shweta
     * Returns the AuthenticationHandler based on if an apikey is provided or the token from the request
     *
     * @param request
     * @return
     * @throws AuthorizationException when no authentication is provided
     */
    public static AuthenticationHandler getAuthorization(
            HttpServletRequest request, AuthenticationHandler fallback) throws AuthorizationException {
        
        String apikey = extractApikey(request);
        if ( apikey != null) { return new ApikeyBasedAuthentication(apikey); }

        String token = extractToken(request);
        if (token != null) { return new StaticTokenAuthentication(token); }

        if ( fallback != null ) { return fallback; }

        throw new AuthorizationException("No authentication information provided, Authorization header or Api key should be provided !!!");
    }

    private static String extractApikey(HttpServletRequest request) throws AuthorizationException {
        String apikey = request.getParameter("wskey");
        if (apikey != null) { return apikey; }

        apikey = request.getHeader("X-Api-Key");
        if (apikey != null) { return apikey; }

        return extractPayloadFromAuthorizationHeader(request, APIKEY);
    }

    private static  String extractToken(HttpServletRequest request) throws AuthorizationException {
        return extractPayloadFromAuthorizationHeader(request, Bearer);
    }

    private static String extractPayloadFromAuthorizationHeader(HttpServletRequest request, String authorizationType) throws AuthorizationException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        return extractPayloadFromHeaderValue(authorizationType, authorization);
    }


    private static String extractPayloadFromHeaderValue(String authorizationType, String authorization) throws AuthorizationException {
        if (authorization == null) { return null; }
        
        if (authorization.startsWith(Bearer) || authorization.startsWith(APIKEY)) {
            return authorization.startsWith(authorizationType) ? authorization.substring(authorizationType.length()).trim() : null;
        }
        throw new AuthorizationException("Unsupported type in Auhtorization header: " + authorization);
    }
}
