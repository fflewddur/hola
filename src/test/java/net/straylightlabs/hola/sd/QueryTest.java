/*
 * The MIT License
 *
 * Copyright (c) 2015-2018 Todd Kulesza <todd@dropline.net>
 *
 * This file is part of Hola.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.straylightlabs.hola.sd;

import net.straylightlabs.hola.dns.Domain;
import net.straylightlabs.hola.dns.Response;
import net.straylightlabs.hola.utils.Utils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.assertTrue;

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

    @Test
    public void testTivoQueryWithValidResponse() throws IOException {
        Service service = Service.fromName("_tivo-mindrpc._tcp");
        Query query = Query.createFor(service, Domain.LOCAL);
        query.runOnceOn(Optional.ofNullable(Query.TEST_SUITE_ADDRESS));
        Response response = loadResponse("response-mdns-tivo");
        assertTrue(response.answers(query.getQuestions()));
    }

    @Test
    public void testQueryWithMultipleValidResponse() throws IOException {
        Service service = Service.fromName("_airport._tcp");
        Query query = Query.createFor(service, Domain.LOCAL);
        query.runOnceOn(Optional.ofNullable(Query.TEST_SUITE_ADDRESS));
        DatagramPacket packet = loadPacket("response-mdns-appletv-1");
        query.parseResponsePacket(packet);
        packet = loadPacket("response-mdns-appletv-2");
        query.parseResponsePacket(packet);
        query.buildInstancesFromRecords();

        // Create the expected instance
        List<InetAddress> addresses = new ArrayList<>();
        addresses.add(InetAddress.getByName("10.0.0.1"));
        addresses.add(InetAddress.getByName("fe80:0:0:0:9272:40ff:fe05:ef68"));
        Map<String, String> attributes = new HashMap<>();
        attributes.put("waMA", "90-72-40-05-EF-68,raMA");
        Instance expected = new Instance("annuvin", addresses, 5009, attributes);

        logger.info("Expected: {}", expected);
        logger.info("Found instances: {}", query.getInstances().size());
        for (Instance instance : query.getInstances()) {
            logger.info("Found: {}", instance);
        }
        Set<Instance> found = query.getInstances();
        assertTrue(found.size() == 1);
        assertTrue(found.contains(expected));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidResponse() {
        loadResponse("response-not-mdns");
    }

    private DatagramPacket loadPacket(String resourceName) {
        try {
            URL resourceURL = getClass().getClassLoader().getResource(resourceName);
            if (resourceURL != null) {
                Path resource = Paths.get(resourceURL.toURI());
                byte[] responseBuffer = Files.readAllBytes(resource);
                Utils.printBuffer(responseBuffer, "Buffer from disk");
                return new DatagramPacket(responseBuffer, responseBuffer.length);
            }
        } catch (IOException e) {
            logger.error("Error reading file: {}", e.getLocalizedMessage());
        } catch (URISyntaxException e) {
            logger.error("Error creating URI: {}", e.getLocalizedMessage());
        }

        return null;
    }

    private Response loadResponse(String resourceName) {
        return Response.createFrom(loadPacket(resourceName));
    }
}
