package io.confluent.udf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.ArrayList;

class WithinAreaFunctionTest {
    private WithinAreaFunction withinArea;

    @BeforeEach
    void setUp() {
        withinArea = new WithinAreaFunction();
    }

    @Test
    void testWithinRectangle() {
        List<Double> locations = new ArrayList<Double>();
        locations.add(50.0);
        locations.add(-0.10);
        locations.add(52.0);
        locations.add(-0.15);
        boolean within = withinArea.eval(51.5074, -0.1278, "RECTANGLE", locations, 0.0);
        assertTrue(within); 
    }

    @Test
    void testOutOfRectangle() {
        List<Double> locations = new ArrayList<Double>();
        locations.add(50.0);
        locations.add(-0.10);
        locations.add(52.0);
        locations.add(-0.15);
        boolean within = withinArea.eval(54.5074, -0.1278, "RECTANGLE", locations, 0.0);
        assertFalse(within); 
    }

    @Test
    void testWithinErrorRectangle() {
        List<Double> locations = new ArrayList<Double>();
        locations.add(50.0);
        locations.add(-0.10);
        boolean within = withinArea.eval(51.5074, -0.1278, "RECTANGLE", locations, 0.0);
        assertFalse(within); 
    }

    @Test
    void testWithinCircle() {
        List<Double> locations = new ArrayList<Double>();
        locations.add(50.0);
        locations.add(-0.10);
        //System.out.println(withinArea.haversineMeters(51.5074,  -0.12780, 50.0, -0.10));
        boolean within = withinArea.eval(51.5074, -0.1278, "CIRCLE", locations, 168.0);
        assertTrue(within); 
    }


    @Test
    void testWithinCircleDataErrorNoRadius() {
        List<Double> locations = new ArrayList<Double>();
        locations.add(50.0);
        locations.add(-0.10);
        boolean within = withinArea.eval(51.5074, -0.1278, "CIRCLE", locations, 8.0);
        assertFalse(within); 
        locations.add(52.0);
        locations.add(-0.15);
        within = withinArea.eval(51.5074, -0.1278, "CIRCLE", locations, 188.0);
        assertFalse(within); 
    }

    @Test
    void testWithinCircleDataErrorTooManyLocations() {
        List<Double> locations = new ArrayList<Double>();
        locations.add(50.0);
        locations.add(-0.10);
        locations.add(52.0);
        locations.add(-0.15);
        boolean within = withinArea.eval(51.5074, -0.1278, "CIRCLE", locations, 188.0);
        assertFalse(within); 
    }
    
    @Test
    void testToString() {
        assertEquals("IS_WITHIN_DISTANCE", withinArea.toString());
    }
}
