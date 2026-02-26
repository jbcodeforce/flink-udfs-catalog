package io.confluent.udf;

import org.apache.flink.table.functions.ScalarFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import org.xml.sax.InputSource;

/**
 * Flink scalar UDF that extracts content from an XML document column using an XPath expression.
 * Returns the string value of the first matching node (element text or attribute value).
 * Use it multiple times in a SELECT with different XPath expressions to create matching columns.
 *
 * <p>Usage in SQL (register as xpath_string):
 * <pre>
 * SELECT
 *   xpath_string(PAYLOAD, '/*[name()="POSLog"]/*[name()="Transaction"]/@CancelFlag') AS cancel_flag,
 *   xpath_string(PAYLOAD, '/*[name()="POSLog"]/*[name()="Transaction"]/@ABCSubTransactionType') AS sub_type
 * FROM my_table
 * WHERE xpath_string(PAYLOAD, '...') = 'false';
 * </pre>
 */
public class XmlXpathFunction extends ScalarFunction {
    private static final Logger logger = LogManager.getLogger(XmlXpathFunction.class);

    /**
     * Evaluates an XPath expression against an XML document string and returns the string value
     * of the first matching node (element text content or attribute value).
     *
     * @param xml            the XML document as a string (e.g. a payload column)
     * @param xpathExpression XPath expression (e.g. "/*[name()=\"POSLog\"]/*[name()=\"Transaction\"]/@CancelFlag")
     * @return the extracted string, or null if xml/xpath is null/blank, no match, or on parse/eval error
     */
    public String eval(String xml, String xpathExpression) {
        if (xml == null || xml.isBlank() || xpathExpression == null || xpathExpression.isBlank()) {
            return null;
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));

            XPath xpath = XPathFactory.newInstance().newXPath();
            Object result = xpath.evaluate(xpathExpression, doc, XPathConstants.NODESET);
            NodeList nodes = (NodeList) result;
            if (nodes == null || nodes.getLength() == 0) {
                return null;
            }
            org.w3c.dom.Node first = nodes.item(0);
            String value = first.getTextContent();
            if (value == null || value.isBlank()) {
                value = first.getNodeValue();
            }
            if (value == null || value.isBlank()) {
                return null;
            }
            return value.trim();
        } catch (Exception e) {
            logger.debug("XPath evaluation failed: xml length={}, xpath={}, error={}",
                    xml.length(), xpathExpression, e.getMessage());
            return null;
        }
    }

    @Override
    public String toString() {
        return "XPATH_STRING";
    }
}
