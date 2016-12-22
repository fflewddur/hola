package net.straylightlabs.hola.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Utils {
    private static int nextDumpPathSuffix;
    private final static Logger logger = LoggerFactory.getLogger(Utils.class);

    static {
        nextDumpPathSuffix = 1;
    }

    /**
     * Save @buffer to a new file beginning with @prefix. Append a sequential suffix to ensure we don't
     * overwrite existing files.
     * @param buffer The byte buffer to dump to disk
     * @param prefix The start of the file name
     */
    @SuppressWarnings("unused")
    public static void dumpBuffer(byte[] buffer, String prefix) {
        try {
            Path path = getNextPath(prefix);
            logger.info("Dumping buffer to {}", path);
            Files.write(path, buffer);
        } catch (IOException e) {
            logger.error("Error writing file: {}", e.getLocalizedMessage());
        }
    }

    /**
     * Get the next sequential Path for a binary dump file, ensuring we don't overwrite any existing files.
     * @param prefix The start of the file name
     * @return Next sequential Path
     */
    public static Path getNextPath(String prefix) {
        Path path;

        do {
            path = Paths.get(prefix, Integer.toString(nextDumpPathSuffix));
            nextDumpPathSuffix++;
        } while (Files.exists(path));

        return path;
    }
}
