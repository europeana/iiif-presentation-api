/**
 * 
 */
package eu.europeana.api.set;

//import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

//import org.apache.commons.httpclient.Header;
//import org.apache.commons.httpclient.HttpClient;
//import org.apache.commons.httpclient.HttpException;
//import org.apache.commons.httpclient.HttpMethod;
//import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
//import org.apache.commons.httpclient.NameValuePair;
//import org.apache.commons.httpclient.methods.GetMethod;
//import org.apache.commons.httpclient.params.HttpClientParams;
//import org.apache.commons.io.IOUtils;
//
//import com.jayway.jsonpath.JsonPath;

import eu.europeana.api.item.Item;

/**
 * @author Hugo
 * @since 14 Oct 2024
 */
public class SetSearch
{
    protected SetConfig  _config;
//    protected HttpClient _client;

//    public SetSearch(SetConfig config) {
//        _config = config;
//        HttpClientParams params = new HttpClientParams();
//        params.setParameter(HttpClientParams.USER_AGENT, "Hugo Manguinhas Tool");
//        _client  = new HttpClient(params, new MultiThreadedHttpConnectionManager());
//    }

    /*
     Get all sets which have as visibility "published",
     e.g. https://api.europeana.eu/set/search?query=visibility:published&pageSize=1000
     */
//    public List<Set> search(String query, String profile, Integer pageSize, Integer page)
//            throws IOException {
//
//        GetMethod m = fillMethod(new GetMethod(getURL()), query, profile, pageSize, page);
//        try {
//            int r = _client.executeMethod(m);
//            return deserialize(getInputStream(m));
//        }
//        finally { closeMethod(m); }
//    }
//
//    protected String getURL() {
//        return _config.getEndpoint() + "search";
//    }
//
//    protected <M extends HttpMethod> M fillMethod(
//            M base, String query, String profile, Integer pageSize, Integer page)
//                    throws HttpException, IOException
//    {
//        NameValuePair[] list = new NameValuePair[4];
//        base.setRequestHeader("Accept-Encoding", "gzip");
//        //base.setRequestHeader("Accept", "application/ld+json");
//        base.setRequestHeader("X-API-KEY", _config.getKey());
//
//        query    = (query    == null ? "*" : query);
//        profile  = (profile  == null ? "itemDescriptions" : profile);
//        pageSize = (pageSize == null ? 10 : pageSize);
//        page     = (page     == null ? 1 : page);
//
//        list[0] = new NameValuePair("query",query);
//        list[1] = new NameValuePair("profile",profile);
//        list[2] = new NameValuePair("pageSize",pageSize.toString());
//        list[3] = new NameValuePair("page",page.toString());
//
//        base.setQueryString(list);
//        return base;
//    }
//
//    protected InputStream getInputStream(HttpMethod m) throws IOException
//    {
//        InputStream is = m.getResponseBodyAsStream();
//        Header encoding = m.getResponseHeader("Content-Encoding");
//        if ( encoding == null ) { return is; }
//
//        String value = encoding.getValue().toLowerCase();
//        if ( value.equals("gzip")   ) { return new GZIPInputStream(is);     }
//        if ( value.equals("deflate")) { return new InflaterInputStream(is); }
//        return is;
//    }
//
//    protected List<Set> deserialize(InputStream is) throws IOException
//    {
//        try {
//            List<Map> items = JsonPath.read(is, "$.items[*]");
//            List<Set> ret   = new ArrayList<>(items.size());
//
//
//            for ( Map item : items ) {
//                String id = (String)item.get("id");
//                Set set = new Set(id);
//                set.setTitle((Map)item.get("title"));
//                set.setDescription((Map)item.get("description"));
//                copyItems((List)item.get("items"), set);
//                ret.add(set);
//            }
//            return ret;
//        }
//        finally { is.close(); }
//    }
////
//    protected void copyItems(List list, Set set) {
//        for ( Object item : list ) {
//            if ( item instanceof String ) {
//                set.getItems().add(new Item((String)item));
//            }
//        }
//    }
//
//    protected void closeMethod(HttpMethod m)
//    {
//        try {
//            closeQuietly(m.getResponseBodyAsStream());
//            m.releaseConnection();
//        }
//        catch (IOException e) {}
//    }
//
//    public static final void main(String[] args) throws IOException {
//        SetSearch search = new SetSearch(new SetConfig("https://api.europeana.eu/set/", "api2demo"));
//        List<Set> list = search.search("visibility:published", "standard", 1000, 0);
//
//        System.out.println(list);
//    }
}
