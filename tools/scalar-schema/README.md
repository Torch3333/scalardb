# Schema Tool for Scalar DB
This tool creates Scalar DB schemas on Cosmos DB and Cassandra.
  - For Cosmos DB, this tool creates databases(collections) and tables(containers), also inserts metadata which is required by Scalar DB.
  - For Cassandra, this tool creates databases(keyspaces) and tables. You can specify the compaction strategy, the network topology strategy, and the replication factor as well.
  - You don't have to add Scalar DB metadata for transactions. This tool automatically adds them when you set the `transaction` parameter `true` in your schema file.

# Usage

## Build & Run
### Build a standalone jar
```console
$ cd /path/to/scalardb/tools/scalar-schema
$ lein uberjar
```

### Create tables
```console
# For Cosmos DB
$ java -jar target/scalar-schema-standalone-<version>.jar --cosmos -h <YOUR_ACCOUNT_URI> -p <YOUR_ACCOUNT_PASSWORD> -f schema.json [-r BASE_RESOURCE_UNIT]
```
  - `-r BASE_RESOURCE_UNIT` is an option. You can specify the RU of each database. The maximum RU in tables in the database will be set. If you don't specify RU of tables, the database RU will be set with this option. When you use transaction function, the RU of the coordinator table of Scalar DB is specified by this option. By default, it's 400.

```console
# For Cassandra
$ java -jar target/scalar-schema-standalone-<version>.jar --cassandra -h <CASSANDRA_IP> [-P <CASSANDRA_PORT>] [-u <CASSNDRA_USER>] [-p <CASSANDRA_PASSWORD>] -f schema.json [-n <NETWORK_STRATEGY>] [-R <REPLICATION_FACTOR>]
```

  - If `-P <CASSANDRA_PORT>` is not supplied, it defaults to `9042`.
  - If `-u <CASSNDRA_USER>` is not supplied, it defaults to `cassandra`.
  - If `-p <CASSANDRA_PASSWORD>` is not supplied, it defaults to `cassandra`.
  - `<NETWORK_STRATEGY>` should be `SimpleStrategy` or `NetworkTopologyStrategy`

### Delete all tables
```console
# For Cosmos DB
$ java -jar target/scalar-schema-standalone-<version>.jar --cosmos -h <YOUR_ACCOUNT_URI> -p <YOUR_ACCOUNT_PASSWORD> -D
```

```console
# For Cassandra
$ java -jar target/scalar-schema-standalone-<version>.jar --cassandra -h <CASSANDRA_IP> [-P <CASSANDRA_PORT>] [-u <CASSNDRA_USER>] [-p <CASSANDRA_PASSWORD>] -D
```

### Show help
```console
$ java -jar target/scalar-schema-standalone-<version>.jar --help
```

### Sample schema file
```json
{
  "sample-db.sample-table1": {
    "transaction": true,
    "partition-key": [
      "c1"
    ],
    "clustering-key": [
      "c4"
    ],
    "columns": {
      "c1": "INT",
      "c2": "TEXT",
      "c3": "INT",
      "c4": "INT",
      "c5": "BOOLEAN"
    },
    "compaction-strategy": "LCS"
  },

  "sample-db.sample-table2": {
    "transaction": false,
    "partition-key": [
      "c1"
    ],
    "clustering-key": [
      "c3",
      "c4"
    ],
    "columns": {
      "c1": "INT",
      "c2": "TEXT",
      "c3": "INT",
      "c4": "INT",
      "c5": "BOOLEAN"
    },
    "ru": 800
  }
}
```
- `compaction-strategy` should be `STCS`, `LCS` or `TWCS`. This is ignored in Cosmos DB.
- This `ru` value is set for all tables on this database even if `-r BASE_RESOURCE_UNIT` is set when Cosmos DB. `ru` is ignored in Cassandra.
