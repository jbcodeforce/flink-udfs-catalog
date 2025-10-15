package io.confluent.udf;

import org.apache.flink.table.functions.TableFunction;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SequenceFunctionTest {
    private SequenceFunction function;
    private TestCollector collector;

    private static class TestCollector implements Collector<Long> {
        private final List<Long> collected = new ArrayList<>();

        @Override
        public void collect(Long record) {
            collected.add(record);
        }

        @Override
        public void close() {
            // Not needed for testing
        }

        List<Long> getCollected() {
            return collected;
        }

        void clear() {
            collected.clear();
        }
    }

    @BeforeEach
    void setUp() {
        function = new SequenceFunction();
        collector = new TestCollector();
        function.setCollector(collector);
    }

    @Test
    void testAscendingSequence() {
        function.eval(1L, 5L);
        List<Long> result = collector.getCollected();
        assertEquals(5, result.size());
        assertEquals(List.of(1L, 2L, 3L, 4L, 5L), result);
    }

    @Test
    void testDescendingSequence() {
        function.eval(5L, 1L);
        List<Long> result = collector.getCollected();
        assertEquals(5, result.size());
        assertEquals(List.of(5L, 4L, 3L, 2L, 1L), result);
    }

    @Test
    void testSingleValueSequence() {
        function.eval(1L, 1L);
        List<Long> result = collector.getCollected();
        assertEquals(1, result.size());
        assertEquals(List.of(1L), result);
    }

    @Test
    void testNullInputs() {
        // Explicitly use Long type for null values to avoid ambiguity
        Long nullLong = null;
        
        function.eval(nullLong, 5L);
        assertTrue(collector.getCollected().isEmpty());
        
        collector.clear();
        function.eval(1L, nullLong);
        assertTrue(collector.getCollected().isEmpty());
        
        collector.clear();
        function.eval(nullLong, nullLong);
        assertTrue(collector.getCollected().isEmpty());
    }

    @Test
    void testIntegerInputs() {
        function.eval(1, 5);
        List<Long> result = collector.getCollected();
        assertEquals(5, result.size());
        assertEquals(List.of(1L, 2L, 3L, 4L, 5L), result);
    }
}
