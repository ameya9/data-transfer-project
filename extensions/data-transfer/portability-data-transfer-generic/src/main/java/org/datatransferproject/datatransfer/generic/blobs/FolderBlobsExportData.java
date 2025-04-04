package org.datatransferproject.datatransfer.generic.blobs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonTypeName("Folder")
public class FolderBlobsExportData implements BlobsExportData {
  @JsonProperty
  private final String path;

  @JsonCreator
  public FolderBlobsExportData(@JsonProperty String path) {
    this.path = path;
  }

  public String getPath() {
    return path;
  }
}
