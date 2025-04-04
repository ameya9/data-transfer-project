package org.datatransferproject.datatransfer.generic.blobs;

import static java.lang.String.format;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import org.datatransferproject.datatransfer.generic.GenericPayload;
import org.datatransferproject.datatransfer.generic.GenericTransferConstants;
import org.datatransferproject.datatransfer.generic.ImportableData;
import org.datatransferproject.datatransfer.generic.ImportableFileData;
import org.datatransferproject.spi.transfer.idempotentexecutor.IdempotentImportExecutor;
import org.datatransferproject.types.common.models.blob.BlobbyStorageContainerResource;
import org.datatransferproject.types.common.models.blob.DigitalDocumentWrapper;

public class BlobbySerializer {

  static class BlobbyContainerPath {
    private BlobbyStorageContainerResource container;
    private String path;

    public BlobbyContainerPath(BlobbyStorageContainerResource container, String path) {
      this.container = container;
      this.path = path;
    }

    public BlobbyStorageContainerResource getContainer() {
      return container;
    }

    public String getPath() {
      return path;
    }
  }

  static final String SCHEMA_SOURCE =
      GenericTransferConstants.SCHEMA_SOURCE_BASE
          + "/extensions/data-transfer/portability-data-transfer-generic/src/main/java/org/datatransferproject/datatransfer/generic/BlobbySerializer.java";

  public static Iterable<ImportableData<BlobsExportData>> serialize(
      BlobbyStorageContainerResource root, IdempotentImportExecutor idempotentImportExecutor) {
    List<ImportableData<BlobsExportData>> results = new ArrayList<>();
    // Search whole tree of container resource
    Queue<BlobbyContainerPath> horizon = new ArrayDeque<>();
    BlobbyContainerPath containerAndPath = new BlobbyContainerPath(root, "");
    do {
      BlobbyStorageContainerResource container = containerAndPath.getContainer();
      String parentPath = containerAndPath.getPath();
      String path = format("%s/%s", parentPath, container.getName());

      if(idempotentImportExecutor.isKeyCached(container.getIdempotentId())) {
        parentPath = idempotentImportExecutor.getCachedValue(container.getIdempotentId());
        path = parentPath;
      }

      // String path = format("%s/%s", parentPath, container.getName());
      // Import the current folder, no need add current folder, already added
      results.add(
          new ImportableData<>(
              new GenericPayload<>(new FolderBlobsExportData(path), SCHEMA_SOURCE),
              container.getId(),
              path));

      // Add all sub-folders to the search tree
      for (BlobbyStorageContainerResource child : container.getFolders()) {
        horizon.add(new BlobbyContainerPath(child, path));
      }


      // Import all files in the current folder
      // Intentionally done after importing the current folder
      for (DigitalDocumentWrapper file : container.getFiles()) {
        results.add(
            new ImportableFileData<>(
                new CachedDownloadableItem(
                    file.getCachedContentId(), file.getDtpDigitalDocument().getName()),
                file.getDtpDigitalDocument().getEncodingFormat(),
                new GenericPayload<>(
                    FileBlobsExportData.fromDtpDigitalDocument(path, file.getDtpDigitalDocument()),
                    SCHEMA_SOURCE),
                file.getCachedContentId(),
                file.getDtpDigitalDocument().getName()));
      }
    } while ((containerAndPath = horizon.poll()) != null);

    return results;
  }
}
