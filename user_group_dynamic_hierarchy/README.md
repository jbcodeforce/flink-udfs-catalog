# Traverse User Group Hierarchy UDF for Flink

The problem to addres is how to support a hierarchy within the same table, and try to get the users in a group by flattening the hierarchy. As an example there is a group of hospital, with department, and group of people, then persons.

![](./images/group_hierarchy.drawio.png)

* Person can be part of multiple groups
* Group can be part of other group.
* There is one root group
* The hierarchy is dynamic and the number of level may change over time. The hierarchy depth is unknown.


* The unique table keeps group  and user assignment information. Here is the simplest definition:
    ```sql
    CREATE TABLE group_hierarchy (
    id              INT PRIMARY KEY NOT ENFORCED,
    group_name STRING,
    item_name       STRING,
    item_type       STRING NOT NULL,   -- 'GROUP' or 'PERSON'
    ```

With the following insert statements:
```sql
insert into group_hierarchy (id, group_name, item_name, item_type, created_at) values 
(1, 'region_1', CAST(NULL AS STRING), 'GROUP', TO_TIMESTAMP('2021-01-01 00:00:00')),
(2, 'region_1',  'hospital_west', 'GROUP', TO_TIMESTAMP('2021-01-01 00:00:10')),
(3, 'region_1',  'hospital_east', 'GROUP', TO_TIMESTAMP('2021-01-01 00:00:10')),
(4, 'hospital_west',  'department_1', 'GROUP', TO_TIMESTAMP('2021-01-01 00:00:20')),
(5, 'hospital_east',  'department_11', 'GROUP', TO_TIMESTAMP('2021-01-01 00:00:20')),
(6, 'hospital_west',  'nurses_gp_1', 'GROUP', TO_TIMESTAMP('2021-01-01 00:00:20')),
(7, 'department_1',  'nurses_gp_2', 'GROUP', TO_TIMESTAMP('2021-01-01 00:00:20')),
(8, 'department_1',  'Julie', 'PERSON', TO_TIMESTAMP('2021-01-01 00:00:30')),
(9, 'nurses_gp_1',  'Himani', 'PERSON', TO_TIMESTAMP('2021-01-01 00:00:40')),
(10, 'nurses_gp_1',  'Laura', 'PERSON', TO_TIMESTAMP('2021-01-01 00:00:40')),
(11,'nurses_gp_2', 'Bratt', 'PERSON', TO_TIMESTAMP('2021-01-01 00:00:40')),
(12, 'nurses_gp_2', 'Caroll', 'PERSON', TO_TIMESTAMP('2021-01-01 00:00:40')),
(13, 'nurses_gp_2', 'Lucy', 'PERSON', TO_TIMESTAMP('2021-01-01 00:00:40')),
(14, 'nurses_gp_2', 'Mary', 'PERSON', TO_TIMESTAMP('2021-01-01 00:00:40')),
(15, 'department_11', 'Paul', 'PERSON', TO_TIMESTAMP('2021-01-01 00:00:50')),
(16, 'department_11', 'Julie', 'PERSON', TO_TIMESTAMP('2021-01-01 00:00:50')),;
```

* If we extract the following array from the group_hierarchy:

region_1,,GROUP,
region_1,hospital_west,GROUP,
region_1,hospital_east,GROUP,
hospital_west,department_1,GROUP,
hospital_east,department_11,GROUP,
hospital_west,nurses_gp_1,GROUP,
department_1,nurses_gp_2,GROUP,
department_1,Julie,PERSON,
nurses_gp_1,Himani,PERSON,
nurses_gp_1,Laura,PERSON,
nurses_gp_2,Bratt,PERSON,
nurses_gp_2,Caroll,PERSON,
nurses_gp_2,Lucy,PERSON,
nurses_gp_2,Mary,PERSON,
department_11,Paul,PERSON,
department_11,Julie,PERSON

The function needs to accumulate 
* The expected results look like:

| Group | Persons |
| --- | --- |
| nurses_gp_1 | [Himani, Laura] |
| nurses_gp_2 | [Bratt, Carol, Lucy, Mary] |
| Dept_1 | [Bratt, Carol, Julie, Lucy, Mary] |
| Dept_11 | [Paul, Julie] |
| Hospital West | [Bratt, Carol,  Himani, Julie, Laura, Lucy, Mary] |
| Hopital East | [Paul, Julie] |
| Region 1 | [Bratt, Carol,  Himani, Julie, Laura, Lucy, Mary, Paul] |


## Expected usage

```sh
-- First, collect all hierarchy data into an array, then call the UDF
WITH hierarchy_array AS (
    SELECT ARRAY_AGG(ROW(group_name, item_name, item_type)) AS hierarchy_data
    FROM group_hierarchy
)
SELECT 
    node_name,
    USERS_IN_GROUPS(h.hierarchy_data, node_name) AS persons
FROM hierarchy_array h
CROSS JOIN (VALUES ('Region-1')) AS nodes(node_name);
```

## Building the UDF

To build the UDF JAR file:

```bash
mvn clean package
```

The JAR file will be created in the `target` directory.


## Deploying to Confluent Cloud

[See product documentation.](https://docs.confluent.io/cloud/current/flink/concepts/user-defined-functions.html)

* Use the Confluent CLI to upload the jar file. Example
    ```sh
    confluent environment list
    # then in your environment
    confluent flink artifact create sequence --artifact-file target/dynamic-group-hierarchy-udf-1.0-0.jar --cloud aws --region us-west-2 --environment env-nk...
    ```

* Declare the function in the Catalog
```sql
CREATE FUNCTION USERS_IN_GROUPS
AS
'io.confluent.udf.HierarchyTraversal'
USING JAR 'confluent-artifact://cfa-qj...';
```
