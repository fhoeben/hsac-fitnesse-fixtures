package nl.hsac.fitnesse.fixture.fit;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import javax.xml.datatype.XMLGregorianCalendar;

import nl.hsac.fitnesse.fixture.util.CalendarUtil;



/**
  * Log information.
 */
public class LogFixture extends MapColumnFixture {
    private CalendarUtil calendarUtil = new CalendarUtil();

    public String timeStamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        XMLGregorianCalendar now = calendarUtil.buildXMLGregorianCalendarDateTime(new GregorianCalendar());
        return dateFormat.format(now.toGregorianCalendar().getTime());

    }

}

