package io.confluent.udf;

import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HierarchyTraversalFunctionTest {
    private HierarchyTraversal function;
    private List<Row> collectedRows;

    /**
     * A simple Collector implementation that stores collected rows in a list.
     */
    private static class ListCollector implements Collector<Row> {
        private final List<Row> rows;

        ListCollector(List<Row> rows) {
            this.rows = rows;
        }

        @Override
        public void collect(Row row) {
            rows.add(row);
        }

        @Override
        public void close() {
            // No-op
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        function = new HierarchyTraversal();
        collectedRows = new ArrayList<>();

        // Inject the collector using reflection
        Field collectorField = function.getClass().getSuperclass().getDeclaredField("collector");
        collectorField.setAccessible(true);
        collectorField.set(function, new ListCollector(collectedRows));
    }

    @Test
    void testHierarchyTraversal() throws Exception {
        // Create rows with structure: (group_name, item_name, item_type)
        Row[] rows = new Row[]{
            Row.of("region_1", "NULL", "GROUP"),
            Row.of("region_1", "hospital_west", "GROUP"),
            Row.of("region_1", "hospital_east", "GROUP"),
            Row.of("hospital_west", "department_1", "GROUP"),
            Row.of("hospital_east", "department_11", "GROUP"),
            Row.of("hospital_west", "nurses_gp_1", "GROUP"),
            Row.of("department_1", "nurses_gp_2", "GROUP"),
            Row.of("department_1", "Julie", "PERSON"),
            Row.of("nurses_gp_1", "Himani", "PERSON"),
            Row.of("nurses_gp_1", "Laura", "PERSON"),
            Row.of("nurses_gp_2", "Bratt", "PERSON"),
            Row.of("nurses_gp_2", "Caroll", "PERSON"),
            Row.of("nurses_gp_2", "Lucy", "PERSON"),
            Row.of("nurses_gp_2", "Mary", "PERSON"),
            Row.of("department_11", "Paul", "PERSON"),
            Row.of("department_11", "Julie", "PERSON")

        };
        function.eval(rows);    
        assertEquals(7, collectedRows.size(), "Should find 7 groups");
        collectedRows.forEach(System.out::println);
    }

    @Test
    void testHierarchyTraversalWithNestedGroups() throws Exception {
        Row[] rows = new Row[]{
            Row.of("Dept_1", "SubGroup", "GROUP"),
            Row.of("SubGroup", "person1", "PERSON"),
            Row.of("SubGroup", "person2", "PERSON")
        };

        // Test traversal through nested group
        function.eval(rows);

        // Should find 2 persons in the nested group
        assertEquals(2, collectedRows.size(), "Should find 2 persons in nested group");
    }

    @Test
    void testToString() {
        assertEquals("USERS_IN_GROUPS", function.toString());
    }

    @Test
    void testCompleteScenario() throws Exception {
        System.out.println("testCompleteScenario: start");
        Row[] rows = new Row[]{
            Row.of("region_1", "NULL", "GROUP")
        };

        function.eval(rows);
        collectedRows.forEach(System.out::println);
        System.out.println("testCompleteScenario: add hospital_west to region_1");
        rows = new Row[]{
            Row.of("region_1", "NULL", "GROUP"),
            Row.of("region_1", "hospital_west", "GROUP")
        };
        function.eval(rows);
        collectedRows.forEach(System.out::println);
        System.out.println("testCompleteScenario: end");
    }
}
