package nl.hsac.fitnesse.fixture.util;

import java.io.StringReader;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.InputSource;

import fit.exception.FitFailureException;

/**
 * Helper to evaluate XPath expressions.
 */
public class XPathHelper {
    /**
     * Evaluates xPathExpr against xml.
     * @param xml xml document to apply XPath to.
     * @param xPathExpr XPath expression to evaluate.
     * @return result of evaluation, null if xml is null.
     */
    public static String getXPath(NamespaceContext context, String xml, String xPathExpr) {
        String result = null;
        if (xml != null) {
            if (!xml.startsWith("<")) {
                throw new FitFailureException("Cannot perform XPATH on non-xml: " + xml);
            }
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            if (context != null) {
                xpath.setNamespaceContext(context);
            }
            try {
                XPathExpression expr = xpath.compile(xPathExpr);
                InputSource source = new InputSource(new StringReader(xml));
                
                result = expr.evaluate(source);
            } catch (XPathExpressionException e) {
                throw new FitFailureException("Unable to evaluate xpath: " + xPathExpr);
            }
        }
        return result;
    }
}
