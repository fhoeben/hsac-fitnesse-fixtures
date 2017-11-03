package nl.hsac.fitnesse.fixture.util;

public interface RemoteFileUtil {
    /**
     * Get size of file on server
     * @param hostName the FTP server host name to connect
     * @param port the port to connect
     * @param userName the user name
     * @param password the password
     * @param filePath remote location of remote file
     * @return
     */
    Integer getFileSizeOnServer(String hostName, Integer port, String userName, String password, String filePath);

    /**
     * Upload a given file to FTP server.
     * @param hostName the FTP server host name to connect
     * @param port the port to connect
     * @param userName the user name
     * @param password the password
     * @param localFileFullName the full name (inclusive path) of the local file.
     * @param remotePath the path to the file on the FTP.
     * @return reply string from FTP server after the command has been executed.
     * @throws RuntimeException in case any exception has been thrown.
     */
    String uploadFileToServer(String hostName, Integer port, String userName, String password, String localFileFullName, String remotePath);

    /**
     * Delete given directory from FTP server (directory must be empty).
     * @param hostName the FTP server host name to connect
     * @param port the port to connect
     * @param userName the user name
     * @param password the password
     * @param remotePath the path to the directory on the FTP to be removed
     * @return true if file has been removed  and false otherwise.
     * @throws RuntimeException in case any exception has been thrown.
     */
    boolean deleteDirectoryFromServer(String hostName, Integer port, String userName, String password, String remotePath);

    /**
     * Reads content of file on from FTP server to String.
     * @param hostName the FTP server host name to connect
     * @param port the port to connect
     * @param userName the user name
     * @param password the password
     * @param filePath file to read.
     * @return file's content.
     * @throws RuntimeException in case any exception has been thrown.
     */
    String loadFileFromServer(String hostName, Integer port, String userName, String password, String filePath, int numberOfLines);

}
