WITH hierarchy_array AS (
    SELECT ARRAY_AGG(ROW(group_name, item_name, item_type)) AS hierarchy_data
    FROM group_hierarchy
)
SELECT 
    t.node_name,
    t.persons
FROM hierarchy_array as h, lateral table(USERS_IN_GROUPS(h.hierarchy_data, 'Region-1')) as t(node_name, persons)