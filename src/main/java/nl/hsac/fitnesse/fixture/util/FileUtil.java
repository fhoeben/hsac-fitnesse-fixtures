package nl.hsac.fitnesse.fixture.util;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.Scanner;

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
     *
     * @param filename file to read.
     * @return file's content.
     * @throws IllegalArgumentException if file could not be found.
     * @throws IllegalStateException if file could not be read.
     */
    public static String loadFile(String filename) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream is = classLoader.getResourceAsStream(filename);
        if (is == null) {
            throw new IllegalArgumentException("Unable to locate: " + filename);
        }
        return streamToString(is, filename);
    }

    /**
     * Copies UTF-8 input stream's content to a string (closes the stream).
     *
     * @param is input stream (UTF-8) to read.
     * @param name description for stream in error messages.
     * @return content of stream
     * @throws RuntimeException if content could not be read.
     */
    public static String streamToString(InputStream inputStream, String fileName) {
        return streamToString(inputStream, fileName, null);
    }

    /**
     * Copies first <numberOfLines> or all lines from UTF-8 input stream's content to a
     * string and closes the stream.
     *
     * @param is input stream (UTF-8) to read.
     * @param name description for stream in error messages.
     * @param numberOfLines number of lines to be read from input stream - if null, all lines are read
     * @return content of stream
     * @throws RuntimeException if content could not be read.
     */
    public static String streamToString(InputStream is, String name, Integer numberOfLines) {
        String result = null;
        try {
            try {
                if (numberOfLines == null) {
                    // read complete file into string
                    result = new Scanner(is, FILE_ENCODING).useDelimiter("\\A").next();
                } else {
                    // read only given number of lines
                    Scanner scanner = new Scanner(is, FILE_ENCODING);
                    int resultNrOfLines = 1;
                    result = "";
                    while (scanner.hasNext()) {
                        result = result + scanner.nextLine() + System.lineSeparator();
                        if (++resultNrOfLines > numberOfLines) {
                            break;
                        }
                    }
                    scanner.close();
                    if (result.isEmpty()) {
                        result = null;
                    }
                }
            } catch (java.util.NoSuchElementException e) {
                throw new IllegalStateException("Unable to read: " + name + ". Error: " + e.getMessage(), e);
            }
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // what the hell?!
                throw new RuntimeException("Unable to close: " + name, e);
            }
        }
        return result;
    }

    /**
     * Copies source file to target.
     * @param source source file to copy.
     * @param target destination to copy to.
     * @return target as File.
     * @throws IOException when unable to copy.
     */
    public static File copyFile(String source, String target) throws IOException {
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(target).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } finally {
            if (inputChannel != null) {
                inputChannel.close();
            }
            if (outputChannel != null) {
                outputChannel.close();
            }
        }
        return new File(target);
    }

    /**
     * Copy the contents of the given InputStream to the given OutputStream.
     * Closes both streams when done.
     *
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
     *
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

    public static File  writeFromScanner(String filename, Scanner sc) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(filename, FILE_ENCODING);

            while (sc.hasNextLine()) {
                String next = sc.nextLine();
                pw.write(next + System.getProperty("line.separator"));
            }
        } catch (FileNotFoundException e){
            throw new IllegalArgumentException("Unable to write to: " + filename + ".tmp", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }  finally {
            if(pw != null) {
                pw.flush();
                pw.close();
            }
        }
        return new File(filename);
    }

    /**
     * Saves byte[] to new file.
     *
     * @param baseName name for file created (without extension),
     *                 if a file already exists with the supplied name an
     *                 '_index' will be added.
     * @param extension extension for file.
     * @param content data to store in file.
     * @return absolute path of created file.
     */
    public static String saveToFile(String baseName, String extension, byte[] content) {
        String result;
        File output = determineFilename(baseName, extension);
        FileOutputStream target = null;
        try {
            target = new FileOutputStream(output);
            target.write(content);
            result = output.getAbsolutePath();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (target != null) {
                try {
                    target.close();
                } catch (IOException ex) {
                    // what to to?
                }
            }
        }
        return result;
    }

    private static File determineFilename(String baseName, String extension) {
        File output = new File(baseName + "." + extension);
        // ensure directory exists
        File parent = output.getAbsoluteFile().getParentFile();
        if (!parent.exists()) {
            if (!parent.mkdirs()) {
                throw new IllegalArgumentException(
                        "Unable to create directory: "
                                + parent.getAbsolutePath());
            }
        }
        int i = 0;
        // determine first filename not yet in use.
        while (output.exists()) {
            i++;
            String name = String.format("%s_%s.%s", baseName, i, extension);
            output = new File(name);
        }
        return output;
    }
}
