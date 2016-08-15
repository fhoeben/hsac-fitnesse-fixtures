package nl.hsac.fitnesse.junit.allure;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by Tom on 15-8-2016.
 */
public class TestObjectVideo {
    public byte[] video(String user, String project, String testID) throws Exception {
        String videoLocation = String.format("https://app.testobject.com/api/rest/users/%s/projects/%s/video/%s", user, project, testID);
        byte[] bytes = null;
        URL videoUrl = new URL(videoLocation);
        InputStream is = null;
        try{
            is = videoUrl.openStream();
            bytes = IOUtils.toByteArray(is);
        } catch (IOException e) {
        e.printStackTrace();
    } finally {
        if(is != null){
            is.close();
        }
    }
    return bytes;
    }

}
