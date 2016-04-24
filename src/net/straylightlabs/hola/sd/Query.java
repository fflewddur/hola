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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.*;

public class Query {
    private final Service service;
    private final Domain domain;
    private final int browsingTimeout;

    private MulticastSocket socket;
    private InetAddress mdnsGroupIPv4;
    private InetAddress mdnsGroupIPv6;
    private Set<Instance> instances;
    private Map<String, Response> instanceResponseMap;

    private final static Logger logger = LoggerFactory.getLogger(Query.class);

    public static final String MDNS_IP4_ADDRESS = "224.0.0.251";
    public static final String MDNS_IP6_ADDRESS = "FF02::FB";
    public static final int MDNS_PORT = 5353;

    /**
     * The browsing socket will timeout after this many milliseconds
     */
    private static final int BROWSING_TIMEOUT = 750;

    /**
     * Create a Query for the given Service and Domain.
     *
     * @param service service to search for
     * @param domain  domain to search on
     * @return a new Query object
     */
    @SuppressWarnings("unused")
    public static Query createFor(Service service, Domain domain) {
        return new Query(service, domain, BROWSING_TIMEOUT);
    }

    /**
     * Create a Query for the given Service and Domain.
     *
     * @param service service to search for
     * @param domain  domain to search on
     * @param timeout time in MS to wait for a response
     * @return a new Query object
     */
    @SuppressWarnings("unused")
    public static Query createWithTimeout(Service service, Domain domain, int timeout) {
        return new Query(service, domain, timeout);
    }

    private Query(Service service, Domain domain, int browsingTimeout) {
        this.service = service;
        this.domain = domain;
        this.browsingTimeout = browsingTimeout;
        this.instanceResponseMap = Collections.synchronizedMap(new HashMap<>());
    }

    /**
     * Synchronously runs the Query a single time.
     *
     * @return a list of Instances that match this Query
     * @throws IOException
     */
    public Set<Instance> runOnce() throws IOException {
        Question question = new Question(service, domain);
        instances = Collections.synchronizedSet(new HashSet<>());
        try {
            openSocket();
            Thread listener = listenForResponses();
            question.askOn(socket, mdnsGroupIPv4);
            question.askOn(socket, mdnsGroupIPv6);
            try {
                listener.join();
            } catch (InterruptedException e) {
                logger.error("InterruptedException while listening for mDNS responses: ", e);
            }
        } finally {
            closeSocket();
        }
        return instances;
    }

    /**
     * Asynchronously runs the Query in a new thread.
     */
    @SuppressWarnings("unused")
    public void start() {
        throw new RuntimeException("Not implemented yet");
    }

    private void openSocket() throws IOException {
        mdnsGroupIPv4 = InetAddress.getByName(MDNS_IP4_ADDRESS);
        mdnsGroupIPv6 = InetAddress.getByName(MDNS_IP6_ADDRESS);
        socket = new MulticastSocket(MDNS_PORT);
        socket.joinGroup(mdnsGroupIPv4);
        socket.joinGroup(mdnsGroupIPv6);
        socket.setTimeToLive(10);
        socket.setSoTimeout(browsingTimeout);
    }

    private Thread listenForResponses() {
        Thread listener = new Thread(() -> {
            collectResponsesOn(socket);
        });

        listener.start();
        return listener;
    }

    private Set<Instance> collectResponsesOn(MulticastSocket socket) {
        for (int timeouts = 0; timeouts == 0; ) {
            byte[] responseBuffer = new byte[Message.MAX_LENGTH];
            DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
            try {
                logger.debug("Listening for responses...");
                socket.receive(responsePacket);
                logger.debug("Response received!");
//                logger.debug("Response of length {} at offset {}: {}", responsePacket.getLength(), responsePacket.getOffset(), responsePacket.getData());
                try {
                    Response response = Response.createFrom(responsePacket);
                    if (response.isComplete()) {
                        instances.add(Instance.createFrom(response));
                    } else {
                        boolean foundRecords = false;
                        for (String name : instanceResponseMap.keySet()) {
                            if (response.containsRecordsFor(name)) {
                                instanceResponseMap.put(name, response.mergeWith(instanceResponseMap.get(name)));
                                foundRecords = true;
                            }
                        }
                        if (!foundRecords) {
                            fetchMissingRecords(response);
                        } else if (response.isComplete()) {
                            instances.add(Instance.createFrom(response));
                        }
                    }
                } catch (IllegalArgumentException e) {
                    logger.debug("Response was not a mDNS response packet, ignoring it");
                    timeouts = 0;
                    continue;
                }
                timeouts = 0;
            } catch (SocketTimeoutException e) {
                timeouts++;
            } catch (IOException e) {
                logger.error("IOException while listening for mDNS responses: ", e);
            }
        }

        return instances;
    }

    private void fetchMissingRecords(Response response) throws IOException {
        instanceResponseMap.put(response.getPtr(), response);
        if (!response.hasSrvRecords()) {
            logger.debug("Response has no SRV records: {}", response);
            queryForSrvRecord(response);
        }
        if (!response.hasTxtRecords()) {
            logger.debug("Response has no TXT records: {}", response);
            queryForTxtRecord(response);
        }
        if (!response.hasInetAddresses()) {
            logger.debug("Response has no A or AAAA records: {}", response);
            queryForAddresses(response);
        }
    }

    private void queryForSrvRecord(Response response) throws IOException {
        Question question = new Question(response.getPtr(), Question.QType.SRV, Question.QClass.IN);
        ask(question);
    }

    private void queryForTxtRecord(Response response) throws IOException {
        Question question = new Question(response.getPtr(), Question.QType.TXT, Question.QClass.IN);
        ask(question);
    }

    private void queryForAddresses(Response response) throws IOException {
        Question question = new Question(response.getPtr(), Question.QType.A, Question.QClass.IN);
        ask(question);
    }

    private void ask(Question question) throws IOException {
        question.askOn(socket, mdnsGroupIPv4);
        question.askOn(socket, mdnsGroupIPv6);
    }

    private void closeSocket() {
        if (socket != null) {
            socket.close();
            socket = null;
        }
    }
}
