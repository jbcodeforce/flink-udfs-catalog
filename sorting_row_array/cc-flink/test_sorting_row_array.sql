
with assets_data (asset_id, item_id, item_name, item_description, item_display_order) as (
    values
        (10, 1,'item1', 'description1', 2 ),
        (20, 2,'item2', 'description2', 1),
        (30, 3,'item3', 'description3', 5),
        (40, 4,'item4', 'description4', 3),
        (50, 5,'item5', 'description5', 4)
)
SELECT
  asset_id,
  ARRAY_AGG(ROW(item_id, item_name, item_description, item_display_order)) AS items
FROM assets_data
GROUP BY asset_id;
