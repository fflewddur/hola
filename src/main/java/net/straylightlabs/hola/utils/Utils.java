package net.straylightlabs.hola.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
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
     * Save @packet to a new file beginning with @prefix. Append a sequential suffix to ensure we don't
     * overwrite existing files.
     * @param packet The data packet to dump to disk
     * @param prefix The start of the file name
     */
    @SuppressWarnings("unused")
    public static void dumpPacket(DatagramPacket packet, String prefix) {
        byte[] buffer = new byte[packet.getLength()];
        System.arraycopy(packet.getData(), packet.getOffset(), buffer, 0, packet.getLength());
        printBuffer(buffer, "Buffer to save");
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
            path = Paths.get(String.format("%s%s", prefix, Integer.toString(nextDumpPathSuffix)));
            nextDumpPathSuffix++;
        } while (Files.exists(path));

        return path;
    }

    /**
     * Print a formatted version of @buffer in hex
     * @param buffer the byte buffer to display
     */
    public static void printBuffer(byte[] buffer, String msg) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < buffer.length; i++) {
            if (i % 20 == 0) {
                sb.append("\n\t");
            }
            sb.append(String.format("%02x", buffer[i]));
        }

        logger.info("{}: {}", msg, sb.toString());
    }
}
