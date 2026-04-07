package eu.europeana.api.record.model;

/**
 * Represents dimensions of rectangular area (image)
 * @param width horizontal dimension in pixels
 * @param height vertical dimension in pixels
 */
public record Resolution(int width, int height) {

    /**
     * Provide adjusted resolution based on provided width.     *
     * @param width width of image
     * @return adjusted resolution
     */
    public Resolution scaleToWidth(int width) {
        // width is higher (and equal) than 400px - Set width = 400 ; Set height = (height / width) x 400
        return (this.width <= width ? this
            : new Resolution(width, (int) (((double) height / this.width) * width)));
    }

}
