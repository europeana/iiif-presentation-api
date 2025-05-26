package eu.europeana.api.iiif.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;

import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import eu.europeana.api.iiif.exceptions.RecordParseException;
import eu.europeana.api.iiif.generator.ManifestSettings;
import eu.europeana.api.iiif.media.MediaTypes;
import eu.europeana.api.iiif.model.IIIFResource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;


/**
 * Service that loads record data, uses that to generate a Manifest object and 
 * serializes the manifest in JSON-LD
 *
 * @author Patrick Ehlert
 * Created on 06-12-2017
 */
@Service
public class ManifestService {

    private final ObjectMapper mapper;


    /**
     * Creates an instance of the ManifestService bean with provided settings
     *  @param settings   read from properties file
     * @param mediaTypes
     * @param fulltextService
     * @param recordService
     */
    public ManifestService(ManifestSettings settings, MediaTypes mediaTypes
                         , RecordService recordService) {
        mapper = new ObjectMapper();

        // configure jsonpath: we use jsonpath in combination with Jackson because that makes it easier to know what
        // type of objects are returned (see also https://stackoverflow.com/a/40963445)
        com.jayway.jsonpath.Configuration.setDefaults(new com.jayway.jsonpath.Configuration.Defaults() {

            private final JsonProvider jsonProvider = new JacksonJsonNodeJsonProvider();
            private final MappingProvider mappingProvider = new JacksonMappingProvider();

            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public MappingProvider mappingProvider() {
                return mappingProvider;
            }

            @Override
            public Set<Option> options() {
                if (Boolean.TRUE.equals(settings.getSuppressParseException())) {
                    // we want to be fault tolerant in production, but for testing we may want to disable this option
                    return EnumSet.of(Option.SUPPRESS_EXCEPTIONS);
                } else {
                    return EnumSet.noneOf(Option.class);
                }
            }
        });
    }


    /**
     * Serialize manifest to JSON-LD
     *
     * @param m manifest
     * @return JSON-LD string
     * @throws RecordParseException when there is a problem parsing
     */
    public void serializeManifest(Object m, OutputStream out) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(out, m);
    }
}
