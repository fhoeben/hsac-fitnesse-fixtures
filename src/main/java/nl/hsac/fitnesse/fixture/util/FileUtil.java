package nl.hsac.fitnesse.fixture.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * File utilities.
 */
public final class FileUtil {
    private static final int BUFFER_SIZE = 4096;
    private static final String FILE_ENCODING = "UTF-8";

    private FileUtil() {
        // ensure no instance is made.
    }

    /**
     * Reads content of UTF-8 file on classpath to String.
     * @param filename file to read.
     * @return file's content.
     * @throws IllegalArgumentException if file could not be found.
     * @throws IllegalStateException if file could not be read.
     */
    public static String loadFile(String filename) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String result = null;
        InputStream is = classLoader.getResourceAsStream(filename);
        if (is == null) {
            throw new IllegalArgumentException("Unable to locate: " + filename);
        }
        try {
            try {
                result = new java.util.Scanner(is, FILE_ENCODING).useDelimiter("\\A").next();
            } catch (java.util.NoSuchElementException e) {
                throw new IllegalStateException("Unable to read: " + filename + ". Error: " + e.getMessage(), e);
            }
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // what the hell?!
                throw new RuntimeException("Unable to close: " + filename, e);
            }
        }
        return result;
    }

    /**
     * Copy the contents of the given InputStream to the given OutputStream.
     * Closes both streams when done.
     * @param in the stream to copy from
     * @param out the stream to copy to
     * @return the number of bytes copied
     * @throws java.io.IOException in case of I/O errors
     */
    public static int copy(InputStream in, OutputStream out) throws IOException {
        try {
            int byteCount = 0;
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = -1;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                byteCount += bytesRead;
            }
            out.flush();
            return byteCount;
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            try {
                out.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Writes content to file, in UTF-8 encoding.
     * @param filename file to create or overwrite.
     * @param content content to write.
     * @return file reference to file.
     */
    public static File writeFile(String filename, String content) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(filename, FILE_ENCODING);
            pw.write(content);
            pw.flush();
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Unable to write to: " + filename, e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
        return new File(filename);
    }
}
