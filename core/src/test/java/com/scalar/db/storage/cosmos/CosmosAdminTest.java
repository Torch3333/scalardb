package com.scalar.db.storage.cosmos;

import static org.mockito.Mockito.verify;

import com.scalar.db.exception.storage.ExecutionException;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CosmosAdminTest {

  @Mock CosmosTableMetadataManager metadataManager;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void
      getTableMetadata_ConstructedWithoutNamespacePrefix_ShouldBeCalledWithoutNamespacePrefix()
          throws ExecutionException {
    // Arrange
    Optional<String> namespacePrefix = Optional.empty();
    String namespace = "ns";
    String table = "table";

    CosmosAdmin admin = new CosmosAdmin(metadataManager, namespacePrefix);

    // Act
    admin.getTableMetadata(namespace, table);

    // Assert
    verify(metadataManager).getTableMetadata(namespace, table);
  }

  @Test
  public void getTableMetadata_ConstructedWithNamespacePrefix_ShouldBeCalledWithNamespacePrefix()
      throws ExecutionException {
    // Arrange
    Optional<String> namespacePrefix = Optional.of("prefix_");
    String namespace = "ns";
    String table = "table";

    CosmosAdmin admin = new CosmosAdmin(metadataManager, namespacePrefix);

    // Act
    admin.getTableMetadata(namespace, table);

    // Assert
    verify(metadataManager).getTableMetadata(namespacePrefix.get() + "_" + namespace, table);
  }
}