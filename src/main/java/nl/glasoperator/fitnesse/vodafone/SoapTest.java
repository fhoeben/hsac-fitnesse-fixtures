package nl.glasoperator.fitnesse.vodafone;

import nl.hsac.fitnesse.fixture.slim.XmlHttpTest;
import nl.hsac.fitnesse.fixture.util.CalendarUtil;

import javax.xml.datatype.XMLGregorianCalendar;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SoapTest extends XmlHttpTest {
    public String xPathDate(String xPathExpr) {
        String nlDate = "";
        String xmlDate = xPath(xPathExpr);
        if (!(xmlDate == null || "".equals(xmlDate))) {
            XMLGregorianCalendar date = new CalendarUtil().buildXMLGregorianCalendar(xmlDate);
            Date cal = date.toGregorianCalendar().getTime();
            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
            nlDate = df.format(cal);
        }
        return nlDate;
    }
}
