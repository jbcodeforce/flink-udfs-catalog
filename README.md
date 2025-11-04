# User Defined Function for Flink

This repository will group of User Defined Functions for Flink. Some functions are easy to understand and to replicate and may be used in production.

| User Defined Function | Description | Path |
| --- | --- | --- |
| GEO_DISTANCE | Compute the distance using the Haversine function between two geo positions on earth | [geo_distance](./geo_distance/) |
| WITHIN_AREA | Assess if a geo location is within an area. Areas are defined in a table as reference data. | [within_area](./within_area/) |
| SEQUENCE | generates a sequence of numbers, used in Flink SQL to generate rows with sequential numbers | [sequence](./sequence/) |


## UDFs Candidates

* Async call to a DB for lookup - which may not be a good idea for Confluent Cloud.
* Compute Max of values included in a JSON object -> need to specify in detail



