# XML XPath UDF for Apache Flink

This User Defined Function extracts content from a column containing an XML document using an XPath expression and returns it as a string. Use the function multiple times in a SELECT with different XPath expressions to create matching columns (one column per extraction). Register the function in your Flink catalog as `xpath_string` (Java class: `io.confluent.udf.XmlXpathFunction`).

## Implementation summary

The `XmlXpathFunction` is a scalar function that exposes `eval(String xml, String xpathExpression)` returning the string value of the first matching node (element text or attribute value). Parsing and XPath use JDK JAXP only (DocumentBuilder, XPathFactory). Null or blank xml/xpath returns null; parsing or evaluation errors are logged and return null so the pipeline does not fail. For documents with default or explicit XML namespaces, use namespace-agnostic XPath such as `name()` or `local-name()` (e.g. `/*[name()="POSLog"]`).

## Building

The project uses Maven for dependency management and building. To build the project:

```bash
mvn clean package
```

This will create a JAR file in the `target` directory (`target/xml-xpath-udf-1.0-0.jar`) that you can use with your Flink application or deploy as a function to Confluent Cloud.

## Testing

The project includes unit tests that verify:
- Extraction of element text and attributes
- No match returns null
- Null or empty xml/xpath returns null
- Malformed XML returns null (no uncaught exception)

To run the tests:

```bash
mvn test
```

## Deployment

### Confluent Cloud for Flink

[See Confluent cloud product documentation.](https://docs.confluent.io/cloud/current/flink/concepts/user-defined-functions.html)

* Be sure to have a user or service account with FlinkDeveloper RBAC to manage workspaces and artifacts.
* Upload the JAR via Confluent Console > Artifacts > Upload artifact, or use the Confluent CLI:

    ```sh
    confluent login
    confluent environment list
    # then in your environment
    confluent flink artifact create xml-xpath-udf --artifact-file target/xml-xpath-udf-1.0-0.jar --cloud aws --region us-west-2 --environment env-nk...
    ```

    ![](./images/upload-artifact.png)

    Note the artifact unique identifier after upload.

* Register the function in the Flink catalog:

    ```sql
    CREATE FUNCTION XPATH_STRING AS 'io.confluent.udf.XmlXpathFunction' USING JAR 'confluent-artifact://cfa-...';
    ```

* Example test in Confluent Cloud: insert sample XML rows into your table and run a SELECT using XPATH_STRING to extract attributes. Example output: ![](./images/execution.png)

### Apache Flink OSS

Add the UDF JAR to the cluster classpath: place `target/xml-xpath-udf-1.0-0.jar` in the `lib/` directory of each JobManager and TaskManager, or include it in your job JAR when submitting. Then register the function in a Flink catalog:

```sql
CREATE FUNCTION XPATH_STRING AS 'io.confluent.udf.XmlXpathFunction' USING JAR 'file:///path/to/xml-xpath-udf-1.0-0.jar';
```

Or with the Table API: `tEnv.createTemporarySystemFunction("XPATH_STRING", XmlXpathFunction.class);`

## Usage

The input record is generic (e.g. a payload column). Example table and query:

```sql
CREATE TABLE my_table (payload STRING);
```

```sql
SELECT
  XPATH_STRING(PAYLOAD, '/*[name()="POSLog"]/*[name()="Transaction"]/@CancelFlag') AS cancel_flag,
  XPATH_STRING(PAYLOAD, '/*[name()="POSLog"]/*[name()="Transaction"]/@ABCSubTransactionType') AS sub_type
FROM stores_storedigital_retail_saletransactions
WHERE XPATH_STRING(PAYLOAD, '/*[name()="POSLog"]/*[name()="Transaction"]/@CancelFlag') = 'false'
  AND XPATH_STRING(PAYLOAD, '/*[name()="POSLog"]/*[name()="Transaction"]/@ABCSubTransactionType') = 'SERVICE_TIP';
```

For documents with default or explicit XML namespaces, use namespace-agnostic XPath such as `name()` or `local-name()` so element names match without binding a namespace prefix.

## Requirements

- Java 17 or later
- Apache Flink 1.18.1 or later
- Maven 3.x
