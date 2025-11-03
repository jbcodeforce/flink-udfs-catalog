# Within Area

This Apache Flink user defined function helps to assess if a geo location is within an area. The area may be a rectangle between two geo locations, or a circle around a geo location given its radius. The locations will be defined in a table area_id, area_type, geo_locations as an array, radius as optional.

The areas are defined in a table format or json to iterate over.

```json
[{  area_id: 'A1'
    area_type:  'rectangle'
    geo_locations: [lat1, lon1, lat2, lon2]
 },
 {  area_id: 'A2'
    area_type:  'circle'
    geo_locations: [lat1, lon1],
    radius: 10
 },
]
```

For rectangle the function get two points in array (maybe [lat1, lon1], [lat2, lon2]). For Circle area there is a single point in array and radius optional.

If you have *many* areas (e.g. >10 k) and want to test a point against all of them, a single scalar UDF can become a bottleneck.  In that case broadcast the area table and use a `TableFunction` that emits each matching area id. Or pre‑compute a spatial index (e.g. a simple grid or a R‑Tree) and encode it in your data so that the UDF only looks up relevant candidates.

The haversine formula gives ~10 m precision on Earth’s surface – fine for most use cases.  For sub‑meter accuracy use a geodesic library.

For polygons larger than a circle, you can switch to a JTS `Polygon` and use `contains(Point)` – that will require adding JTS to the classpath

As an alternate implementation, th eval function may return the area_id(s) instead of returning a boolean, create a `TableFunction<String>` that `collect(area_id` for each area that contains the point.

## How to use the function

In SQL

```sql
SELECT area_id, is_within_area(user_lat, user_lon, area_id) AS inside
```

Another way to be able to get the area from a table / topic
```sql
SELECT a.area_id
FROM points p
JOIN areas a
ON is_within_area(p.lat, p.lon, a.area_type, a.geo_locations, a.radius);
```

Table API usage:
```java
Table area = tEnv.fromValues(
    Types.ROW(Types.STRING, Types.STRING, Types.ARRAY(Types.DOUBLE), Types.DOUBLE),
    Row.of("a1", "RECTANGLE", new Double[]{40.0, -74.0, 41.0, -73.0}, null),
    Row.of("a2", "CIRCLE", new Double[]{40.5, -73.5}, 5000.0)
);
```


## Building

The project uses Maven for dependency management and building. To build the project:

```bash
mvn clean package
```

This will create a JAR file in the `target` directory that you can use with your Flink application.

## Testing

The project includes unit tests that verify the accuracy of the distance calculations. The tests include:
- Known distance between cities (Paris to London)
- Distance to same point (should be 0)
- Distance between antipodal points (opposite sides of Earth)

To run the tests:

```bash
mvn test
```