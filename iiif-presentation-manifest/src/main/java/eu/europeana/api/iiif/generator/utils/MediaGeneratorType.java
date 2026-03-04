package eu.europeana.api.iiif.generator.utils;

import eu.europeana.api.record.model.WebResource;

public enum MediaGeneratorType {

    euscreen(new NoCanvas(), new EUScreenV3()),
    specialised(new SpecialisedV2(), new SpecialisedV3()),
    supported(new BrowserSupportedV2(), new BrowserSupportedV3()),
    absent(new NoCanvas(), new NoCanvas());

    private MediaGenerator<eu.europeana.api.iiif.v2.model.Canvas> genV2;
    private MediaGenerator<eu.europeana.api.iiif.v3.model.Canvas> genV3;

    private MediaGeneratorType(
            MediaGenerator<eu.europeana.api.iiif.v2.model.Canvas> genV2
          , MediaGenerator<eu.europeana.api.iiif.v3.model.Canvas> genV3) {
        this.genV2 = genV2;
        this.genV3 = genV3;
    }

    public eu.europeana.api.iiif.v2.model.Canvas generate(
            eu.europeana.api.iiif.v2.model.Canvas canvas, WebResource wr) {
        return this.genV2.generate(canvas, wr);
    }

    public eu.europeana.api.iiif.v3.model.Canvas generate(
            eu.europeana.api.iiif.v3.model.Canvas canvas, WebResource wr) {
        return this.genV3.generate(canvas, wr);
    }
}
