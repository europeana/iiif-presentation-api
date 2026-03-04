package eu.europeana.api.iiif.media;

public enum MediaCategory {

    Video, Sound, Text, Image, Model, EmbeddableResource;

    public boolean isAudioVisual() {
        return ( name().equals(Video.name()) || name().equals(Sound.name()) );
    }
}
