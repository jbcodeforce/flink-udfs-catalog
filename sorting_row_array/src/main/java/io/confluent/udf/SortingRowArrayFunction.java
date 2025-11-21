package io.confluent.udf;

import org.apache.flink.table.annotation.DataTypeHint;
import org.apache.flink.table.functions.ScalarFunction;
import org.apache.flink.types.Row;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;

/**
 * A Flink UDF that returns a sorted array of ROWs based on the column referenced by the given id.
 * 
 * This scalar function takes an array of ROW elements as input and generates a new array of ROW
 * elements sorted by the value in the column at the specified index.
 * 
 * Example SQL usage:
 * SORT_ROW_ARRAY_ON_ID(ARRAY_AGG(ROW(item_id, item_name, item_description, item_display_order)), 3)
 * 
 * @param rows List of Row objects to be sorted
 * @param columnIndex Zero-based index of the column to sort by
 * @return Sorted array of Row objects, or null if input is invalid
 */
public class SortingRowArrayFunction extends ScalarFunction {
    private static final Logger logger = LogManager.getLogger(SortingRowArrayFunction.class);

    /**
     * Sorts an array of Row objects based on a specified column index.
     * 
     * @param rows The list of Row objects to sort
     * @param columnIndex The zero-based index of the column to sort by
     * @return A sorted array of Row objects, or null if input is invalid
     */
    public @DataTypeHint("ARRAY<ROW <item_id INT, item_name STRING, item_description STRING, item_display_order INT>>") Row[] eval(@DataTypeHint("ARRAY<ROW <item_id INT, item_name STRING, item_description STRING, item_display_order INT>>") Row[] rows, Integer columnIndex) {
        // Validate inputs
        if (rows == null || columnIndex == null) {
            logger.warn("Null input provided: rows={}, columnIndex={}", rows, columnIndex);
            return null;
        }

        if (rows.length == 0) {
            logger.debug("Empty array provided, returning empty array");
            return rows;
        }

        // Validate column index
        if (columnIndex < 0 || columnIndex >= rows[0].getArity()) {
            logger.error("Invalid column index: {}. Row has {} fields.", columnIndex, rows[0].getArity());
            return null;
        }

        try {
            // Sort the rows based on the specified column
            // Using a null-safe comparator to handle null values in the specified column
            Row[] sortedRows = java.util.Arrays.stream(rows)
                .sorted(Comparator.comparing(
                    row -> {
                        Object field = row.getField(columnIndex);
                        @SuppressWarnings("unchecked")
                        Comparable<Object> comparable = field != null ? (Comparable<Object>) field : null;
                        return comparable;
                    },
                    Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .toArray(Row[]::new);

            logger.debug("Successfully sorted {} rows by column index {}", sortedRows.length, columnIndex);
            return sortedRows;

        } catch (ClassCastException e) {
            logger.error("Column at index {} is not comparable: {}", columnIndex, e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Error sorting rows: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Returns a string describing the function.
     */
    @Override
    public String toString() {
        return "SORT_ROW_ARRAY_ON_ID";
    }
}
