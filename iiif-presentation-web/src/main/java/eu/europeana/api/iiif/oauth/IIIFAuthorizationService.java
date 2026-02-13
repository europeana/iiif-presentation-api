package eu.europeana.api.iiif.oauth;

import eu.europeana.api.commons_sb3.auth.AuthenticationHandler;
import eu.europeana.api.commons_sb3.auth.apikey.ApikeyBasedAuthentication;
import eu.europeana.api.commons_sb3.auth.token.StaticTokenAuthentication;
import eu.europeana.api.commons_sb3.exception.ApiKeyExtractionException;
import eu.europeana.api.commons_sb3.exception.AuthorizationExtractionException;
import eu.europeana.api.commons_sb3.oauth2.utils.OAuthUtils;
import eu.europeana.api.iiif.exceptions.AuthorizationException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import static eu.europeana.api.iiif.utils.IIIFConstants.BEAN_FALLBACK_AUTHORIZATION;

/**
 * Authorization Service for IIIF presentation API
 *
 * @author Srishti Singh
 * @since 5 march 2025
 */
@SuppressWarnings("java:S115")
@Service
public class IIIFAuthorizationService {

    private IIIFAuthorizationService() {
        //to hide implicit one
    }

    @Resource(name = BEAN_FALLBACK_AUTHORIZATION)
    AuthenticationHandler authFallback;

    /**
     * Returns the AuthenticationHandler based on if an apikey is provided or the token from the request
     *
     * Note if apikey or token is not provided, fallback authentication is returned
     *
     * @param request
     * @return
     * @throws AuthorizationException when no authentication is provided
     */
    public AuthenticationHandler getAuthorization(HttpServletRequest request) throws AuthorizationException {
        String apikey = extractApikey(request);
        if (apikey != null) {
            return new ApikeyBasedAuthentication(apikey);
        }

        String token = extractToken(request);
        if (token != null) {
            return new StaticTokenAuthentication(token);
        }
        if (authFallback == null) {
            throw new AuthorizationException("No Authentication information provided in the request!! Also, IIIF Fallback authentication is NOT provided");
        }
        return authFallback;
    }

    /**
     * Extracts apikey from the provided request.
     * This is just an extraction and NOT the validation of apikey,
     * hence all the exception thrown should be neglected as - if apikey or token
     * is not present in the request a fallback Authentication is passed to the third api's request
     *
     * @param request
     * @return
     * @throws ApiKeyExtractionException
     */
    private static String extractApikey(HttpServletRequest request) {
        try {
            return OAuthUtils.extractApiKey(request);
        } catch (AuthorizationExtractionException | ApiKeyExtractionException e) {
            return null;
        }
    }

    /**
     * Extracts token from the provided request.
     * This is just an extraction and NOT the validation of token,
     * hence all the exception thrown should be neglected as - if apikey or token
     * is not present in the request a fallback Authentication is passed to the third api's request
     *
     * @param request
     * @return
     * @throws ApiKeyExtractionException
     */
    private static String extractToken(HttpServletRequest request) {
        try {
            return OAuthUtils.extractPayloadFromAuthorizationHeader(request, OAuthUtils.TYPE_BEARER);
        } catch (AuthorizationExtractionException | ApiKeyExtractionException e) {
            return null;
        }
    }
}
