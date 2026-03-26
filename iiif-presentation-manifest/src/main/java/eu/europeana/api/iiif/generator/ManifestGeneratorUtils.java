package eu.europeana.api.iiif.generator;


import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.europeana.api.commons_sb3.definitions.iiif.IIIFDefinitions;
import eu.europeana.api.record.model.Proxy;
import eu.europeana.api.record.model.RecordUtils;
import eu.europeana.api.record.model.WebResource;

import static eu.europeana.api.iiif.generator.ManifestGeneratorConstants.*;

public class ManifestGeneratorUtils extends RecordUtils {

    protected static final Map<String, String> CONFORMS_TO_SERVICE = new HashMap<>();

    // TODO make EdmDateStringToDate handle more different date strings (see EA-990)

    private static final Logger LOG = LogManager.getLogger(ManifestGeneratorUtils.class);

    private static final DateTimeFormatter DATE_YEARFIRST = DateTimeFormatter.ofPattern(
        "yyyy-MM-dd");
    private static final DateTimeFormatter DATE_YEARLAST = DateTimeFormatter.ofPattern(
        "dd-MM-yyyy");

    static {
        CONFORMS_TO_SERVICE.put(SERVICE_TYPE_IMAGE, IMAGE_SERVICE_TYPE_3);
        CONFORMS_TO_SERVICE.put(SERVICE_TYPE_EMBED, EMBED_SERVICE_TYPE);
    }

    /**
     * Naive check if the provide string is an url
     * @param s the url to check
     * @return true if we consider it an url, otherwise false
     */
    public static boolean isUrl(String s) {
        return StringUtils.startsWithIgnoreCase(s, "http://")
            || StringUtils.startsWithIgnoreCase(s, "https://")
            || StringUtils.startsWithIgnoreCase(s, "ftp://")
            || StringUtils.startsWithIgnoreCase(s, "file://");
    }

    /**
     * Return the first dctermsIssued date we can find in a proxy
     * Note that we assume that the desired value is in a mapping with a 'def' key
     * @param proxy proxy Object
     * @return date string in xsd:datetime format (i.e. YYYY-MM-DDThh:mm:ssZ)
     */
    public static String getNavDate(Proxy proxy) {
        LocalDate navDate = null;
        for (List<String> dates : proxy.getIssued().values()) {
            for (String date : dates) {
                navDate = dateStringToDate(date);
                if (navDate != null) {
                    break;
                }
            }
        }

        if (navDate == null) {
            return null;
        }
        ZonedDateTime zdt = Timestamp.valueOf(navDate.atStartOfDay()).toLocalDateTime()
            .atZone(ZoneOffset.UTC);
        return zdt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }


    public static String getThumbnailV2(ManifestSettings settings, WebResource wr) {
        String url = URLEncoder.encode(wr.getId(), StandardCharsets.UTF_8);
        return (settings.getThumbnailApiUrl() + url
            + "&type=" + wr.getMediaType().getCategory().name());
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
        return DigestUtils.sha1Hex(mediaUrl).substring(0, 7);
    }

    public static String getServiceType(String conformsTo) {
        return CONFORMS_TO_SERVICE.get(conformsTo);
    }

    /**
     * /presentation/{europeanaID}/annopage/
     * @param europeanaId europeana id
     * @return fulltext summary path
     */
    public static String getFulltextSummaryPath(String europeanaId) {
        return IIIFDefinitions.PRESENTATION_PATH + europeanaId
            + IIIFDefinitions.FULLTEXT_SUMMARY_PATH
            + "/"; // for now trailing slash is needed
    }

    /**
     * This converts an EDM date string to a LocalDate object (so no timezone information is available)
     * Note that there can be a lot of different format, most of which we don't support yet
     * See also https://github.com/hugomanguinhas/europeana_experiments/tree/master/blackhole
     * @param edmDate
     */
    public static LocalDate dateStringToDate(String edmDate) {
        // try most common format first
        LocalDate result = tryParseFormat(edmDate, DATE_YEARFIRST, false);

        // try second common format
        if (result == null) {
            result = tryParseFormat(edmDate, DATE_YEARLAST, false);
        }

        LOG.debug("Parsed edmDateString {}, result {}", edmDate, result);
        return result;
    }

    private static LocalDate tryParseFormat(String edmDate, DateTimeFormatter format,
        boolean logError) {
        try {
            return LocalDate.parse(edmDate, format);
        } catch (RuntimeException e) {
            if (logError) {
                LOG.error("Error parsing date {} (used formatter = {})", edmDate, format);
            }
        }
        return null;
    }

}
