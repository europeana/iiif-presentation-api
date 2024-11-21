/**
 * 
 */
package eu.europeana.api.set;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.io.IOUtils;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import eu.europeana.api.item.Item;
import eu.europeana.api.item.Item;

/**
 * @author Hugo
 * @since 14 Oct 2024
 */
public class SetItemRetrieval
{
    protected SetConfig  _config;
    protected HttpClient _client;
    protected Integer    _pageSize = 100;

    public SetItemRetrieval(SetConfig config) {
        _config = config;
        HttpClientParams params = new HttpClientParams();
        params.setParameter(HttpClientParams.USER_AGENT, "Hugo Manguinhas Tool");
        _client  = new HttpClient(params, new MultiThreadedHttpConnectionManager());
    }

    /*
     Get all sets which have as visibility "published", 
     e.g. https://api.europeana.eu/set/search?query=visibility:published&pageSize=1000
     */
    public Set retrieveItems(Set set, int limit) throws IOException {

        Integer page = 0;
        while ( true ) {
            GetMethod m = fillMethod(new GetMethod(getURL(set.getLocalID()))
                    , page, _pageSize, null);
            try {
                int r = _client.executeMethod(m);
                if ( r != 200 ) { return set; }

                boolean ret = fill(getInputStream(m), set);
                if ( !ret ) { break; }
                page++;
            }
            finally { closeMethod(m); }
        }
        return set;
    }

    protected String getURL(String id) {
        return _config.getEndpoint() + id;
    }

    protected <M extends HttpMethod> M fillMethod(
            M base, Integer page, Integer pageSize, String profile) 
                    throws HttpException, IOException
    {
        NameValuePair[] list = new NameValuePair[3];
        base.setRequestHeader("Accept-Encoding", "gzip");
        base.setRequestHeader("Accept", "application/ld+json");
        base.setRequestHeader("X-API-KEY", _config.getKey());

        profile  = (profile  == null ? "itemDescriptions" : profile);
        pageSize = (pageSize == null ? 10 : pageSize);
        page     = (page     == null ? 1 : page);

        list[0] = new NameValuePair("profile",profile);
        list[1] = new NameValuePair("pageSize",pageSize.toString());
        list[2] = new NameValuePair("page",page.toString());

        base.setQueryString(list);
        return base;
    }

    protected InputStream getInputStream(HttpMethod m) throws IOException
    {
        InputStream is = m.getResponseBodyAsStream();
        Header encoding = m.getResponseHeader("Content-Encoding");
        if ( encoding == null ) { return is; }

        String value = encoding.getValue().toLowerCase();
        if ( value.equals("gzip")   ) { return new GZIPInputStream(is);     }
        if ( value.equals("deflate")) { return new InflaterInputStream(is); }
        return is;
    }

    protected boolean fill(InputStream is, Set set) throws IOException
    {
        try {
            Map meta = JsonPath.parse(is).json();
            String nextURL = (String)meta.get("next");

            List<Item> items = set.getItems();
            for ( Map item : (List<Map>)meta.get("items") ) {
                items.add(getItem(item));
            }
            return (nextURL != null);
        }
        finally { is.close(); }
    }

    protected Item getItem(Map map) {
        String id = (String)map.get("id");
        Item item = new Item(id);
        item.setTitle(toLanguageMap(map.get("dcTitleLangAware")));
        item.setDescription(toLanguageMap(map.get("dcDescriptionLangAware")));
        item.setPreview(toString(map.get("edmPreview")));

        return item;
    }

    protected String toString(Object value) {
        if ( value instanceof Collection ) { 
            value = ((Collection)value).iterator().next(); 
        }
        if ( value instanceof String ) { return (String)value; }
        return null;
    }

    protected Map<String,String> toLanguageMap(Object obj) {
        if ( obj == null ) { return null; }

        if ( !(obj instanceof Map) ) { return null; }

        Map<String,?> map = (Map)obj;
        Map<String,String> ret = new LinkedHashMap<>();
        for ( Map.Entry<String,?> entry : map.entrySet() ) {
            String key = entry.getKey();
            if ( key.equals("def") ) { key = null; }

            String value = null;
            obj = entry.getValue();
            if ( obj instanceof String ) { value = (String)obj; }
            if ( obj instanceof Collection ) { 
                Collection col = ((Collection)obj);
                if ( col.isEmpty() ) { continue; }
                value = (String)col.iterator().next();
            }

            ret.put(key, value);
        }
        return ret;
    }

    protected void closeMethod(HttpMethod m)
    {
        try {
            closeQuietly(m.getResponseBodyAsStream());
            m.releaseConnection();
        }
        catch (IOException e) {} 
    }

    public static final void main(String[] args) throws IOException {
        SetItemRetrieval retrieval = new SetItemRetrieval(new SetConfig("https://api.europeana.eu/set/", "api2demo"));
        Set set = new Set("15694");
        set = retrieval.retrieveItems(set, 1000);

        System.out.println(set.getItems());
    }
}
