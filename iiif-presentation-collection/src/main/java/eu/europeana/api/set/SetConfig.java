/**
 * 
 */
package eu.europeana.api.set;

/**
 * @author Hugo
 * @since 14 Oct 2024
 */
public class SetConfig
{

    private static final long serialVersionUID = 1L;

    private String        _endpoint;
    private String        _key;

    public SetConfig(String endpoint, String key)
    {
        _endpoint = endpoint;
        _key      = key;
    }

    public String        getEndpoint() { return _endpoint;   }
    public String        getKey() { return _key;   }


}
