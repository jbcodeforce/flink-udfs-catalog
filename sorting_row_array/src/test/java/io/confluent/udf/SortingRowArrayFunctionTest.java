package io.confluent.udf;

import org.apache.flink.types.Row;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SortingRowArrayFunctionTest {
    private SortingRowArrayFunction function;

    @BeforeEach
    void setUp() {
        function = new SortingRowArrayFunction();
    }

    @Test
    void testSortByIntegerColumn() {
        // Create rows with structure: (id, name, value)
        Row[] rows = new Row[]{
            Row.of(3, "item3", 300),
            Row.of(1, "item1", 100),
            Row.of(2, "item2", 200)
        };

        // Sort by first column (index 0 - the id)
        Row[] result = function.eval(rows, 0);

        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals(1, result[0].getField(0));
        assertEquals(2, result[1].getField(0));
        assertEquals(3, result[2].getField(0));
    }

    @Test
    void testSortByStringColumn() {
        // Create rows with structure: (id, name, value)
        Row[] rows = new Row[]{
            Row.of(1, "Charlie", 300),
            Row.of(2, "Alice", 100),
            Row.of(3, "Bob", 200)
        };

        // Sort by second column (index 1 - the name)
        Row[] result = function.eval(rows, 1);

        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals("Alice", result[0].getField(1));
        assertEquals("Bob", result[1].getField(1));
        assertEquals("Charlie", result[2].getField(1));
    }

    @Test
    void testSortByLastColumn() {
        // Test sorting by the last column in the row
        Row[] rows = new Row[]{
            Row.of("A", "item1", 30),
            Row.of("B", "item2", 10),
            Row.of("C", "item3", 20)
        };

        // Sort by third column (index 2 - the value)
        Row[] result = function.eval(rows, 2);

        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals(10, result[0].getField(2));
        assertEquals(20, result[1].getField(2));
        assertEquals(30, result[2].getField(2));
    }

    @Test
    void testSortWithDuplicateValues() {
        // Test sorting when multiple rows have the same value in the sort column
        Row[] rows = new Row[]{
            Row.of(1, "item1", 100),
            Row.of(2, "item2", 100),
            Row.of(3, "item3", 50)
        };

        Row[] result = function.eval(rows, 2);

        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals(50, result[0].getField(2));
        // The two rows with value 100 should be present (order between them is not guaranteed)
        assertEquals(100, result[1].getField(2));
        assertEquals(100, result[2].getField(2));
    }

    @Test
    void testSortWithNullValuesInSortColumn() {
        // Test that null values in the sort column are handled (placed at the end)
        Row[] rows = new Row[]{
            Row.of(1, "item1", 100),
            Row.of(2, "item2", null),
            Row.of(3, "item3", 50)
        };

        Row[] result = function.eval(rows, 2);

        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals(50, result[0].getField(2));
        assertEquals(100, result[1].getField(2));
        assertNull(result[2].getField(2)); // Null should be last
    }

    @Test
    void testSingleRowArray() {
        // Test with single row
        Row[] rows = new Row[]{
            Row.of(1, "item1", 100)
        };

        Row[] result = function.eval(rows, 0);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(1, result[0].getField(0));
    }

    @Test
    void testEmptyArray() {
        // Test with empty array
        Row[] rows = new Row[0];

        Row[] result = function.eval(rows, 0);

        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void testNullRowArray() {
        // Test with null input array
        Row[] result = function.eval(null, 0);
        assertNull(result);
    }

    @Test
    void testNullColumnIndex() {
        // Test with null column index
        Row[] rows = new Row[]{
            Row.of(1, "item1", 100)
        };

        Row[] result = function.eval(rows, null);
        assertNull(result);
    }

    @Test
    void testInvalidColumnIndexNegative() {
        // Test with negative column index
        Row[] rows = new Row[]{
            Row.of(1, "item1", 100)
        };

        Row[] result = function.eval(rows, -1);
        assertNull(result);
    }

    @Test
    void testInvalidColumnIndexTooHigh() {
        // Test with column index greater than row arity
        Row[] rows = new Row[]{
            Row.of(1, "item1", 100)
        };

        Row[] result = function.eval(rows, 5);
        assertNull(result);
    }

    @Test
    void testSortWithDoubleValues() {
        // Test sorting with double values
        Row[] rows = new Row[]{
            Row.of("A", 3.5),
            Row.of("B", 1.2),
            Row.of("C", 2.8)
        };

        Row[] result = function.eval(rows, 1);

        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals(1.2, (Double) result[0].getField(1), 0.001);
        assertEquals(2.8, (Double) result[1].getField(1), 0.001);
        assertEquals(3.5, (Double) result[2].getField(1), 0.001);
    }

    @Test
    void testSortWithLongValues() {
        // Test sorting with long values
        Row[] rows = new Row[]{
            Row.of("A", 3000L),
            Row.of("B", 1000L),
            Row.of("C", 2000L)
        };

        Row[] result = function.eval(rows, 1);

        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals(1000L, result[0].getField(1));
        assertEquals(2000L, result[1].getField(1));
        assertEquals(3000L, result[2].getField(1));
    }

    @Test
    void testComplexRowStructure() {
        // Test with a more complex row structure similar to the README example
        // ROW(item_id, item_name, item_description, item_display_order)
        Row[] rows = new Row[]{
            Row.of(101, "Product A", "Description A", 3),
            Row.of(102, "Product B", "Description B", 1),
            Row.of(103, "Product C", "Description C", 2)
        };

        // Sort by display order (index 3)
        Row[] result = function.eval(rows, 3);

        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals(1, result[0].getField(3));
        assertEquals(2, result[1].getField(3));
        assertEquals(3, result[2].getField(3));
        
        // Verify the complete rows are correctly ordered
        assertEquals("Product B", result[0].getField(1));
        assertEquals("Product C", result[1].getField(1));
        assertEquals("Product A", result[2].getField(1));
    }

    @Test
    void testLargeArray() {
        // Test with a larger array
        Row[] rows = new Row[100];
        for (int i = 0; i < 100; i++) {
            rows[i] = Row.of(99 - i, "item" + i);
        }

        Row[] result = function.eval(rows, 0);

        assertNotNull(result);
        assertEquals(100, result.length);
        
        // Verify it's sorted
        for (int i = 0; i < 99; i++) {
            int current = (Integer) result[i].getField(0);
            int next = (Integer) result[i + 1].getField(0);
            assertTrue(current <= next, "Array should be sorted in ascending order");
        }
    }

    @Test
    void testToString() {
        assertEquals("SORT_ROW_ARRAY_ON_ID", function.toString());
    }
}

