# Explode Function UDF for Apache Flink

This project implements a User Defined Function (UDF) for Apache Flink that generates one row per string from a list or array of strings. Use it with LATERAL TABLE in Flink SQL to expand arrays into rows.

## Implementation summary

The `ExplodeFunction` is a Table Function (Java class `io.confluent.udf.ExplodeFunction`). It takes an array of strings as input and emits one row per string. Use it in combination with JOIN LATERAL to generate rows in Flink SQL.

## Building

The project uses Maven for dependency management and building. To build the project:

```bash
mvn clean package
```

This will create a JAR file in the `target` directory (`target/explode-1.0-0.jar`) that you can use with your Flink application or deploy as a function to Confluent Cloud.

## Testing

The project includes unit tests that verify:
- One row emitted per element in the input array
- Empty array and null handling
- Correct output column type

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
    confluent flink artifact create explode --artifact-file target/explode-1.0-0.jar --cloud aws --region us-west-2 --environment env-nk...
    ```

    Example artifact table after creation:

    ```sh
    +--------------------+--------------+
    | ID                 | cfa-...      |
    | Name               | explode      |
    | Version            | ver-...      |
    | Cloud              | aws          |
    | Region             | us-west-2    |
    | Environment        | env-...      |
    | Content Format     | JAR          |
    +--------------------+--------------+
    ```

* Register the function in the Flink catalog:

    ```sql
    CREATE FUNCTION EXPLODE AS 'io.confluent.udf.ExplodeFunction' USING JAR 'confluent-artifact://cfa-...';
    ```

### Apache Flink OSS

Add the UDF JAR to the cluster classpath: place `target/explode-1.0-0.jar` in the `lib/` directory of each JobManager and TaskManager, or include it in your job JAR when submitting. Then register the function in a Flink catalog:

```sql
CREATE FUNCTION EXPLODE AS 'io.confluent.udf.ExplodeFunction' USING JAR 'file:///path/to/explode-1.0-0.jar';
```

Or with the Table API: `tEnv.createTemporarySystemFunction("EXPLODE", ExplodeFunction.class);`

## Usage

The EXPLODE function generates one row per string in the list or array of strings provided as input. Use it with LATERAL TABLE:

```sql
SELECT t.sub_string
FROM LATERAL TABLE(EXPLODE(ARRAY['ab','bc','cd'])) AS t(sub_string);
```

## Requirements

- Java 17 or later
- Apache Flink 1.18.1 or later
- Maven 3.x
