package nl.hsac.fitnesse.fixture.fit;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;



/**
  * Log information.
 */
public class LogFixture extends MapColumnFixture {

    public String timeStamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new GregorianCalendar().getTime());
    }

}

