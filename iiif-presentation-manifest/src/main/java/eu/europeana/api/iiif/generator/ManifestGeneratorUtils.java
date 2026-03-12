package eu.europeana.api.iiif.generator;


import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.digest.DigestUtils;

import com.jayway.jsonpath.JsonPath;

import eu.europeana.api.iiif.utils.LanguageMapUtils;
import eu.europeana.api.iiif.v3.model.LanguageMap;
import eu.europeana.api.record.model.WebResource;
import org.springframework.stereotype.Component;

@Component
public class ManifestGeneratorUtils {
    ManifestSettings settings;
    public ManifestGeneratorUtils(ManifestSettings settings) {
        this.settings = settings;
    }
    public String getThumbnailV2(WebResource wr) {
        String url = URLEncoder.encode(wr.getId(), StandardCharsets.UTF_8);
        return (settings.getThumbnailApiUrl() + url
               + "&type=" + wr.getMediaType().getCategory().name() );
    }
    /**
     * We first check all proxies for a title. If there are no titles, then we check the description fields
     * @param jsonDoc parsed json document
     * @return
     */
    public static LanguageMap getLabels(Object jsonDoc)  {
        LanguageMap[] maps = JsonPath.parse(jsonDoc).read("$.object.proxies[*].dcTitle", LanguageMap[].class);
        if (maps == null || maps.length == 0) {
            maps = JsonPath.parse(jsonDoc).read("$.object.proxies[*].dcDescription", LanguageMap[].class);
        }
        return LanguageMapUtils.mergeLanguageMaps(maps);
    }

    /**
     * Returns the values from the proxy.dcDescription fields, but only if they aren't used as a label yet.
     * @param jsonDoc parsed json document
     * @return
     */
    public static LanguageMap getDescription(Object jsonDoc) {
        if (JsonPath.parse(jsonDoc).read("$.object.proxies[*].dcTitle", LanguageMap[].class).length > 0) {
            return LanguageMapUtils.mergeLanguageMaps(JsonPath.parse(jsonDoc).read("$.object.proxies[*].dcDescription", LanguageMap[].class));
        }
        return null;
    }

    /**
     * Derives PageID from a media URL.
     *
     * @param mediaUrl media (target) url
     * @return MD5 hash of media url truncated to the first 5 characters
     */
    public static String derivePageId(String mediaUrl) {
        // truncate hash to reduce URL length.
        // Should not be changed as this method can be used in place of fetching the pageId from the
        // database.
        return DigestUtils.sha1Hex(mediaUrl).substring(0,7);
    }
}