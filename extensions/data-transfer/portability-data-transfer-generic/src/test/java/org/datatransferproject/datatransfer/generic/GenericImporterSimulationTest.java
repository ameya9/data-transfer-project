package org.datatransferproject.datatransfer.generic;

import static org.datatransferproject.datatransfer.generic.GenericFileImporterTest.getMultipartStream;
import static org.datatransferproject.datatransfer.generic.GenericFileImporterTest.readPartBody;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.fileupload.MultipartStream;
import org.datatransferproject.api.launcher.Monitor;
import org.datatransferproject.datatransfer.generic.blobs.BlobbySerializer;
import org.datatransferproject.datatransfer.generic.blobs.BlobsExportData;
import org.datatransferproject.spi.cloud.storage.TemporaryPerJobDataStore;
import org.datatransferproject.spi.transfer.idempotentexecutor.InMemoryIdempotentImportExecutor;
import org.datatransferproject.types.common.models.blob.BlobbyStorageContainerResource;
import org.datatransferproject.types.common.models.blob.DigitalDocumentWrapper;
import org.datatransferproject.types.common.models.blob.DtpDigitalDocument;
import org.datatransferproject.types.transfer.auth.AppCredentials;
import org.datatransferproject.types.transfer.auth.TokensAndUrlAuthData;
import org.junit.Before;
import org.junit.Test;

public class GenericImporterSimulationTest {
  private final Monitor monitor = new Monitor() {};
  private final TemporaryPerJobDataStore dataStore =
      new TemporaryPerJobDataStore() {
        @Override
        public InputStreamWrapper getStream(UUID jobId, String key) throws IOException {
          return new InputStreamWrapper(new ByteArrayInputStream("Hello world".getBytes()));
        }
      };
  private MockWebServer webServer;

  private void simulateImporterIterations(
      UUID jobId,
      InMemoryIdempotentImportExecutor idempotentImportExecutor,
      TokensAndUrlAuthData authData,
      List<BlobbyStorageContainerResource> blobbyStorageContainerResources,
      GenericFileImporter<BlobbyStorageContainerResource, BlobsExportData> importer)
      throws Exception {

    for(BlobbyStorageContainerResource dataModel: blobbyStorageContainerResources) {
      importer.importItem(jobId, idempotentImportExecutor, authData, dataModel);
    }
  }

  @Before
  public void setup() throws IOException {
    webServer = new MockWebServer();
    webServer.start();
  }


  @Test
  public void simpleImportTest() throws Exception {
    webServer.enqueue(new MockResponse().setResponseCode(201).setBody("OK"));
    webServer.enqueue(new MockResponse().setResponseCode(201).setBody("OK"));
    webServer.enqueue(new MockResponse().setResponseCode(201).setBody("OK"));
    webServer.enqueue(new MockResponse().setResponseCode(201).setBody("OK"));
    webServer.enqueue(new MockResponse().setResponseCode(201).setBody("OK"));
    webServer.enqueue(new MockResponse().setResponseCode(201).setBody("OK"));
    webServer.enqueue(new MockResponse().setResponseCode(201).setBody("OK"));
    webServer.enqueue(new MockResponse().setResponseCode(201).setBody("OK"));
    webServer.enqueue(new MockResponse().setResponseCode(201).setBody("OK"));
    webServer.enqueue(new MockResponse().setResponseCode(201).setBody("OK"));
    webServer.enqueue(new MockResponse().setResponseCode(201).setBody("OK"));
    webServer.enqueue(new MockResponse().setResponseCode(201).setBody("OK"));
    webServer.enqueue(new MockResponse().setResponseCode(201).setBody("OK"));


    DigitalDocumentWrapper wrapper = new DigitalDocumentWrapper(
        new DtpDigitalDocument("bar.txt", null, "text/plain"), "text/plain", "bartxt");

    BlobbyStorageContainerResource rootPre = new BlobbyStorageContainerResource(
        "Test Root",
        "root",
        Collections.emptyList(),
        Collections.emptyList());

    BlobbyStorageContainerResource root = new BlobbyStorageContainerResource(
        "Test Root",
        "root",
        Collections.emptyList(),
        List.of(new BlobbyStorageContainerResource("Folder 1.1", "1.1", Collections.emptyList(), Collections.emptyList()),
        new BlobbyStorageContainerResource("Folder 1.2", "1.2", Collections.emptyList(), Collections.emptyList())));

    BlobbyStorageContainerResource folder1_1 = new BlobbyStorageContainerResource(
        "Folder 1.1",
        "1.1",
        Collections.emptyList(),
        List.of(new BlobbyStorageContainerResource("Folder 1.1.1", "1.1.1", Collections.emptyList(), Collections.emptyList()),
            new BlobbyStorageContainerResource("Folder 1.1.2", "1.1.2", Collections.emptyList(), Collections.emptyList())));

    BlobbyStorageContainerResource folder1_2 = new BlobbyStorageContainerResource(
        "Folder 1.2",
        "1.2",
        List.of(wrapper),
        Collections.emptyList());

    BlobbyStorageContainerResource folder1_1_1 = new BlobbyStorageContainerResource(
        "Folder 1.1.1",
        "1.1.1",
        List.of(wrapper),
        Collections.emptyList());

    BlobbyStorageContainerResource folder1_1_2 = new BlobbyStorageContainerResource(
        "Folder 1.1.2",
        "1.1.2",
        List.of(wrapper),
        Collections.emptyList());



    UUID jobId = UUID.randomUUID();
    InMemoryIdempotentImportExecutor executor = new InMemoryIdempotentImportExecutor(monitor);
    TokensAndUrlAuthData authData = new TokensAndUrlAuthData(
        "accessToken", "refreshToken", webServer.url("/refresh").toString());

        GenericFileImporter<BlobbyStorageContainerResource, BlobsExportData> importer =
        new GenericFileImporter<>(
            BlobbySerializer::serialize,
            new AppCredentials("key", "secret"),
            webServer.url("/id").url(),
            dataStore,
            monitor);

    simulateImporterIterations(jobId, executor, authData, List.of(rootPre, root, folder1_1, folder1_1_1, folder1_1_2, folder1_2), importer);


    for (int i=0; i<webServer.getRequestCount(); i++) {
      RecordedRequest request = webServer.takeRequest();
      if(request.getHeader("Content-Type").equalsIgnoreCase("application/json")) {
        String body = request.getBody().readUtf8();
        System.out.println(body);
      } else {
        MultipartStream stream = getMultipartStream(request);
        String out = readPartBody(stream);
        System.out.println(out);
      }
    }


  }
}
