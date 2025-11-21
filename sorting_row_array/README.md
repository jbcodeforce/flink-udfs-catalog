# Sorting Row Array UDF for Apache Flink

This project implements a User-Defined Function (UDF) for Apache Flink that sorts an array of ROW elements based on a specified column index.

## Overview

The `SortingRowArrayFunction` is a scalar function that takes:
- An array of ROW objects
- An integer column index (zero-based)

It returns a new array with the same ROW objects sorted by the values in the specified column.

## Usage

### SQL Example

```sql
-- Example: Sort items by display order (column index 3)
WITH assets_data (asset_id, item_id, item_name, item_description, item_display_order) AS (
    VALUES
        (10, 1, 'item1', 'description1', 2),
        (10, 2, 'item2', 'description2', 1),
        (10, 3, 'item3', 'description3', 5),
        (10, 4, 'item4', 'description4', 3),
        (10, 5, 'item5', 'description5', 4)
)
SELECT
  asset_id,
  SORT_ROW_ARRAY_ON_ID(
    ARRAY_AGG(ROW(item_id, item_name, item_description, item_display_order)),
    3  -- Sort by the 4th column (index 3) - display_order
  ) AS sorted_items
FROM assets_data
GROUP BY asset_id;
```

## Building the UDF

To build the UDF JAR file:

```bash
mvn clean package
```

The JAR file will be created in the `target` directory: `sorting-row-array-udf-1.0-0.jar`

## Deploying to Confluent Cloud

[See product documentation.](https://docs.confluent.io/cloud/current/flink/concepts/user-defined-functions.html)

### Known Limitations

⚠️ **Important**: This UDF currently has type inference limitations on Confluent Cloud. The error "error extracting metadata: Error in extracting a signature to output mapping" may occur because:

1. Confluent Cloud does not support custom type inference
2. Row arrays with dynamic schemas are complex types that require explicit type hints
3. The current implementation uses `@DataTypeHint("ARRAY<RAW>")` which may not be fully supported

### Alternative Approaches

* Using the Confluent Console / Artifacts
    * From artifacts main page, add artifact
        ![](./images/artifact_page.png)
    * Upload the `sorting-row-array-udf-1.0-0.jar` by specifying the Cloud provider and region:
        ![](./images/upload_jar.png) 

1. **Specific Schema Version**: Create a version with a hardcoded schema:
   ```java
   @FunctionHint(
       input = {@DataTypeHint("ARRAY<ROW<id INT, name STRING, desc STRING, order INT>>"), @DataTypeHint("INT")},
       output = @DataTypeHint("ARRAY<ROW<id INT, name STRING, desc STRING, order INT>>")
   )
   ```

2. **Use on Self-Managed Flink**: Deploy to Apache Flink or Confluent Platform where custom type inference is supported.

### Deployment Steps (if supported)

```sh
# List your environments
confluent environment list

# Upload the artifact
confluent flink artifact create sorting_row_array \
  --artifact-file target/sorting-row-array-udf-1.0-0.jar \
  --cloud aws \
  --region us-west-2 \
  --environment env-xxxxx
```

Register the function in Flink SQL:

```sql
CREATE FUNCTION SORT_ROW_ARRAY_ON_ID
AS 'io.confluent.udf.SortingRowArrayFunction'
USING JAR 'confluent-artifact://cfa-xxxxx';
```

## Features

- Sorts arrays of ROW objects by any column
- Handles null values (placed at the end)
- Works with any comparable column type (integers, strings, doubles, etc.)
- Validates input parameters and column indices
- Comprehensive error handling and logging

## Implementation Details

The UDF:
- Uses Java's Stream API for efficient sorting
- Implements null-safe comparators (nulls last)
- Validates column indices against row arity
- Returns null for invalid inputs
- Logs errors for troubleshooting

## Testing

The implementation includes 16 comprehensive unit tests covering:
- Sorting by different column types (integers, strings, doubles, longs)
- Sorting by first, middle, and last columns
- Handling duplicate values
- Handling null values in sort columns
- Edge cases (empty arrays, single rows, null inputs)
- Invalid inputs (negative/out-of-bounds indices)
- Large arrays (100+ rows)
- Complex row structures

Run the tests using:

```bash
mvn test
```

## Requirements

- Java 17 or later
- Apache Flink 1.20.1 or later
- Maven 3.x

## Technical Notes

### Type Hints

The function uses `@DataTypeHint("ARRAY<RAW>")` for both input and output to handle dynamic Row schemas. This approach may have limitations on some platforms.

### Performance

- Time complexity: O(n log n) where n is the array size
- Space complexity: O(n) for the sorted array
- Suitable for arrays up to thousands of elements

### Error Handling

The function returns `null` in these cases:
- Null input array
- Null column index
- Column index out of bounds
- Non-comparable column type
- Any unexpected errors (logged)
