/*
 * Copyright 2015 Todd Kulesza <todd@dropline.net>.
 *
 * This file is part of Hola.
 *
 * Hola is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Hola is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Hola.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.straylightlabs.hola.sd;

import net.straylightlabs.hola.dns.Domain;
import net.straylightlabs.hola.dns.Message;
import net.straylightlabs.hola.dns.Question;
import net.straylightlabs.hola.dns.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Query {
    private final Service service;
    private final Domain domain;
    private MulticastSocket socket;
    private List<Instance> instances;
    private Map<Question,Response> questions;

    private final static Logger logger = LoggerFactory.getLogger(Query.class);

    public static final String MDNS_IP4_ADDRESS = "224.0.0.251";
    public static final String MDNS_IP6_ADDRESS = "FF02::FB";
    public static final int MDNS_PORT = 5353;

    /**
     * The browsing socket will timeout after this many milliseconds
     */
    private static final int BROWSING_TIMEOUT = 500;

    /**
     * Create a Query for the given Service and Domain.
     * @param service service to search for
     * @param domain domain to search on
     * @return a new Query object
     */
    public static Query createFor(Service service, Domain domain) {
        return new Query(service, domain);
    }

    private Query(Service service, Domain domain) {
        this.service = service;
        this.domain = domain;
        questions = new HashMap<>();
    }

    /**
     * Synchronously runs the Query a single time.
     * @return a list of Instances that match this Query
     * @throws IOException
     */
    public List<Instance> runOnce() throws IOException {
        Question question = new Question(service, domain);
        instances = new ArrayList<>();
        try {
            openSocket();
            questions.put(question, null);
            question.askOn(socket);
            collectResponses();
        } finally {
            closeSocket();
        }
        return instances;
    }

    /**
     * Asynchronously runs the Query in a new thread.
     */
    public void start() {
        throw new NotImplementedException();
    }

    private void openSocket() throws IOException {
        socket = new MulticastSocket();
        socket.setReuseAddress(true);
        socket.setSoTimeout(BROWSING_TIMEOUT);
    }

    private List<Instance> collectResponses() throws IOException {

        for (int timeouts = 0; timeouts == 0; ) {
            byte[] responseBuffer = new byte[Message.MAX_LENGTH];
            DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
            try {
                logger.debug("Listening for response...");
                socket.receive(responsePacket);
                Response response = Response.createFrom(responsePacket);
//                if (response.answersOneOf(questions)) {
                    parseResponse(response);
//                } else {
//                    logger.debug("Received a response, but it doesn't answer any of our questions: {}", response);
//                }

                timeouts = 0;
            } catch (SocketTimeoutException e) {
                timeouts++;
            }
        }
        return instances;
    }

    private void parseResponse(Response response) throws IOException {
        if (!response.hasSrvRecords()) {
            logger.debug("Response has no SRV records: {}", response);
            queryForSrvRecord(response);
        } else if (!response.hasTxtRecords()) {
            logger.debug("Response has no TXT records: {}", response);
        } else if (!response.hasInetAddresses()) {
            logger.debug("Response has no A or AAAA records: {}", response);
            queryForAddresses(response);
        } else {
            logger.debug("Response = {}", response);
            instances.add(Instance.createFrom(response));
        }
    }

    private void queryForSrvRecord(Response response) throws IOException {
        Question aQuestion = new Question(response.getPtr(), Question.QType.SRV, Question.QClass.IN);
        logger.debug("asking '{}'", aQuestion);
        aQuestion.askOn(socket);
    }

    private void queryForAddresses(Response response) throws IOException {
        Question aQuestion = new Question(response.getPtr(), Question.QType.A, Question.QClass.IN);
        logger.debug("asking '{}'", aQuestion);
        aQuestion.askOn(socket);
    }

    private void closeSocket() {
        if (socket != null) {
            socket.close();
            socket = null;
        }
    }
}
