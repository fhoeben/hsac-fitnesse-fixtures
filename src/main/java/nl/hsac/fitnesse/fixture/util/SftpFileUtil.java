package nl.hsac.fitnesse.fixture.util;

import java.io.File;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;

import nl.hsac.fitnesse.fixture.util.FileUtil;

/**
 * Sftp file utilities.
 */
public class SftpFileUtil implements RemoteFileUtil {

    /**
     * Get files content as strings from SFTP server for files which names matches supplied pattern.
     * @param hostName the SFTP server host name
     * @param userName user name for SFTP server
     * @param password password
     * @param filesPath abs path to the files (directory on FTP server)
     * @param fileNamePattern the filename pattern
     * @param nrOfLines number of lines to be read from files (if null, all lines will be read)
     * @return collection with (file name, file content) pairs for each found file
     */
    public static Map<String, String> getSftpFilesAsString(String hostName, String userName, String password,
            String filesPath, String fileNamePattern, Integer nrOfLines) {

        StandardFileSystemManager manager = new StandardFileSystemManager();
        Map<String, String> result = new TreeMap<String, String>();
        try {
            FileObject remoteDir = getRemoteFileObject(manager, hostName, userName, password, filesPath, false);
            FileObject[] files = remoteDir.findFiles(getFileSelector(fileNamePattern));
            if (files != null && files.length > 0) {
                for (FileObject fileObject : files) {
                    String absFilePath = fileObject.getName().getPath();
                    try {
                        String content = FileUtil.streamToString(fileObject.getContent().getInputStream(), absFilePath, nrOfLines);
                        result.put(fileObject.getName().getBaseName(), content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                throw new RuntimeException(
                        String.format("No files found with name matching '%s' pattern on SFTP server '%s' in folder '%s'.",
                                fileNamePattern, hostName, filesPath));
            }
            remoteDir.close();
        } catch (Exception ex) {
            throw new RuntimeException(String.format(
                    "Unable to connect and download file '%s' from SFTP server '%s'. Cause: %s",
                    filesPath, hostName, ex.getMessage()));
        } finally {
            manager.close();
        }
        return result;
    }

    private static FileSelector getFileSelector(final String fileNamePattern) {
        FileSelector fs = new FileSelector() {

            @Override
            public boolean includeFile(FileSelectInfo fileInfo) throws Exception {
                boolean result = false;
                Pattern pattern = Pattern.compile(fileNamePattern);
                // only files must be checked
                if (fileInfo.getFile().getType() == FileType.FILE) {
                    result = pattern.matcher(fileInfo.getFile().getName().getBaseName()).matches();
                }
                return result;
            }

            @Override
            public boolean traverseDescendents(FileSelectInfo fileInfo) throws Exception {
                return fileInfo.getDepth() <= 1;
            }

        };
        return fs;
    }

    /* (non-Javadoc)
     * @see nl.hsac.fitnesse.fixture.util.RemoteFileUtil#getFileSizeOnServer(java.lang.String, java.lang.Integer, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Integer getFileSizeOnServer(String hostName, Integer port, String userName, String password, String filePath) {

        Integer result = 0;
        StandardFileSystemManager manager = new StandardFileSystemManager();

        try {
            FileObject remoteFile = getRemoteFileObject(manager, hostName, userName, password, filePath, null);
            result = Long.valueOf(remoteFile.getContent().getSize()).intValue();
        } catch (Exception ex) {
            throw new RuntimeException(String.format(
                    "Unable to get size of the %s file. Cause: %s", filePath, ex.getMessage()));
        } finally {
            manager.close();
        }
        return result;
    }

    /* (non-Javadoc)
     * @see nl.hsac.fitnesse.fixture.util.RemoteFileUtil#loadFileFromServer(java.lang.String, java.lang.Integer, java.lang.String, java.lang.String, java.lang.String, int)
     */
    @Override
    public String loadFileFromServer(String hostName, Integer port,
            String userName, String password, String filePath, int numberOfLines) {

        String result = null;
        StandardFileSystemManager manager = new StandardFileSystemManager();

        try {
            FileObject remoteFile = getRemoteFileObject(manager, hostName, userName, password, filePath, null);
            result = FileUtil.streamToString(remoteFile.getContent().getInputStream(), filePath, numberOfLines);
        } catch (Exception ex) {
            throw new RuntimeException(String.format(
                    "Unable to connect and download file '%s' from FTP server '%s'. Cause: %s",
                    filePath, hostName, ex.getMessage()));
        } finally {
            manager.close();
        }

        return result;
    }

    /* (non-Javadoc)
     * @see nl.hsac.fitnesse.fixture.util.RemoteFileUtil#uploadFileToServer(java.lang.String, java.lang.Integer, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public String uploadFileToServer(String hostName, Integer port, String userName, String password, String localFileFullName,
            String remotePath) {

        String errorMsg = String.format("Unable to copy file ' %s' to SFTP server %s. Cause: ", localFileFullName, hostName);
        StandardFileSystemManager manager = new StandardFileSystemManager();
        String result = "";
        try {
            // check if the file exists
            File localFile = new File(localFileFullName);
            if (!localFile.exists()) {
                throw new RuntimeException(errorMsg + "local file not found.");
            }

            // String fullRemotePath = remotePath + fileName;
            String remoteFile = remotePath + FilenameUtils.getName(localFileFullName);

            // Create remote file object
            FileObject targetFile = getRemoteFileObject(manager, hostName, userName, password, remoteFile, null);

            // Create local file object
            FileObject sourceFile = manager.resolveFile(localFile.getAbsolutePath());

            // Copy local file to sftp server
            targetFile.copyFrom(sourceFile, Selectors.SELECT_SELF);
            result = "Uploaded done";

        } catch (Exception ex) {
            throw new RuntimeException(errorMsg + ex.getMessage());
        } finally {
            manager.close();
        }
        return result;
    }

    /* (non-Javadoc)
     * @see nl.hsac.fitnesse.fixture.util.RemoteFileUtil#deleteDirectoryFromServer(java.lang.String, java.lang.Integer, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public boolean deleteDirectoryFromServer(
            String hostName, Integer port, String userName, String password, String filePath) {

        boolean isDeleted = false;
        StandardFileSystemManager manager = new StandardFileSystemManager();
        String errorMsg = String.format("Unable to delete file '%s' from SFTP server %s. Cause: ", filePath, hostName);

        try {
            FileObject remoteFile = getRemoteFileObject(manager, hostName, userName, password, filePath, null);
            if (remoteFile.exists()) {
                if (remoteFile.getType() == FileType.FILE) {
                    isDeleted = remoteFile.delete();
                } else {
                    // not a file; must be directory
                    isDeleted = remoteFile.delete(Selectors.SELECT_SELF_AND_CHILDREN) > 0;
                }
            } else {
                throw new RuntimeException("file does not exists.");
            }
        } catch (Exception ex) {
            throw new RuntimeException(errorMsg + ex.getMessage());
        } finally {
            manager.close();
        }
        return isDeleted;
    }

    private static FileObject getRemoteFileObject(StandardFileSystemManager manager, String hostName,
            String userName, String password, String filePath, Boolean isUserDirIsRoot) throws UnknownHostException, FileSystemException {

        boolean userDirIsRoot = isUserDirIsRoot == null ? true : isUserDirIsRoot;

        // get sftp uri
        String sftpUri = String.format("sftp://%s:%s@%s/%s", userName, password, hostName, filePath);

        // Initializes the file manager
        manager.init();

        // Setup our SFTP configuration
        FileSystemOptions opts = new FileSystemOptions();
        SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
        SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, userDirIsRoot);
        SftpFileSystemConfigBuilder.getInstance().setTimeout(opts, 10000);

        // Create remote file object
        FileObject remoteFile = manager.resolveFile(sftpUri, opts);

        return remoteFile;
    }

    /**
     * Create dir on  SFTP server.
     * @param hostName the SFTP server host name to connect
     * @param userName the user name
     * @param password the password
     * @param dirPath file to delete.
     * @return true if file has been successfully deleted and false otherwise
     * @throws RuntimeException in case any exception has been thrown.
     */
    public static boolean createDirSFTPServer(
            String hostName, String userName, String password, String dirPath) {

        boolean isCreated = false;
        StandardFileSystemManager manager = new StandardFileSystemManager();
        String errorMsg = String.format("Unable to create dir '%s' on SFTP server %s. Cause: ", dirPath, hostName);

        try {
            FileObject remoteFile = getRemoteFileObject(manager, hostName, userName, password, dirPath, null);
            if (remoteFile.exists()) {
                if (remoteFile.getType() != FileType.FOLDER) {
                    throw new RuntimeException("File with same name already exists. It's not a folder.");
                }
            } else {
                remoteFile.createFolder();
            }
        } catch (Exception ex) {
            throw new RuntimeException(errorMsg + ex.getMessage());
        } finally {
            manager.close();
        }
        return isCreated;
    }

}
