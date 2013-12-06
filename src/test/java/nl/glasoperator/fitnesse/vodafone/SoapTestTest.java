package nl.glasoperator.fitnesse.vodafone;

import static org.junit.Assert.*;

import nl.hsac.fitnesse.fixture.UnitTestHelper;
import nl.hsac.fitnesse.fixture.util.XmlHttpResponse;
import org.junit.Before;
import org.junit.Test;

/**
 * SoapTest test.
 */
public class SoapTestTest {
    private SoapTest test;

    @Before
    public void setUp() {
        test = new SoapTest();
        test.registerPrefixForNamespace("vispEx", "http://glasoperator.nl/VispPortalSoap/2013/01");
    }

    @Test
    public void testOneOffCosts() {
        fillResponseLargeOrder();
        String am = test.xPathAmount("sum(//vispEx:OfferLine[vispEx:CategoryId = 'O']/vispEx:Price)");
        assertEquals("€ 30,00", am);
    }

    @Test
    public void testMonthlyCosts() {
        fillResponseLargeOrder();
        String am  = test.xPathAmount("sum(//vispEx:OfferLine[vispEx:CategoryId = 'P']/vispEx:Price)");
        assertEquals("€ 130,83", am);
    }

    @Test
    public void testOneOffDiscount() {
        fillResponseLargeOrder();
        String am  = test.xPathAmount("sum(//vispEx:OfferLine[vispEx:CategoryId = 'O' and vispEx:Price < 0]/vispEx:Price)");
        assertEquals("€ -75,00", am);
    }

    private void fillResponseLargeOrder() {
        XmlHttpResponse resp = test.getResponse();
        UnitTestHelper.fillResponse(resp, "vodafone/largeOrderOffer.xml");
    }
}
