package eu.europeana.api.record.model;

public record Resolution(int width, int height) {

    public Resolution scaleToWidth(int width) {
        // width is higher (and equal) than 400px - Set width = 400 ; Set height = (height / width) x 400
        return (this.width <= width ? this
            : new Resolution(width, (int) (((float) height / this.width) * width)));
    }

}
