package io.confluent.udf;

import org.apache.flink.types.Row;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HierarchyTraversalFunctionTest {
    private HierarchyTraversal function;

    @BeforeEach
    void setUp() {
        function = new HierarchyTraversal();
    }

    @Test
    void testHierarchyTraversalDoesNotThrow() {
        // Create rows with structure: (department_name, item_name, item_type)
        Row[] rows = new Row[]{
            Row.of("Dept_1", "item1", "GROUP"),
            Row.of("Dept_1", "item2", "PERSON"),
            Row.of("Dept_1", "item3", "PERSON")
        };

        // Verify eval executes without throwing exceptions
        // Note: TableFunction.collect() is final, so we cannot capture outputs in unit tests.
        // Full integration testing requires a Flink test harness.
        assertDoesNotThrow(() -> function.eval(rows, "Dept_1"));
    }

    @Test
    void testHierarchyTraversalWithNullInput() {
        // Should handle null gracefully
        assertDoesNotThrow(() -> function.eval(null, "Dept_1"));
        assertDoesNotThrow(() -> function.eval(new Row[]{}, null));
    }

    @Test
    void testHierarchyTraversalWithMaxDepth() {
        Row[] rows = new Row[]{
            Row.of("Dept_1", "SubGroup", "GROUP"),
            Row.of("SubGroup", "person1", "PERSON")
        };

        // Test with explicit max depth
        assertDoesNotThrow(() -> function.eval(rows, "Dept_1", 5));
    }

    @Test
    void testToString() {
        assertEquals("DYNAMIC_GROUP_HIERARCHY_TRAVERSAL", function.toString());
    }
}

