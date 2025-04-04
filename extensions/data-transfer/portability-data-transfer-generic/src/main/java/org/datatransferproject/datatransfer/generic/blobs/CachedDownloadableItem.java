package org.datatransferproject.datatransfer.generic.blobs;

import org.datatransferproject.types.common.DownloadableItem;

/**
 * Wrapper to adapt items known to be in temp storage (e.g. BLOB data) into {@link DownloadableItem}
 *
 * <p>It's useful to wrap such items so upstream code can consume either known temp store'd items or
 * items the Importer has to download itself (some MEDIA items) from the same interface.
 */
public class CachedDownloadableItem implements DownloadableItem {
  private String cachedId;
  private String name;

  public CachedDownloadableItem(String cachedId, String name) {
    this.cachedId = cachedId;
    this.name = name;
  }

  @Override
  public String getIdempotentId() {
    return cachedId;
  }

  @Override
  public String getFetchableUrl() {
    // 'url' is ID when cached
    return cachedId;
  }

  @Override
  public boolean isInTempStore() {
    return true;
  }

  @Override
  public String getName() {
    return name;
  }
}
