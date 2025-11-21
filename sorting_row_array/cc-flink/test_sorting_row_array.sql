-- results:
-- 10 [1,item1,description1,2, 4,item4,description4,3, 3,item3,description3,5]
-- 20 [2,item2,description2,1, 5,item5,description5,4]

with assets_data (asset_id, item_id, item_name, item_description, item_display_order) as (
    values
        (10, 1,'item1', 'description1', 2 ),
        (20, 2,'item2', 'description2', 1),
        (10, 3,'item3', 'description3', 5),
        (10, 4,'item4', 'description4', 3),
        (20, 5,'item5', 'description5', 4)
)
SELECT
  asset_id,
  SORT_ROW_ARRAY_ON_ID(ARRAY_AGG(ROW(item_id, item_name, item_description, item_display_order)),3) AS items
FROM assets_data
GROUP BY asset_id;
