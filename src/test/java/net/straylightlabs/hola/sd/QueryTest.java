package net.straylightlabs.hola.sd;

import net.straylightlabs.hola.dns.Response;
import net.straylightlabs.hola.utils.Utils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class QueryTest {

    private final static Logger logger = LoggerFactory.getLogger(QueryTest.class);

    @Test
    public void testResponse() {
/*        loadAndParseResponse("response1");
        loadAndParseResponse("response2");
        loadAndParseResponse("response3");
        loadAndParseResponse("response4");
        loadAndParseResponse("response5");
        loadAndParseResponse("response6");
        loadAndParseResponse("response7");
        loadAndParseResponse("response8");
        loadAndParseResponse("response9");
        */
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidResponse() {
        loadAndParseResponse("response-not-mdns");
    }

    private void loadAndParseResponse(String resourceName) {
        try {
            Path resource = Paths.get(getClass().getClassLoader().getResource(resourceName).toURI());
            byte[] responseBuffer = Files.readAllBytes(resource);
            Utils.printBuffer(responseBuffer, "Buffer from disk");
            DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
            Response response = Response.createFrom(responsePacket);
            logger.info("Response: {}", response);
        } catch (IOException e) {
            logger.error("Error reading file: {}", e.getLocalizedMessage());
        } catch (URISyntaxException e) {
            logger.error("Error creating URI: {}", e.getLocalizedMessage());
        }
    }
}
