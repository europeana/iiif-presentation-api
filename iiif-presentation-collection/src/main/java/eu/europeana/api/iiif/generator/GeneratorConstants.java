package eu.europeana.api.iiif.generator;

/**
 * Constants for Generator
 * @author Hugo
 * @since 14 Oct 2024
 */
public interface GeneratorConstants {

    public static final String LANG_META = "en";

    public static final String ROOT_LABEL = "Top-level Collection";
    public static final String ROOT_DESCRIPTION = "Top-level Collection for the Data Space for Cultural Heritage";

    public static final String ROOT_GALLERY_LABEL = "Collection of galleries";
    public static final String ROOT_GALLERY_DESCRIPTION = "Collection of all galleries published in the data space";

    public static final String EUROPEANA_NAME = "Europeana";
    public static final String EUROPEANA_LOGO = "https://style.europeana.eu/images/europeana-logo-default.png";

    public static final String WEBSITE_PAGE_LANDING = "https://www.europeana.eu/";
    public static final String WEBSITE_PAGE_ABOUT = "https://www.europeana.eu/en/about-us";
    public static final String WEBSITE_TITLE = "Europeana Website";
    public static final String WEBSITE_TITLE_GALLERY = "Europeana Gallery";

    public static final String MIMETYPE_JSONLD = "application/ld+json";
    public static final String MIMETYPE_HTML = "text/html";

    public static final String EXTENSION_JSONLD = "jsonld";

    public static final String SET_JSONLD_CONTEXT = "https://api.europeana.eu/schema/context/set.jsonld";
}
