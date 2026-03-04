package eu.europeana.api.iiif.media;

import java.util.Map;

import eu.europeana.api.iiif.generator.utils.MediaGeneratorType;

/*

<config>
	<mapping mediaType="image/jpg" methodV2="supported" methodV3="absent"/>
</config>

 */

public class MappingTable {

	private static Map<String,MappingEntry> map;

	public static MediaGeneratorType getGeneratorTypeV2(String mediaType) {
		return map.get(mediaType).v2;
	}

	public static MediaGeneratorType getGeneratorTypeV3(String mediaType) {
		return map.get(mediaType).v3;
	}

	private static record MappingEntry(MediaGeneratorType v2, MediaGeneratorType v3) {}

}
