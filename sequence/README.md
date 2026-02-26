# Sequence Function UDF for Apache Flink

This project implements a User Defined Function (UDF) for Apache Flink that generates a sequence of numbers between a start and end value (inclusive), similar to SQL's SEQUENCE function. Use it with LATERAL TABLE in Flink SQL to generate rows with sequential numbers.

## Implementation summary

The `SequenceFunction` is a Table Function (Java class `io.confluent.udf.SequenceFunction`). It takes start and end parameters (Integer or Long) and emits one row per number in the range. It supports ascending and descending sequences, handles null inputs, and uses inclusive start and end values. Uses Flink's TableFunction API.

## Building

The project uses Maven for dependency management and building. To build the project:

```bash
mvn clean package
```

This will create a JAR file in the `target` directory (`target/sequence-function-1.0-0.jar`) that you can use with your Flink application or deploy as a function to Confluent Cloud.

## Testing

The implementation includes unit tests covering:
- Ascending sequences
- Descending sequences
- Single value sequences
- Null input handling
- Integer input handling

To run the tests:

```bash
mvn test
```

## Deployment

### Confluent Cloud for Flink

[See Confluent cloud product documentation.](https://docs.confluent.io/cloud/current/flink/concepts/user-defined-functions.html)

* Be sure to have a user or service account with FlinkDeveloper RBAC to manage workspaces and artifacts.
* Use the Confluent CLI to upload the JAR file. Example:

    ```sh
    confluent login
    confluent environment list
    # then in your environment
    confluent flink artifact create sequence --artifact-file target/sequence-function-1.0-0.jar --cloud aws --region us-west-2 --environment env-nk...
    ```

    Example artifact table after creation:

    ```sh
    +--------------------+--------------+
    | ID                 | cfa-...      |
    | Name               | sequence     |
    | Version            | ver-...      |
    | Cloud              | aws          |
    | Region             | us-west-2    |
    | Environment        | env-...      |
    | Content Format     | JAR          |
    +--------------------+--------------+
    ```

* Register the function in the Flink catalog:

    ```sql
    CREATE FUNCTION SEQUENCE AS 'io.confluent.udf.SequenceFunction' USING JAR 'confluent-artifact://cfa-...';
    ```

### Apache Flink OSS

Add the UDF JAR to the cluster classpath: place `target/sequence-function-1.0-0.jar` in the `lib/` directory of each JobManager and TaskManager, or include it in your job JAR when submitting. Then register the function in a Flink catalog:

```sql
CREATE FUNCTION SEQUENCE AS 'io.confluent.udf.SequenceFunction' USING JAR 'file:///path/to/sequence-function-1.0-0.jar';
```

Or with the Table API: `tEnv.createTemporarySystemFunction("SEQUENCE", SequenceFunction.class);`

## Usage

The SEQUENCE function generates a sequence of numbers between a start and end value (inclusive). Use it with LATERAL TABLE:

```sql
-- Generate a sequence from 1 to 50
SELECT a.*, t.id
FROM table_a a
CROSS JOIN LATERAL TABLE(SEQUENCE(1, 50)) AS t(id);

-- Generate a descending sequence
SELECT a.*, t.id
FROM table_a a
CROSS JOIN LATERAL TABLE(SEQUENCE(10, 1)) AS t(id);
```

## Requirements

- Java 17 or later
- Apache Flink 1.18.1 or later
- Maven 3.x
