package nl.hsac.fitnesse.fixture.util;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Helper to (un)zip files.
 */
public class ZipHelper {

    public File createZip(String targetName, String... sourceFiles) throws IOException {
        File target = new File(targetName);
        ensureFileDoesNotYetExist(target);
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(target))) {
            for (String srcFile : sourceFiles) {
                File fileToZip = new File(srcFile);
                addEntries(fileToZip, fileToZip.getName(), zipOut);
            }
        }
        return target;
    }

    protected List<ZipEntry> addEntries(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return Collections.emptyList();
        }
        List<ZipEntry> result;
        if (fileToZip.isDirectory()) {
            result = addDirectory(zipOut, fileToZip, fileName);
        } else {
            ZipEntry entry = addFile(zipOut, fileToZip, fileName);
            result = Collections.singletonList(entry);
        }
        return result;
    }

    private List<ZipEntry> addDirectory(ZipOutputStream zipOut, File fileToZip, String directoryName) throws IOException {
        List<ZipEntry> result = new ArrayList<>();
        if (!directoryName.endsWith("/")) {
            directoryName = directoryName + "/";
        }
        ZipEntry dirEntry = new ZipEntry(directoryName);
        zipOut.putNextEntry(dirEntry);
        zipOut.closeEntry();
        result.add(dirEntry);

        File[] children = fileToZip.listFiles();
        for (File childFile : children) {
            List<ZipEntry> childEntries = addEntries(childFile, directoryName + childFile.getName(), zipOut);
            result.addAll(childEntries);
        }
        return result;
    }

    private ZipEntry addFile(ZipOutputStream zipOut, File fileToZip, String fileName) throws IOException {
        try (FileInputStream fis = new FileInputStream(fileToZip)) {
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOut.putNextEntry(zipEntry);
            IOUtils.copy(fis, zipOut);
            return zipEntry;
        }
    }

    public List<File> unzip(String zipFile, String destination) throws IOException {
        File destDir = new File(destination);
        if (!destDir.exists()) {
            if (!destDir.mkdirs()){
                throw new IOException("Unable to create: " + destDir.getAbsolutePath());
            }
        }
        List<File> allEntries = visitZipEntries(zipFile, (zf, zipEntry) -> unzipEntry(zf, zipEntry, destDir));
        return allEntries.stream()
                .filter(f -> !f.isDirectory())
                .collect(Collectors.toList());
    }

    public List<ZipEntry> getEntries(String zipFile) throws IOException {
        return visitZipEntries(zipFile, (zf, entry) -> entry);
    }

    public <T> List<T> visitZipEntries(String zipFile, BiFunction<ZipFile, ZipEntry, T> consumer) throws IOException {
        try (ZipFile zFile = new ZipFile(zipFile)) {
            return zFile.stream()
                    .parallel()
                    .map(e -> consumer.apply(zFile, e))
                    .collect(Collectors.toList());
       } catch (RuntimeIOException e) {
            throw e.getCause();
        }
    }

    protected File unzipEntry(ZipFile zipFile, ZipEntry entry, File targetDir) {
        try {
            File targetFile = preventZipSlip(targetDir, entry.getName());
            Path targetPath = targetFile.getAbsoluteFile().toPath();
            if (entry.isDirectory()) {
                Files.createDirectories(targetPath);
            } else {
                Files.createDirectories(targetPath.getParent());
                try (InputStream in = zipFile.getInputStream(entry)) {
                    Files.copy(in, targetPath);
                }
            }
            return targetPath.toFile();
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    private File preventZipSlip(File destinationDir, String name) throws IOException {
        // prevent Zip Slip: entry creation outside of target directory
        File destFile = new File(destinationDir, name);
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + name);
        }
        return destFile;
    }

    private void ensureFileDoesNotYetExist(File target) throws IOException {
        if (target.exists()) {
            throw new IOException("File already exists: " + target.getAbsolutePath());
        }
    }

    public static class RuntimeIOException extends RuntimeException {
        RuntimeIOException(IOException e) {
            super(e);
        }

        @Override
        public IOException getCause() {
            return (IOException) super.getCause();
        }
    }
}
