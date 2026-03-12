package eu.europeana.api.iiif.generator.utils;

import eu.europeana.api.iiif.generator.utils.v2.BrowserSupportedV2;
import eu.europeana.api.iiif.generator.utils.v2.SpecialisedV2;
import eu.europeana.api.iiif.generator.utils.v3.BrowserSupportedV3;
import eu.europeana.api.iiif.generator.utils.v3.EUScreenV3;
import eu.europeana.api.iiif.generator.utils.v3.SpecialisedV3;
import java.util.EnumMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * The registry class storing the mapping for the respective generators for each mediaGeneratorType
 * euscreen -> { v2 -> NoCanvas(), v3 -> EUScreenV3() }
 * specialised -> { v2 -> SpecialisedV2(), v3 -> SpecialisedV3() }
 * supported -> { v2 -> BrowserSupportedV2(), v3 -> BrowserSupportedV3()}
 * absent -> { v2 -> NoCanvas(), v3 -> NoCanvas() }
 */
@Component
public class MediaGeneratorRegistry {
  private static final Logger LOG = LogManager.getLogger(MediaGeneratorRegistry.class);
  public record GeneratorSet(MediaGenerator v2Generator, MediaGenerator v3Generator) {
    public <T> MediaGenerator<T> get(MediaGeneratorVersion v) {
      return switch (v) {
        case V2 -> v2Generator;
        case V3 -> v3Generator;
      };
    }
  }
  private Map<MediaGeneratorType,GeneratorSet> registry = new EnumMap<>(MediaGeneratorType.class);

  public MediaGeneratorRegistry(NoCanvas noCanvas,
      EUScreenV3 euscreenV3,
      SpecialisedV2 specV2,
      SpecialisedV3 specV3,
      BrowserSupportedV2 suppV2,
      BrowserSupportedV3 suppV3) {

    registry.put(MediaGeneratorType.EUSCREEN, new GeneratorSet(noCanvas, euscreenV3));
    registry.put(MediaGeneratorType.SPECIALISED, new GeneratorSet(specV2, specV3));
    registry.put(MediaGeneratorType.SUPPORTED, new GeneratorSet(suppV2, suppV3));
    registry.put(MediaGeneratorType.ABSENT, new GeneratorSet(noCanvas, noCanvas));

    printRegistry();
  }

  public <T> MediaGenerator<T> getGenerator(MediaGeneratorType type,MediaGeneratorVersion version) {
    GeneratorSet generatorSet = registry.get(type);
    if(generatorSet == null){
      throw new IllegalArgumentException("No Media Generators found for the Type : "+ type);
    }
    return (MediaGenerator<T>) generatorSet.get(version);
  }


  void printRegistry() {
    StringBuilder str = new StringBuilder();
    str.append("\n\n");
    for (Map.Entry<MediaGeneratorType, GeneratorSet> e : registry.entrySet()) {
      MediaGeneratorType type = e.getKey();
      GeneratorSet versionMap = e.getValue();
      str.append(type + " ->  {");
      for (MediaGeneratorVersion version : MediaGeneratorVersion.values()) {
        MediaGenerator<?> generator = versionMap.get(version);
        str.append(" " + version + " -> " + generator.getClass().getSimpleName() + " ");
      }
      str.append("}\n");
    }
    LOG.info(str.toString());
  }
}