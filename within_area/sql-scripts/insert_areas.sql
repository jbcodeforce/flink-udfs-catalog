-- San Francisco Areas - Mix of Circles and Rectangles
-- Latitude range: ~37.70 to 37.81
-- Longitude range: ~-122.52 to -122.35

-- CIRCLE Areas (using single point + radius)
INSERT INTO areas (area_id, area_type, geo_locations, radius) VALUES 
('sf_downtown', 'CIRCLE', ARRAY[37.78825, -122.40640], 1.0),
('sf_financial_district', 'CIRCLE', ARRAY[37.79483, -122.40142], 0.5),
('sf_golden_gate_park', 'CIRCLE', ARRAY[37.76955, -122.48345], 2.0),
('sf_mission_district', 'CIRCLE', ARRAY[37.75985, -122.41456], 1.5),
('sf_fishermans_wharf', 'CIRCLE', ARRAY[37.80833, -122.41583], 0.8),
('sf_castro', 'CIRCLE', ARRAY[37.76076, -122.43490], 0.7),
('sf_haight_ashbury', 'CIRCLE', ARRAY[37.76985, -122.44631], 0.6),
('sf_chinatown', 'CIRCLE', ARRAY[37.79451, -122.40795], 0.4), 
('sf_presidio', 'CIRCLE', ARRAY[37.79852, -122.46606], 1.8), 
('sf_soma', 'CIRCLE', ARRAY[37.77616, -122.41384], 1.2),
('sf_north_beach_rect', 'RECTANGLE', ARRAY[37.80800, -122.41700, 37.79800, -122.40500], 0.0), 
('sf_marina_rect', 'RECTANGLE', ARRAY[37.81000, -122.44500, 37.79500, -122.43000], 0.0), 
('sf_potrero_hill_rect', 'RECTANGLE', ARRAY[37.76500, -122.40000, 37.75000, -122.38500], 0.0), 
('sf_sunset_district_rect', 'RECTANGLE', ARRAY[37.76000, -122.51000, 37.74000, -122.48500], 0.0), 
('sf_richmond_rect', 'RECTANGLE', ARRAY[37.78500, -122.51000, 37.76500, -122.48000], 0.0), 
('sf_nob_hill_rect', 'RECTANGLE', ARRAY[37.79500, -122.41800, 37.78800, -122.40800], 0.0), 
('sf_russian_hill_rect', 'RECTANGLE', ARRAY[37.80500, -122.42500, 37.79800, -122.41500], 0.0), 
('sf_excelsior_rect', 'RECTANGLE', ARRAY[37.72500, -122.43000, 37.70500, -122.41000], 0.0), 
('sf_bayview_rect', 'RECTANGLE', ARRAY[37.73500, -122.39500, 37.71500, -122.37000], 0.0), 
('sf_twin_peaks_rect', 'RECTANGLE', ARRAY[37.75500, -122.45000, 37.74500, -122.44000], 0.0);

