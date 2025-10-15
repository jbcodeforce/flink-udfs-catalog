# Sequence Function UDF for Apache Flink

This project implements a User Defined Function (UDF) for Apache Flink that generates a sequence of numbers, similar to SQL's SEQUENCE function. The function can be used in Flink SQL to generate rows with sequential numbers.

## Usage

The SEQUENCE function generates a sequence of numbers between a start and end value (inclusive). It can be used in combination with UNNEST to generate rows in Flink SQL.

### Example Usage

```sql
-- Generate a sequence from 1 to 50
SELECT id 
FROM UNNEST(SEQUENCE(1, 50)) AS t(id);

-- Generate a descending sequence
SELECT id 
FROM UNNEST(SEQUENCE(10, 1)) AS t(id);
```

## Building the UDF

To build the UDF JAR file:

```bash
mvn clean package
```

The JAR file will be created in the `target` directory.

## Features

- Generates ascending or descending sequences
- Handles both Integer and Long inputs
- Null-safe implementation
- Inclusive start and end values

## Implementation Details

The UDF is implemented as a Flink Table Function that:
- Takes start and end parameters (Integer or Long)
- Generates a sequence of Long values
- Handles both ascending and descending sequences
- Properly handles null inputs
- Uses Flink's TableFunction API for optimal integration

## Testing

The implementation includes comprehensive unit tests covering:
- Ascending sequences
- Descending sequences
- Single value sequences
- Null input handling
- Integer input handling

Run the tests using:

```bash
mvn test
```

## Deploying to Confluent Cloud

* Use the Confluent CLI to upload the jar file. Example
    ```sh
    confluent environment list
    # then in your environment
    confluent flink artifact create sequence --artifact-file target/sequence-function-1.0-0.jar --cloud aws --region us-west-2 --environment env-nk...
    ```

* Declare the function in the Catalog
```sql
CREATE FUNCTION SEQUENCE
AS
'io.confluent.udf.SequenceFunction'
USING JAR 'confluent-artifact://cfa-qj...';
```
