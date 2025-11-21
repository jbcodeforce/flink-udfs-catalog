# User Defined Function for Flink

This repository will group of User Defined Functions for Flink. Some functions are easy to understand and to replicate and may be used in production.

| User Defined Function | Description | Path |
| --- | --- | --- |
| GEO_DISTANCE | Compute the distance using the Haversine function between two geo positions on earth | [geo_distance](./geo_distance/) |
| WITHIN_AREA | Assess if a geo location is within an area. Areas are defined in a table as reference data. | [within_area](./within_area/) |
| SEQUENCE | generates a sequence of numbers, used in Flink SQL to generate rows with sequential numbers | [sequence](./sequence/) |
| SORT_ROW_ARRAY_ON_ID | returns a sorted array of ROWs based on the column referenced by the given id | [sorting_row_array](./sorting_row_array/) |


## Documentation References

* [Apache Flink UDF chapter](https://nightlies.apache.org/flink/flink-docs-master/docs/dev/table/functions/udfs/)
* [Confluent Cloud UDF documentation](https://docs.confluent.io/cloud/current/flink/how-to-guides/create-udf.html)

## Apache Flink Packaging

* If the UDF is specific to a particular Flink job, you can include the UDF JAR within the main job JAR when submitting it.
* For UDFs intended for broader use or across multiple jobs, you can add the UDF JAR to the Flink cluster's classpath by placing it in the lib directory of each Flink TaskManager and JobManager. 
* If using Flink SQL, you can register the UDF in a catalog

## Future UDFs Candidates

* Async call to a DB for lookup - which may not be a good idea for Confluent Cloud.
* Compute Max of values included in a JSON object -> need to specify in detail



