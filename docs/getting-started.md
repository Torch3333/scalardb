## Getting Started with Scalar DB

## Overview
This document briefly explains how you can get started with Scalar DB with a simple electronic money application.

## Install prerequisites

Scalar DB is written in Java and uses Cassandra as an underlining storage implementation, so the following software is required to run it.

* [Oracle JDK 8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) (OpenJDK 8) or higher
* [Casssandra](http://cassandra.apache.org/) 3.11.x (the current stable version as of writing)
    * Take a look at [this document](http://cassandra.apache.org/download/) for how to set up Cassandra.
    * Change `commitlog_sync` from `periodic` to `batch` in `cassandra.yaml` not to lose data when quorum of replica nodes go down
* Other libraries used from the above are automatically installed through gradle

From here, we assume Oracle JDK 8 and Cassandra 3.11.x are properly installed in your local environment, and Cassandra is running in your localhost.

## Configure the Cassandra connection

The `database.properties` (conf/database.properties) file holds the configuration for Scalar DB. Basically, it describes the Cassandra installation that will be used.

```
# Comma separated contact points
scalar.db.contact_points=localhost

# Port number for all the contact points. Default port number for each database is used if empty.
scalar.db.contact_port=9042

# Credential information to access the database
scalar.db.username=cassandra
scalar.db.password=cassandra

# Storage implementation. Either cassandra or cosmos can be set. Default storage is cassandra.
#scalar.db.storage=cassandra
```

## Build

For building Scalar DB, what you will need to do is as follows.
```
$ SCALARDB_HOME=/path/to/scalardb
$ cd $SCALARDB_HOME
$ ./gradlew installDist
$ sudo mkdir /var/log/scalar
$ sudo chmod 777 /var/log/scalar
```
Or you can download from [maven central repository](https://mvnrepository.com/artifact/com.scalar-labs/scalardb).
For example in Gradle, you can add the following dependency to your build.gradle. Please replace the `<version>` with the version you want to use.
```
dependencies {
    compile group: 'com.scalar-labs', name: 'scalardb', version: '<version>'
}
```

Let's move to the `getting-started` directory so that we can avoid too much copy-and-paste.
```
$ cd docs/getting-started
```

## Set up database schema

First of all, you need to define how the data will be organized (a.k.a database schema) in the application with Scalar DB database schema.
Here is a database schema for the sample application. For the supported data types, please see [this doc](schema.md) for more details.

```json
{
  "emoney.account": {
    "transaction": false,
    "partition-key": [
      "id"
    ],
    "clustering-key": [],
    "columns": {
      "id": "TEXT",
      "balance": "INT"
    }
  }
}
```

Then, download the schema loader that matches with the version you use from [scalardb releases](https://github.com/scalar-labs/scalardb/releases), and run the following command to load the schema.

```
$ java -jar scalar-schema-<vesrion>.jar --cassandra -h localhost -u <CASSNDRA_USER> -p <CASSANDRA_PASSWORD> -f emoney-storage.json -R 1
```

## Store & retrieve data with storage service

[`ElectronicMoneyWithStorage.java`](./getting-started/src/main/java/sample/ElectronicMoneyWithStorage.java)
is a simple electronic money application with storage service.
(Be careful: it is simplified for ease of reading and far from practical and is certainly not production-ready.)

```java
public class ElectronicMoneyWithStorage extends ElectronicMoney {
  private final StorageService service;

  public ElectronicMoneyWithStorage() {
    Injector injector = Guice.createInjector(new StorageModule(new DatabaseConfig(props)));
    service = injector.getInstance(StorageService.class);
    service.with(NAMESPACE, TABLENAME);
  }

  @Override
  public void charge(String id, int amount) throws ExecutionException {
    // Retrieve the current balance for id
    Get get = new Get(new Key(new TextValue(ID, id)));
    Optional<Result> result = service.get(get);

    // Calculate the balance
    int balance = amount;
    if (result.isPresent()) {
      int current = ((IntValue) result.get().getValue(BALANCE).get()).get();
      balance += current;
    }

    // Update the balance
    Put put = new Put(new Key(new TextValue(ID, id))).withValue(new IntValue(BALANCE, balance));
    service.put(put);
  }

  @Override
  public void pay(String fromId, String toId, int amount) throws ExecutionException {
    // Retrieve the current balances for ids
    Get fromGet = new Get(new Key(new TextValue(ID, fromId)));
    Get toGet = new Get(new Key(new TextValue(ID, toId)));
    Optional<Result> fromResult = service.get(fromGet);
    Optional<Result> toResult = service.get(toGet);

    // Calculate the balances (it assumes that both accounts exist)
    int newFromBalance = ((IntValue) (fromResult.get().getValue(BALANCE).get())).get() - amount;
    int newToBalance = ((IntValue) (toResult.get().getValue(BALANCE).get())).get() + amount;
    if (newFromBalance < 0) {
      throw new RuntimeException(fromId + " doesn't have enough balance.");
    }

    // Update the balances
    Put fromPut =
        new Put(new Key(new TextValue(ID, fromId)))
            .withValue(new IntValue(BALANCE, newFromBalance));
    Put toPut =
        new Put(new Key(new TextValue(ID, toId))).withValue(new IntValue(BALANCE, newToBalance));
    service.put(fromPut);
    service.put(toPut);
  }

  @Override
  public void close() {
    service.close();
  }
}
```

Now we can run the application.
```
$ ../../gradlew run --args="-mode storage -action charge -amount 1000 -to user1"
$ ../../gradlew run --args="-mode storage -action charge -amount 0 -to merchant1"
$ ../../gradlew run --args="-mode storage -action pay -amount 100 -to merchant1 -from user1"
```

## Set up database schema with transaction

To apply transaction, we can just add a key `transaction` and value as `true` in Scalar DB scheme.

```json
{
  "emoney.account": {
    "transaction": true,
    "partition-key": [
      "id"
    ],
    "clustering-key": [],
    "columns": {
      "id": "TEXT",
      "balance": "INT"
    }
  }
}
```

Before reapplying the schema, please drop the existing namespace first by issuing the following. 

```
$ java -jar scalar-schema-<vesrion>.jar --cassandra -h localhost -u <CASSNDRA_USER> -p <CASSANDRA_PASSWORD> -D
$ java -jar scalar-schema-<vesrion>.jar --cassandra -h localhost -u <CASSNDRA_USER> -p <CASSANDRA_PASSWORD> -f emoney-transaction.json -R 1
```

## Store & retrieve data with transaction service

The previous application seems fine under ideal conditions, but it is problematic when some failure happens during its operation or when multiple operations occur at the same time because it is not transactional.
For example, money transfer (pay) from `A's balance` to `B's balance` is not done atomically in the application, and there might be a case where only `A's balance` is decreased (and `B's balance` is not increased) if a failure happens right after the first `put` and some money will be lost.

With the transaction capability of Scalar DB, we can make such operations to be executed with ACID properties.

Now we can update the code as follows to make it transactional.
```java
public class ElectronicMoneyWithTransaction extends ElectronicMoney {
  private final TransactionService service;

  public ElectronicMoneyWithTransaction() {
    Injector injector = Guice.createInjector(new TransactionModule(new DatabaseConfig(props)));
    service = injector.getInstance(TransactionService.class);
    service.with(NAMESPACE, TABLENAME);
  }

  @Override
  public void charge(String id, int amount)
      throws CrudException, CommitException, UnknownTransactionStatusException {
    // Start a transaction
    DistributedTransaction tx = service.start();

    // Retrieve the current balance for id
    Get get = new Get(new Key(new TextValue(ID, id)));
    Optional<Result> result = tx.get(get);

    // Calculate the balance
    int balance = amount;
    if (result.isPresent()) {
      int current = ((IntValue) result.get().getValue(BALANCE).get()).get();
      balance += current;
    }

    // Update the balance
    Put put = new Put(new Key(new TextValue(ID, id))).withValue(new IntValue(BALANCE, balance));
    tx.put(put);

    // Commit the transaction (records are automatically recovered in case of failure)
    tx.commit();
  }

  @Override
  public void pay(String fromId, String toId, int amount)
      throws CrudException, CommitException, UnknownTransactionStatusException {
    // Start a transaction
    DistributedTransaction tx = service.start();

    // Retrieve the current balances for ids
    Get fromGet = new Get(new Key(new TextValue(ID, fromId)));
    Get toGet = new Get(new Key(new TextValue(ID, toId)));
    Optional<Result> fromResult = tx.get(fromGet);
    Optional<Result> toResult = tx.get(toGet);

    // Calculate the balances (it assumes that both accounts exist)
    int newFromBalance = ((IntValue) (fromResult.get().getValue(BALANCE).get())).get() - amount;
    int newToBalance = ((IntValue) (toResult.get().getValue(BALANCE).get())).get() + amount;
    if (newFromBalance < 0) {
      throw new RuntimeException(fromId + " doesn't have enough balance.");
    }

    // Update the balances
    Put fromPut =
        new Put(new Key(new TextValue(ID, fromId)))
            .withValue(new IntValue(BALANCE, newFromBalance));
    Put toPut =
        new Put(new Key(new TextValue(ID, toId))).withValue(new IntValue(BALANCE, newToBalance));
    tx.put(fromPut);
    tx.put(toPut);

    // Commit the transaction (records are automatically recovered in case of failure)
    tx.commit();
  }

  @Override
  public void close() {
    service.close();
  }
}
```

As you can see, it's not very different from the code with `StorageService`.
This code instead uses `TransactionService` and all the CRUD operations are done through the `DistributedTransaction` object returned from `TransactionService.start()`.

Now let's run the application with transaction mode.
```
$ ../../gradlew run --args="-mode transaction -action charge -amount 1000 -to user1"
$ ../../gradlew run --args="-mode transaction -action charge -amount 0 -to merchant1"
$ ../../gradlew run --args="-mode transaction -action pay -amount 100 -to merchant1 -from user1"
```

## Further documentation

These are just simple examples of how Scalar DB is used. For more information, please take a look at the following documents.

* [Design Document](design.md)
* [Javadoc](https://scalar-labs.github.io/scalardb/javadoc/)
* [Database schema in Scalar DB](schema.md)
* [Schema tool](../tools/scalar-schema/README.md)
