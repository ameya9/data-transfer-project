package org.datatransferproject.datatransfer.generic.blobs;

import com.fasterxml.jackson.annotation.JsonSubTypes;

@JsonSubTypes({
    @JsonSubTypes.Type(FolderBlobsExportData.class),
    @JsonSubTypes.Type(FileBlobsExportData.class),
})
public interface BlobsExportData {}
