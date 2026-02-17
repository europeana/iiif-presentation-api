package eu.europeana.api.iiif.dto;

/*
  Holds additional details about Record object fetched from api.
 */
public class RecordInfo {

  public RecordInfo(boolean isArchived, Object record) {
    this.isArchived = isArchived;
    this.record = record;
  }

  private boolean isArchived = false;
  private final Object record;

  public boolean isArchived() {
    return isArchived;
  }
  public Object getRecord() {
    return record;
  }
}