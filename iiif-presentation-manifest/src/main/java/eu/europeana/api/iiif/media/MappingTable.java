package eu.europeana.api.iiif.media;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import eu.europeana.api.iiif.generator.media.MediaGeneratorType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
Represents xml e.g. -
<config>
	<mapping mediaType="image/jpg" methodV2="supported" methodV3="absent"/>
</config>

 */

@JacksonXmlRootElement(localName = "config")
public class MappingTable {

	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "mapping")
	public List<MappingTableEntry> entries;

	private static Map<String,MappingEntry> map = new HashMap<>();

	public static  MediaGeneratorType getGeneratorTypeV2(String mediaType) {
		return map.get(mediaType).v2;
	}

	public static MediaGeneratorType getGeneratorTypeV3(String mediaType) {
		return map.get(mediaType).v3;
	}

	public static Map<String, MappingEntry> getMap() {
		return map;
	}

	public record MappingEntry(MediaGeneratorType v2, MediaGeneratorType v3) {}

}