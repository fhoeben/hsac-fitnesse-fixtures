package nl.hsac.fitnesse.fixture.util;

import fit.exception.FitFailureException;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper to evaluate XPath expressions.
 */
public class XPathHelper {
    /**
     * Evaluates xPathExpr against xml, returning single match.
     * @param xml xml document to apply XPath to.
     * @param xPathExpr XPath expression to evaluate.
     * @return result of evaluation, null if xml is null.
     */
    public String getXPath(NamespaceContext context, String xml, String xPathExpr) {
        return (String) evaluateXpath(context, xml, xPathExpr, null);
    }
    
    
    /**
     * Evaluates xPathExpr against xml, returning all matches.
     * @param xml xml document to apply XPath to.
     * @param xPathExpr XPath expression to evaluate.
     * @return text() of all nodes matching XPath, null if xml is null.
     */
    public List<String> getAllXPath(NamespaceContext context, String xml, String xPathExpr) {
        List<String> result = null;
        
        NodeList nodes = (NodeList) evaluateXpath(context, xml, xPathExpr, XPathConstants.NODESET);
        if (nodes != null) {
            result = new ArrayList<String>(nodes.getLength());
            for (int i = 0; i < nodes.getLength(); i++) {
                result.add(nodes.item(i).getNodeValue());
            }
        }

        return result;
    }
    
    private Object evaluateXpath(NamespaceContext context, String xml, String xPathExpr, QName returnType) {
        Object result = null;
        if (xml != null) {
            if (!xml.startsWith("<")) {
                throw new FitFailureException("Cannot perform XPATH on non-xml: " + xml);
            }
            XPathExpression expr = createXPathExpression(context, xPathExpr);
            try {
                result = evaluateXpath(xml, expr, returnType);
            } catch (XPathExpressionException e) {
                String msg = getMessage(e);
                throw new FitFailureException("Unable to evaluate xpath: " + xPathExpr + "\n" + msg);
            }
        }
        return result;
    }

    protected XPathExpression createXPathExpression(NamespaceContext context, String xPathExpr) {
        XPath xpath = createXPath(context);
        try {
            return xpath.compile(xPathExpr);
        } catch (XPathExpressionException e) {
            String msg = getMessage(e);
            throw new FitFailureException("Unable to compile xpath: " + xPathExpr + "\n" + msg);
        }
    }

    protected XPath createXPath(NamespaceContext context) {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        if (context != null) {
            xpath.setNamespaceContext(context);
        }
        return xpath;
    }

    protected Object evaluateXpath(String xml, XPathExpression expr, QName returnType) throws XPathExpressionException {
        Object result;
        InputSource source = new InputSource(new StringReader(xml));

        if (returnType != null) {
            result = expr.evaluate(source, returnType);
        } else {
            result = expr.evaluate(source);
        }
        return result;
    }

    private String getMessage(XPathExpressionException e) {
        String msg;
        Throwable t = e;
        do {
            msg = t.getMessage();
            t = t.getCause();
        } while (msg == null && t != null);
        return msg;
    }
}
