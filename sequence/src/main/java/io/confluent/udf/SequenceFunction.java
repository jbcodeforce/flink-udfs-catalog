package io.confluent.udf;

import org.apache.flink.table.functions.TableFunction;
import org.apache.flink.table.annotation.DataTypeHint;
import org.apache.flink.table.annotation.FunctionHint;

/**
 * A Flink Table Function that generates a sequence of numbers between a start and end value.
 * This function can be used in Flink SQL with UNNEST to generate rows, similar to SQL's SEQUENCE function.
 *
 * Example usage in Flink SQL:
 * SELECT id FROM UNNEST(SEQUENCE(1, 50)) AS t(id)
 */
@FunctionHint(output = @DataTypeHint("BIGINT"))
public class SequenceFunction extends TableFunction<Long> {
    
    /**
     * Generates a sequence of numbers from start to end (inclusive).
     *
     * @param start The starting value of the sequence
     * @param end The ending value of the sequence (inclusive)
     */
    public void eval(Long start, Long end) {
        if (start == null || end == null) {
            return;
        }
        
        // Ensure we handle both ascending and descending sequences
        if (start <= end) {
            for (long i = start; i <= end; i++) {
                collect(i);
            }
        } else {
            for (long i = start; i >= end; i--) {
                collect(i);
            }
        }
    }

    /**
     * Overloaded method to handle integer inputs.
     *
     * @param start The starting value of the sequence
     * @param end The ending value of the sequence (inclusive)
     */
    public void eval(Integer start, Integer end) {
        if (start == null || end == null) {
            return;
        }
        eval(start.longValue(), end.longValue());
    }
}
