package eu.europeana.api.caching;

import java.util.Base64.Encoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import static eu.europeana.api.caching.CachingHeaders.*;

/**
 * @author Hugo
 * @since 22 Nov 2024
 */
public class CachingUtils {

    public static char SEPARATOR = '|';

    /*
     * Examples:
     * If-None-Match: "xyzzy"
     * If-None-Match: W/"xyzzy"
     * If-None-Match: "xyzzy", "r2d2xxxx", "c3piozzzz"
     * If-None-Match: W/"xyzzy", W/"r2d2xxxx", W/"c3piozzzz"
     * If-None-Match: *
     */
    private static Pattern MULTI_ETAG = Pattern.compile("(?:\\s*,\\s*)?(?:[wW]/)?\"(([^\"]|\\\\\")*)\"");

    public static ETag parseETag(String txt) {
        return WeakETag.parseAsWeakEtag(txt);
    }

    public static ZonedDateTime getLastModified(long timestamp) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp)
                                     , ZoneId.systemDefault());
    }

    public static <E extends ETag> List<E> getIfNoneMatch(HttpServletRequest request, E molde) {
        String ifNoneMatch = request.getHeader(IF_NONE_MATCH);
        if ( StringUtils.isBlank(ifNoneMatch) ) { return null; }

        List<E> list = new ArrayList<>(1);
        Matcher m = MULTI_ETAG.matcher(ifNoneMatch);
        while ( m.find() ) {
            E etag = molde.parse(m.group(1));
            if ( etag != null ) { list.add(etag); }
        }
        return list;
    }

    public static ZonedDateTime getIfModifiedSince(HttpServletRequest request) {
        
        String ifModifiedSince = request.getHeader(IF_MODIFIED_SINCE);
        if (StringUtils.isEmpty(ifModifiedSince)) { return null; }

        // Latest RFC7232 https://www.rfc-editor.org/rfc/rfc7232.txt
        try {
            return ZonedDateTime.parse(ifModifiedSince
                                     , DateTimeFormatter.RFC_1123_DATE_TIME);
        }
        catch (DateTimeParseException e) {
            return null;
        }
    }

    /*
    public static String generateEtag(Object... objs) {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        int len = objs.length;
        for ( int i = 0; i < len; i++ ) {
            if ( i > 0 ) { buffer.putChar('|'); }

            Object obj = objs[i];
            if ( obj instanceof Number ) {
                if ( obj instanceof Integer ) {
                    buffer.putInt((Integer)obj);
                    continue;
                }
                else if ( obj instanceof Float ) {
                    buffer.putFloat((Float)obj);
                    continue;
                }
                else if ( obj instanceof Double ) {
                    buffer.putDouble((Double)obj);
                    continue;
                }
                else if ( obj instanceof Long ) {
                    buffer.putLong((Long)obj);
                    continue;
                }
                else if ( obj instanceof Short ) {
                    buffer.putShort((Short)obj);
                    continue;
                }
            }
            buffer.put(obj.toString().getBytes(StandardCharsets.UTF_8));
        }
        return Base64.getEncoder().encodeToString(buffer.array());
    }
    */

    public static WeakETag genWeakEtag(Object... objs) {
        Encoder encoder = Base64.getEncoder();
        int len = objs.length;
        if ( len == 0 ) { return null; }

        if ( len == 1 ) { 
            return new WeakETag(encoder.encodeToString(objs[0].toString().getBytes()));
        }

        StringBuilder sb = new StringBuilder();
        sb.append(objs[0].toString());
        for ( int i = 1 ; i < len; i++ ) {
            sb.append(SEPARATOR);
            sb.append(objs[i].toString());
        }
        return new WeakETag(encoder.encodeToString(sb.toString().getBytes()));
    }


    /**
     * Calculates SHA256 hash of a particular data string
     * @param  data String of data on which the hash is based
     * @return SHA256Hash   String
     */
    private static String getSHA256Hash(String data){
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            //LogManager.getLogger(CachingUtils.class).error("Error generating SHA-265 hash from record timestamp_update", e);
        }
        return null;
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte bt : hash) {
            String hex = Integer.toHexString(0xff & bt);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
