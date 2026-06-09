package eu.europeana.api.iiif.media;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum MediaCategory {
    @JsonProperty("Video")
    VIDEO,
    @JsonProperty("Sound")
    SOUND,
    @JsonProperty("Text")
    TEXT,
    @JsonProperty("Image")
    IMAGE,
    @JsonProperty("Model")
    MODEL,
    @JsonProperty("EmbeddableResource")
    EMBEDDABLE_RESOURCE;

    public boolean isAudioVisual() {
        return ( name().equals(VIDEO.name()) || name().equals(SOUND.name()) );
    }
}
