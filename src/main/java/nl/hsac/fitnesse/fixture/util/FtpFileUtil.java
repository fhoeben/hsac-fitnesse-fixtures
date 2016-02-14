/**
 *
 */
package nl.hsac.fitnesse.fixture.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Ftp file utilities.
 */
public class FtpFileUtil {
    /**
     * Get size of the FTP file.
     * @param hostName the FTP server host name to connect
     * @param port the port to connect
     * @param userName the user name
     * @param password the password
     * @param filePath file to get size for.
     * @return file's size if found.
     * @throws RuntimeException in case any exception has been thrown.
     */
    public static Integer getFileSizeOnFTPServer(String hostName, Integer port,
            String userName, String password, String filePath) {
        Integer result = null;

        // get file size
        String replyString = executeCommandOnFTPServer(hostName, port, userName, password, "SIZE", filePath);

        if (replyString == null || !replyString.contains(" ")) {
            throw new  RuntimeException(String.format(
                    "Unable to get size of the %s file. Got [%s] reply from FTP server.", filePath, replyString));
        } else {
            result = Integer.valueOf(replyString.split(" ")[1].replaceAll("[\r\n]", ""));
        }
        return result;
    }

    /**
     * Execute command with supplied arguments on the FTP server.
     * @param hostName the FTP server host name to connect
     * @param port the port to connect
     * @param userName the user name
     * @param password the password
     * @param command the command to execute.
     * @param commandArgs the command argument(s), if any.
     * @return reply string from FTP server after the command has been executed.
     * @throws RuntimeException in case any exception has been thrown.
     */
    public static String executeCommandOnFTPServer(String hostName, Integer port,
            String userName, String password, String command, String commandArgs) {
        String result = null;

        if (StringUtils.isNotBlank(command)) {
            FTPClient ftpClient = new FTPClient();
            String errorMessage = "Unable to connect and execute %s command with argumments '%s' for FTP server '%s'.";

            try {
                connectAndLoginOnFTPServer(ftpClient, hostName, port, userName, password);
                // execute command
                if (StringUtils.isBlank(commandArgs)) {
                    ftpClient.sendCommand(command);
                } else {
                    ftpClient.sendCommand(command, commandArgs);
                }

                validatResponse(ftpClient);
                result = ftpClient.getReplyString();

            } catch (IOException ex) {
                throw new RuntimeException(String.format(errorMessage, command, commandArgs, hostName), ex);
            } finally {
                disconnectAndLogoutFromFTPServer(ftpClient, hostName);
            }
        }
        return result;
    }

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
    public static String uploadFileToFTPServer(String hostName, Integer port, String userName,
                                              String password, String localFileFullName, String remotePath) {
        String result = null;

        FTPClient ftpClient = new FTPClient();
        String errorMessage = "Unable to upload file '%s' for FTP server '%s'.";
        InputStream inputStream = null;

        try {
            connectAndLoginOnFTPServer(ftpClient, hostName, port, userName, password);
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();

            //String fullLocalPath = localPath + fileName;
            File localFile = new File(localFileFullName);

            //String fullRemotePath = remotePath + fileName;
            String remoteFile = remotePath + FilenameUtils.getName(localFileFullName);
            inputStream = new FileInputStream(localFile);

            boolean uploadFinished = ftpClient.storeFile(remoteFile, inputStream);

            if (uploadFinished) {
                result = String.format("File '%s' successfully uploaded", localFileFullName);
            } else {
                result = String.format("Failed upload '%s' file to FTP server. Got reply: %s",
                                                        localFileFullName, ftpClient.getReplyString());
            }
            } catch (IOException ex) {
                throw new RuntimeException(String.format(errorMessage, remotePath, hostName), ex);
            } finally {
                closeInputStream(inputStream);
                disconnectAndLogoutFromFTPServer(ftpClient, hostName);
            }
        return result;
    }

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
    public static boolean deleteDirectoryFromFTPServer(String hostName, Integer port, String userName, String password, String remotePath) {
        boolean deleted = false;

        FTPClient ftpClient = new FTPClient();
        String errorMessage = "Could not delete the directory '%s' from FTP server '%s'. Cause: %s";

        try {
            connectAndLoginOnFTPServer(ftpClient, hostName, port, userName, password);
            deleted = ftpClient.removeDirectory(remotePath);
        } catch (IOException ex) {
            throw new RuntimeException(String.format(errorMessage, remotePath, hostName), ex);
        } finally {
            disconnectAndLogoutFromFTPServer(ftpClient, hostName);
        }
        return deleted;
    }

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
    public static String loadFileFromFTPServer(String hostName, Integer port,
            String userName, String password, String filePath, int numberOfLines) {

        String result = null;
        FTPClient ftpClient = new FTPClient();
        InputStream inputStream = null;
        String errorMessage = "Unable to connect and download file '%s' from FTP server '%s'.";

        try {
            connectAndLoginOnFTPServer(ftpClient, hostName, port, userName, password);

            // load file into string
            ftpClient.enterLocalPassiveMode();
            inputStream = ftpClient.retrieveFileStream(filePath);
            validatResponse(ftpClient);
            result = FileUtil.streamToString(inputStream, filePath, numberOfLines);
            ftpClient.completePendingCommand();

        } catch (IOException ex) {
            throw new RuntimeException(String.format(errorMessage, filePath, hostName), ex);

        } finally {
            closeInputStream(inputStream);
            disconnectAndLogoutFromFTPServer(ftpClient, hostName);
        }

        return result;
    }

    /**
     * Connect and login on given FTP server with provided credentials.
     * @param hostName the FTP server host name to connect
     * @param port the port to connect
     * @param userName the user name
     * @param password the password
     */
    public static void connectAndLoginOnFTPServer(FTPClient ftpClient,
            String hostName, Integer port, String userName, String password) {
        try {
            if (port != null && port.intValue() > 0) {
                ftpClient.connect(hostName, port);
            } else {
                ftpClient.connect(hostName);
            }
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                throw new IOException(String.format("FTP server '%s' refused connection.", hostName));
            }

            // try to login
            if (!ftpClient.login(userName, password)) {
                throw new IOException(String.format("Unable to login to FTP server '%s'.", hostName));
            }
        } catch (IOException ex) {
            throw new RuntimeException(
                    String.format("Unable to connect and login to FTP server '%s'. Cause: ", hostName), ex);
        }
    }

    /**
     * Disconnect and logout given FTP client.
     * @param hostName the FTP server host name
     */
    public static void disconnectAndLogoutFromFTPServer(FTPClient ftpClient, String hostName) {
        try {
            // logout and disconnect
            if (ftpClient != null && ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        } catch (IOException e) {
            // what the hell?!
            throw new RuntimeException("Unable to logout and disconnect from : " + hostName, e);
        }
    }

    private static void validatResponse(FTPClient ftpClient) {
        if (FTPReply.isNegativeTransient(ftpClient.getReplyCode()) || FTPReply.isNegativePermanent(ftpClient.getReplyCode())) {
            throw new RuntimeException("Got error response: " + ftpClient.getReplyCode());
        }
    }

    private static void closeInputStream(InputStream inputStream) {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            // what the hell?!
            throw new RuntimeException("Unable to close file input stream.", e);
        }
    }
}
