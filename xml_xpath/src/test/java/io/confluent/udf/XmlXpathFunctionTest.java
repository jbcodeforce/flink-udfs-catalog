package io.confluent.udf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class XmlXpathFunctionTest {

    private XmlXpathFunction xpathString;

    private static final String SAMPLE_XML = "<?xml version=\"1.0\"?>"
            + "<POSLog>"
            + "  <Transaction CancelFlag=\"false\" ABCSubTransactionType=\"SERVICE_TIP\">"
            + "    <LineItem Id=\"1\">Widget</LineItem>"
            + "  </Transaction>"
            + "</POSLog>";

    @BeforeEach
    void setUp() {
        xpathString = new XmlXpathFunction();
    }

    @Test
    void testExtractElementText() {
        String result = xpathString.eval(SAMPLE_XML, "/*[name()=\"POSLog\"]/*[name()=\"Transaction\"]/*[name()=\"LineItem\"]");
        assertEquals("Widget", result);
    }

    @Test
    void testExtractAttribute() {
        String result = xpathString.eval(SAMPLE_XML, "/*[name()=\"POSLog\"]/*[name()=\"Transaction\"]/@CancelFlag");
        assertEquals("false", result);
    }

    @Test
    void testExtractSecondAttribute() {
        String result = xpathString.eval(SAMPLE_XML, "/*[name()=\"POSLog\"]/*[name()=\"Transaction\"]/@ABCSubTransactionType");
        assertEquals("SERVICE_TIP", result);
    }

    @Test
    void testNoMatchReturnsNull() {
        assertNull(xpathString.eval(SAMPLE_XML, "/*[name()=\"NonExistent\"]"));
        assertNull(xpathString.eval(SAMPLE_XML, "/*[name()=\"POSLog\"]/*[name()=\"Transaction\"]/@MissingAttr"));
    }

    @Test
    void testNullXmlReturnsNull() {
        assertNull(xpathString.eval(null, "/*"));
    }

    @Test
    void testNullXpathReturnsNull() {
        assertNull(xpathString.eval(SAMPLE_XML, null));
    }

    @Test
    void testEmptyXmlReturnsNull() {
        assertNull(xpathString.eval("", "/*"));
        assertNull(xpathString.eval("   ", "/*"));
    }

    @Test
    void testEmptyXpathReturnsNull() {
        assertNull(xpathString.eval(SAMPLE_XML, ""));
        assertNull(xpathString.eval(SAMPLE_XML, "   "));
    }

    @Test
    void testMalformedXmlReturnsNull() {
        assertNull(xpathString.eval("<root><unclosed>", "/*"));
        assertNull(xpathString.eval("not xml at all", "/*"));
    }

    @Test
    void testToString() {
        assertEquals("XPATH_STRING", xpathString.toString());
    }
}
