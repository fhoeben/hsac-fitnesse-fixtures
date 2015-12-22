package nl.hsac.fitnesse.fixture.fit;

import nl.hsac.fitnesse.fixture.util.FtpFileUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;


public class ReadFileFromFtpServerFixture extends MapColumnFixture {
    public static final String DEFAULT_NR_OF_LINES_TO_BE_READ = "10";

    private static final String HOST_NAME_KEY = "host";
    private static final String PORT_NUMBER_KEY = "port";
    private static final String USER_NAME_KEY = "userName";
    private static final String PASSWORD_KEY = "password";
    private static final String FILE_PATH_KEY = "file";
    private static final String LINES_COUNT_KEY = "nrOfLines";

    private String fileAsString = null;
    private Integer fileSize = null;
    private String errorMessage = null;

    @Override
    public void execute() {
            Map<String, Object> currentRow = getCurrentRowValues();
            String hostName = (String) currentRow.get(HOST_NAME_KEY);
            String userName = (String) currentRow.get(USER_NAME_KEY);
            String password = (String) currentRow.get(PASSWORD_KEY);
            String filePath = (String) currentRow.get(FILE_PATH_KEY);
            Integer port = null;

            // Reset errorMessage between rows.
            errorMessage = "";

            if (currentRow.get(PORT_NUMBER_KEY) != null) {
                port = Integer.valueOf((String) currentRow.get(PORT_NUMBER_KEY));
            }

            Integer nrOfLines = Integer.valueOf((String) get(LINES_COUNT_KEY));
            // read file from FTP server
            fileAsString = "";
            try {
                fileAsString = FtpFileUtil.loadFileFromFTPServer(hostName, port, userName, password, filePath, nrOfLines);
            } catch (Exception ex) {
                handleException(ex, "load file from FTP server");
            }

            // get file size
            fileSize = null;
            try {
                fileSize = FtpFileUtil.getFileSizeOnFTPServer(hostName, port, userName, password, filePath);
            } catch (Exception ex) {
                handleException(ex, "get file size");
            }
    }

    /* (non-Javadoc)
     * @see nl.hsac.fitnesse.fixture.fit.MapColumnFixture#setDefaults(java.util.Map)
     */
    @Override
    protected void setDefaults(Map<String, Object> values) {
        super.setDefaults(values);
        values.put(LINES_COUNT_KEY, DEFAULT_NR_OF_LINES_TO_BE_READ);
    }

    public String result() {
        String result = null;
        if (fileAsString != null) {
            result = fileAsString.replaceAll("[\r\n]", "");
        }
        return result;
    }

    public String error() {
        String errorWithNoNewlines = "";
        if (errorMessage != null) {
            errorWithNoNewlines = errorMessage.replaceAll("[\r\n]", "");
        }
        return errorWithNoNewlines;

    }

    public Integer fileSize() {
        return fileSize;
    }

    private void handleException(Exception ex, String action) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(errorMessage)) {
            sb.append(errorMessage);
            sb.append(" ");
        }
        sb.append(String.format("Failed %s. Cause: %s", action, ex.getMessage()));
        errorMessage = sb.toString();
    }

}
