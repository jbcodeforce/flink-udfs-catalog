
insert into `customers_in_area` select 
  a.area_id,
  c.customer_name,
  c.lat,
  c.lon
from customers as c 
inner join areas a on IS_WITHIN_AREA(c.lat, c.lon, a.area_type, a.geo_locations, a.radius)