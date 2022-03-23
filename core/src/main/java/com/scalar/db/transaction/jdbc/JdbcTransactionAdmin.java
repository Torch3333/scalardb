package com.scalar.db.transaction.jdbc;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.scalar.db.api.DistributedTransactionAdmin;
import com.scalar.db.api.TableMetadata;
import com.scalar.db.exception.storage.ExecutionException;
import com.scalar.db.storage.jdbc.JdbcAdmin;
import com.scalar.db.storage.jdbc.JdbcConfig;
import java.util.Map;
import java.util.Set;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class JdbcTransactionAdmin implements DistributedTransactionAdmin {

  private final JdbcAdmin admin;

  @Inject
  public JdbcTransactionAdmin(JdbcConfig config) {
    admin = new JdbcAdmin(config);
  }

  @VisibleForTesting
  JdbcTransactionAdmin(JdbcAdmin admin) {
    this.admin = admin;
  }

  @Override
  public void createNamespace(String namespace, Map<String, String> options)
      throws ExecutionException {
    admin.createNamespace(namespace, options);
  }

  @Override
  public void createTable(
      String namespace, String table, TableMetadata metadata, Map<String, String> options)
      throws ExecutionException {
    admin.createTable(namespace, table, metadata, options);
  }

  @Override
  public void dropTable(String namespace, String table) throws ExecutionException {
    admin.dropTable(namespace, table);
  }

  @Override
  public void dropNamespace(String namespace) throws ExecutionException {
    admin.dropNamespace(namespace);
  }

  @Override
  public void truncateTable(String namespace, String table) throws ExecutionException {
    admin.truncateTable(namespace, table);
  }

  @Override
  public void createIndex(
      String namespace, String table, String columnName, Map<String, String> options)
      throws ExecutionException {
    admin.createIndex(namespace, table, columnName, options);
  }

  @Override
  public void dropIndex(String namespace, String table, String columnName)
      throws ExecutionException {
    admin.dropIndex(namespace, table, columnName);
  }

  @Override
  public TableMetadata getTableMetadata(String namespace, String table) throws ExecutionException {
    return admin.getTableMetadata(namespace, table);
  }

  @Override
  public Set<String> getNamespaceTableNames(String namespace) throws ExecutionException {
    return admin.getNamespaceTableNames(namespace);
  }

  @Override
  public boolean namespaceExists(String namespace) throws ExecutionException {
    return admin.namespaceExists(namespace);
  }

  @Override
  public void createCoordinatorNamespaceAndTable(Map<String, String> options) {
    throw new UnsupportedOperationException("this method is not supported in JDBC transaction");
  }

  @Override
  public void dropCoordinatorNamespaceAndTable() {
    throw new UnsupportedOperationException("this method is not supported in JDBC transaction");
  }

  @Override
  public void truncateCoordinatorTable() {
    throw new UnsupportedOperationException("this method is not supported in JDBC transaction");
  }

  @Override
  public boolean coordinatorTableExists() {
    throw new UnsupportedOperationException("this method is not supported in JDBC transaction");
  }

  @Override
  public void close() {
    admin.close();
  }
}