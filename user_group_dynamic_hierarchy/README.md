# Traverse User Group Hierarchy UDF for Flink




```sh
-- Register the function
CREATE FUNCTION get_hierarchy_persons AS 'io.confluent.flink.udf.HierarchyTraversal';

-- First, collect all hierarchy data into an array, then call the UDF
WITH hierarchy_array AS (
    SELECT ARRAY_AGG(ROW(group_name, item_name, item_type)) AS hierarchy_data
    FROM group_hierarchy
)
SELECT 
    node_name,
    get_hierarchy_persons(h.hierarchy_data, node_name) AS persons
FROM hierarchy_array h
CROSS JOIN (VALUES ('Region-1')) AS nodes(node_name);
```