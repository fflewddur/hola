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

import net.straylightlabs.hola.dns.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class Query {
    private final Service service;
    private final Domain domain;
    private final int browsingTimeout;
    private final Lock socketLock;

    private MulticastSocket socket;
    private InetAddress mdnsGroupIPv4;
    private InetAddress mdnsGroupIPv6;
    private boolean isUsingIPv4;
    private boolean isUsingIPv6;
    private Question initialQuestion;
    private Set<Question> questions;
    private Set<Instance> instances;
    private Set<Record> records;
    private boolean listenerStarted;
    private boolean listenerFinished;
    private InetAddress host = null;

    private final static Logger logger = LoggerFactory.getLogger(Query.class);

    public static final String MDNS_IP4_ADDRESS = "224.0.0.251";
    public static final String MDNS_IP6_ADDRESS = "FF02::FB";
    public static final int MDNS_PORT = 5353;
    private static final int WAIT_FOR_LISTENER_MS = 10; // Number of milliseconds to wait for the listener to start

    /**
     * The browsing socket will timeout after this many milliseconds
     */
    private static final int BROWSING_TIMEOUT = 750;

    /**
     * Create a Query for the given Service and Domain.
     *
     * @param service service to search for
     * @param domain  domain to search on
     * @param host    Address where the query will be apply
     * @return a new Query object
     */
    @SuppressWarnings("unused")
    public static Query createFor(Service service, Domain domain, InetAddress host) {
        return new Query(service, domain, BROWSING_TIMEOUT, host);
    }

    /**
     * Create a Query for the given Service and Domain.
     *
     * @param service service to search for
     * @param domain  domain to search on
     * @return a new Query object
     */
    @SuppressWarnings("unused")
    public static Query createFor(Service service, Domain domain) throws UnknownHostException {
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
    public static Query createWithTimeout(Service service, Domain domain, int timeout) throws UnknownHostException {
        return new Query(service, domain, timeout);
    }

    /**
     * Create a Query for the given Service and Domain.
     *
     * @param service service to search for
     * @param domain  domain to search on
     * @param timeout time in MS to wait for a response
     * @return a new Query object
     */
    public static Query createWithTimeout(Service service, Domain domain, int timeout, InetAddress host) {
        return new Query(service, domain, timeout, host);
    }

    /**
     * @param service         service to search for
     * @param domain          domain to search on
     * @param browsingTimeout time in MS to wait for a response
     * @throws UnknownHostException Thrown to indicate that the IP address of a host could not be determined.
     */
    private Query(Service service, Domain domain, int browsingTimeout) throws UnknownHostException {
        this.service = service;
        this.domain = domain;
        this.browsingTimeout = browsingTimeout;
        this.questions = new HashSet<>();
        this.records = new HashSet<>();
        this.socketLock = new ReentrantLock();
        this.host = InetAddress.getLocalHost();
    }

    /**
     * @param service         service to search for
     * @param domain          domain to search on
     * @param browsingTimeout time in MS to wait for a response
     * @param host            Address where the query will be apply
     */
    private Query(Service service, Domain domain, int browsingTimeout, InetAddress host) {
        this.service = service;
        this.domain = domain;
        this.browsingTimeout = browsingTimeout;
        this.questions = new HashSet<>();
        this.records = new HashSet<>();
        this.socketLock = new ReentrantLock();
        this.host = host;
    }

    /**
     * Synchronously runs the Query a single time.
     *
     * @return a list of Instances that match this Query
     * @throws IOException thrown on socket and network errors
     */
    public Set<Instance> runOnce() throws IOException {
        return runOnceOn(host);
    }

    /**
     * Synchronously runs the Query a single time.
     *
     * @param localhost address of the network interface to listen on
     * @return a list of Instances that match this Query
     * @throws IOException thrown on socket and network errors
     */
    public Set<Instance> runOnceOn(InetAddress localhost) throws IOException {
        logger.debug("Running query on {}", localhost);
        initialQuestion = new Question(service, domain);
        instances = Collections.synchronizedSet(new HashSet<>());
        try {
            openSocket(localhost);
            Thread listener = listenForResponses();
            while (!isServerIsListening()) {
                logger.debug("Server is not yet listening");
            }
            ask(initialQuestion);
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

    private void ask(Question question) throws IOException {
        if (questions.contains(question)) {
            logger.debug("We've already asked {}, we won't ask again", question);
            return;
        }

        questions.add(question);
        if (isUsingIPv4) {
            question.askOn(socket, mdnsGroupIPv4);
        }
        if (isUsingIPv6) {
            question.askOn(socket, mdnsGroupIPv6);
        }
    }

    private boolean isServerIsListening() {
        boolean retval;
        try {
            while (!socketLock.tryLock(WAIT_FOR_LISTENER_MS, TimeUnit.MILLISECONDS)) {
                socketLock.notify();
                logger.debug("Waiting to acquire socket lock");
            }
            if (listenerFinished) {
                throw new RuntimeException("Listener has already finished");
            }
            retval = listenerStarted;
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting to acquire socket lock: ", e);
            throw new RuntimeException("Server is not listening");
        } finally {
            socketLock.unlock();
        }
        return retval;
    }

    /**
     * Asynchronously runs the Query in a new thread.
     */
    @SuppressWarnings("unused")
    public void start() {
        throw new RuntimeException("Not implemented yet");
    }

    private void openSocket(InetAddress localhost) throws IOException {
        mdnsGroupIPv4 = InetAddress.getByName(MDNS_IP4_ADDRESS);
        mdnsGroupIPv6 = InetAddress.getByName(MDNS_IP6_ADDRESS);
        socket = new MulticastSocket(MDNS_PORT);
        socket.setInterface(localhost);
        try {
            socket.joinGroup(mdnsGroupIPv4);
            isUsingIPv4 = true;
        } catch (SocketException e) {
            logger.error("SocketException when joining group for {}, IPv4-only hosts will not be found",
                    MDNS_IP4_ADDRESS, e);
        }
        try {
            socket.joinGroup(mdnsGroupIPv6);
            isUsingIPv6 = true;
        } catch (SocketException e) {
            logger.error("SocketException when joining group for {}, IPv6-only hosts will not be found",
                    MDNS_IP6_ADDRESS, e);
        }
        if (!isUsingIPv4 && !isUsingIPv6) {
            throw new IOException("No usable network interfaces found");
        }
        socket.setTimeToLive(10);
        socket.setSoTimeout(browsingTimeout);
    }

    private Thread listenForResponses() {
        Thread listener = new Thread(this::collectResponses);
        listener.start();
        return listener;
    }

    private Set<Instance> collectResponses() {
        long startTime = System.currentTimeMillis();
        long currentTime = startTime;
        socketLock.lock();
        listenerStarted = true;
        listenerFinished = false;
        socketLock.unlock();
        for (int timeouts = 0; timeouts == 0 && currentTime - startTime < browsingTimeout; ) {
            byte[] responseBuffer = new byte[Message.MAX_LENGTH];
            DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
            try {
                logger.debug("Listening for responses...");
                socket.receive(responsePacket);
                currentTime = System.currentTimeMillis();
                logger.debug("Response received!");
//                logger.debug("Response of length {} at offset {}: {}", responsePacket.getLength(), responsePacket.getOffset(), responsePacket.getData());
                try {
                    Response response = Response.createFrom(responsePacket);
                    if (!response.answers(questions)) {
                        // This response isn't related to any of the questions we asked
                        logger.debug("This response doesn't answer any of our questions, ignoring it.");
                        timeouts = 0;
                        continue;
                    }
                    records.addAll(response.getRecords());
                    fetchMissingRecords();
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
        socketLock.lock();
        listenerFinished = true;
        socketLock.unlock();
        buildInstancesFromRecords();
        return instances;
    }

    /**
     * Verify that each PTR record has corresponding SRV, TXT, and either A or AAAA records.
     * Request any that are missing.
     */
    private void fetchMissingRecords() throws IOException {
        logger.debug("Records includes:");
        records.stream().forEach(r -> logger.debug("{}", r));
        for (PtrRecord ptr : records.stream().filter(r -> r instanceof PtrRecord).map(r -> (PtrRecord) r).collect(Collectors.toList())) {
            fetchMissingSrvRecordsFor(ptr);
            fetchMissingTxtRecordsFor(ptr);
        }
        for (SrvRecord srv : records.stream().filter(r -> r instanceof SrvRecord).map(r -> (SrvRecord) r).collect(Collectors.toList())) {
            fetchMissingAddressRecordsFor(srv);
        }
    }

    private void fetchMissingSrvRecordsFor(PtrRecord ptr) throws IOException {
        long numRecords = records.stream().filter(r -> r instanceof SrvRecord).filter(
                r -> r.getName().equals(ptr.getPtrName())
        ).count();
        if (numRecords == 0) {
            logger.debug("Response has no SRV records");
            querySrvRecordFor(ptr);
        }
    }

    private void fetchMissingTxtRecordsFor(PtrRecord ptr) throws IOException {
        long numRecords = records.stream().filter(r -> r instanceof TxtRecord).filter(
                r -> r.getName().equals(ptr.getPtrName())
        ).count();
        if (numRecords == 0) {
            logger.debug("Response has no TXT records");
            queryTxtRecordFor(ptr);
        }
    }

    private void fetchMissingAddressRecordsFor(SrvRecord srv) throws IOException {
        long numRecords = records.stream().filter(r -> r instanceof ARecord || r instanceof AaaaRecord).filter(
                r -> r.getName().equals(srv.getTarget())
        ).count();
        if (numRecords == 0) {
            logger.debug("Response has no A or AAAA records");
            queryAddressesFor(srv);
        }
    }

    private void querySrvRecordFor(PtrRecord ptr) throws IOException {
        Question question = new Question(ptr.getPtrName(), Question.QType.SRV, Question.QClass.IN);
        ask(question);
    }

    private void queryTxtRecordFor(PtrRecord ptr) throws IOException {
        Question question = new Question(ptr.getPtrName(), Question.QType.TXT, Question.QClass.IN);
        ask(question);
    }

    private void queryAddressesFor(SrvRecord srv) throws IOException {
        Question question = new Question(srv.getTarget(), Question.QType.A, Question.QClass.IN);
        ask(question);
        question = new Question(srv.getTarget(), Question.QType.AAAA, Question.QClass.IN);
        ask(question);
    }

    private void buildInstancesFromRecords() {
        records.stream().filter(r -> r instanceof PtrRecord && initialQuestion.answeredBy(r))
                .map(r -> (PtrRecord) r).forEach(ptr -> instances.add(Instance.createFromRecords(ptr, records)));
    }

    private void closeSocket() {
        if (socket != null) {
            socket.close();
            socket = null;
        }
    }
}
