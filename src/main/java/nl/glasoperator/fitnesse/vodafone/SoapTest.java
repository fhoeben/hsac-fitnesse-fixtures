package nl.glasoperator.fitnesse.vodafone;

import nl.hsac.fitnesse.fixture.slim.XmlHttpTest;
import nl.hsac.fitnesse.fixture.util.CalendarUtil;

import javax.xml.datatype.XMLGregorianCalendar;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SoapTest extends XmlHttpTest {
    private static final Locale NL = new Locale("nl");

    public String xPathDate(String xPathExpr) {
        String nlDate = null;
        String xmlDate = xPath(xPathExpr);
        if (!(xmlDate == null || "".equals(xmlDate))) {
            XMLGregorianCalendar date = new CalendarUtil().buildXMLGregorianCalendar(xmlDate);
            Date cal = date.toGregorianCalendar().getTime();
            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
            nlDate = df.format(cal);
        }
        return nlDate;
    }

    public String setValueForTrim(String value, String name, String postFix) {
        String valueToSet;
        if (value.endsWith(postFix)) {
            valueToSet = value.substring(0, value.length() - postFix.length());
        } else {
            valueToSet = value;
        }
        setValueFor(valueToSet, name);
        return valueToSet;
    }

    public String xPathAmount(String xPath) {
        String result = null;
        Double amount = xPathDouble(xPath);
        if (amount != null) {
            DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance(NL);
            decimalFormat.applyPattern("0.00");
            result = "€ " + decimalFormat.format(amount);
        }
        return result;
    }

    public boolean connectDateBeforeApril() {
        boolean result = true;
        String actualConnectDate = xPath("//vispEx:OrderProduct[vispEx:ProductTypeId = 'INTA']/vispEx:WishDt");
        if (new CalendarUtil().buildXMLGregorianCalendar().toXMLFormat().compareTo("2014-04-01") < 0) {
            result = actualConnectDate.compareTo("2014-04-01") < 0;
        }
        return result;
    }

}
