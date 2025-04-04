package org.datatransferproject.datatransfer.generic.blobs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.datatransferproject.types.common.models.blob.DtpDigitalDocument;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonTypeName("File")
public class FileBlobsExportData implements BlobsExportData {
  @JsonProperty
  private final String folder;
  @JsonProperty private final String name;
  @JsonProperty private final Optional<ZonedDateTime> dateModified;

  private FileBlobsExportData(String folder, String name, Optional<ZonedDateTime> dateModified) {
    this.folder = folder;
    this.name = name;
    this.dateModified = dateModified;
  }

  public String getFolder() {
    return folder;
  }

  public String getName() {
    return name;
  }

  public Optional<ZonedDateTime> getDateModified() {
    return dateModified;
  }

  public static FileBlobsExportData fromDtpDigitalDocument(String path, DtpDigitalDocument model) {
    return new FileBlobsExportData(
        path,
        model.getName(),
        Optional.ofNullable(model.getDateModified())
            .filter(string -> !string.isEmpty())
            .map(dateString -> ZonedDateTime.parse(model.getDateModified())));
  }
}
