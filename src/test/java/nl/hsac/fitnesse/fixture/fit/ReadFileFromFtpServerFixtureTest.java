package nl.hsac.fitnesse.fixture.fit;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests for {@link ReadFileFromFtpServerFixture}.
 *
 */
public class ReadFileFromFtpServerFixtureTest {
    private ReadFileFromFtpServerFixture fixture = new ReadFileFromFtpServerFixture();

    /**
     * Test isFtp.
     */
    @Test
    public void testIsFtp() {
        assertFalse(fixture.isSftp());

        fixture.getCurrentRowValues().put(ReadFileFromFtpServerFixture.IS_SFTP, "false");
        assertFalse(fixture.isSftp());

        fixture.getCurrentRowValues().put(ReadFileFromFtpServerFixture.IS_SFTP, "0");
        assertFalse(fixture.isSftp());

        fixture.getCurrentRowValues().put(ReadFileFromFtpServerFixture.IS_SFTP, "random value");
        assertFalse(fixture.isSftp());

        fixture.getCurrentRowValues().put(ReadFileFromFtpServerFixture.IS_SFTP, "true");
        assertTrue(fixture.isSftp());

        fixture.getCurrentRowValues().put(ReadFileFromFtpServerFixture.IS_SFTP, "1");
        assertTrue(fixture.isSftp());

        fixture.getCurrentRowValues().put(ReadFileFromFtpServerFixture.IS_SFTP, "yes");
        assertTrue(fixture.isSftp());

        fixture.getCurrentRowValues().put(ReadFileFromFtpServerFixture.IS_SFTP, "yEs");
        assertTrue(fixture.isSftp());

        fixture.getCurrentRowValues().put(ReadFileFromFtpServerFixture.IS_SFTP, "TRUE");
        assertTrue(fixture.isSftp());
    }

}
