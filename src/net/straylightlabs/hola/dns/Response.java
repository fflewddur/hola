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

package net.straylightlabs.hola.dns;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.*;
import java.util.stream.Collectors;

public class Response extends Message {
    private final List<Question> questions;
    private final List<Record> records;
    private int numQuestions;
    private int numAnswers;
    private int numNameServers;
    private int numAdditionalRecords;

    private final static Logger logger = LoggerFactory.getLogger(Response.class);

    private final static int QR_MASK = 0x8000;
    private final static int OPCODE_MASK = 0x7800;
    private final static int RCODE_MASK = 0xF;

    public static Response createFrom(DatagramPacket packet) {
        Response response = new Response(packet);
        response.parseRecords();
        return response;
    }

    private Response() {
        questions = new ArrayList<>();
        records = new ArrayList<>();
    }

    private Response(DatagramPacket packet) {
        this();
        byte[] dstBuffer = buffer.array();
        System.arraycopy(packet.getData(), packet.getOffset(), dstBuffer, 0, packet.getLength());
        buffer.limit(packet.getLength());
        buffer.position(0);
    }

    private void parseRecords() {
        parseHeader();
        for (int i = 0; i < numQuestions; i++) {
            Question question = Question.fromBuffer(buffer);
            questions.add(question);
        }
        for (int i = 0; i < numAnswers; i++) {
            Record record = Record.fromBuffer(buffer);
            records.add(record);
        }
        for (int i = 0; i < numNameServers; i++) {
            Record record = Record.fromBuffer(buffer);
            records.add(record);
        }
        for (int i = 0; i < numAdditionalRecords; i++) {
            Record record = Record.fromBuffer(buffer);
            records.add(record);
        }
    }

    private void parseHeader() {
        readUnsignedShort(); // Skip over the ID
        int codes = readUnsignedShort();
        if ((codes & QR_MASK) != QR_MASK) {
            // FIXME create a custom Exception for DNS errors
            throw new IllegalArgumentException("Packet is not a DNS response");
        }
        if ((codes & OPCODE_MASK) != 0) {
            throw new IllegalArgumentException("mDNS response packets can't have OPCODE values");
        }
        if ((codes & RCODE_MASK) != 0) {
            throw new IllegalArgumentException("mDNS response packets can't have RCODE values");
        }
        numQuestions = readUnsignedShort();
        numAnswers = readUnsignedShort();
        numNameServers = readUnsignedShort();
        numAdditionalRecords = readUnsignedShort();
    }

    public String getUserVisibleName() {
        Optional<PtrRecord> record = records.stream().filter(r -> r instanceof PtrRecord).map(r -> (PtrRecord) r).findAny();
        if (record.isPresent()) {
            return record.get().getUserVisibleName();
        } else {
            logger.debug("No PTR records: {}", records);
            throw new IllegalStateException("Cannot call getUserVisibleName when no PTR record is available");
        }
    }

    public List<InetAddress> getInetAddresses() {
        List<InetAddress> addresses = new ArrayList<>();
        addresses.addAll(records.stream().filter(r -> r instanceof ARecord).map(r -> ((ARecord) r).getAddress()).collect(Collectors.toList()));
        addresses.addAll(records.stream().filter(r -> r instanceof AaaaRecord).map(r -> ((AaaaRecord) r).getAddress()).collect(Collectors.toList()));
        if (addresses.size() == 0) {
            logger.debug("No A/AAAA records: {}", records);
            throw new IllegalStateException("Cannot call getInetAddresses when no address records are available");
        }
        return addresses;
    }

    public int getPort() {
        Optional<SrvRecord> record = records.stream().filter(r -> r instanceof SrvRecord).map(r -> (SrvRecord) r).findAny();
        if (record.isPresent()) {
            return record.get().getPort();
        } else {
            logger.debug("No SRV records: {}", records);
            throw new IllegalStateException("Cannot cal getPort when no SRV record is available");
        }
    }

    public Map<String, String> getAttributes() {
        Optional<TxtRecord> record = records.stream().filter(r -> r instanceof TxtRecord).map(r -> (TxtRecord) r).findAny();
        if (record.isPresent()) {
            return record.get().getAttributes();
        } else {
            return Collections.emptyMap();
        }
    }

    /**
     * Returns true if this response contains at least one A/AAAA record, SRV record, and TXT record.
     */
    public boolean isComplete() {
        return (hasInetAddresses() && hasSrvRecords() && hasTxtRecords());
    }

    /**
     * Returns true if this response has at least one A or AAAA record.
     */
    public boolean hasInetAddresses() {
        long count = 0;
        count += records.stream().filter(r -> r instanceof ARecord).count();
        count += records.stream().filter(r -> r instanceof AaaaRecord).count();
        return count > 0;
    }

    /**
     * Returns true if this response has at least one SRV record.
     */
    public boolean hasSrvRecords() {
        long count = 0;
        count += records.stream().filter(r -> r instanceof SrvRecord).count();
        return count > 0;
    }

    /**
     * Returns true if this response has at least one TXT record.
     */
    public boolean hasTxtRecords() {
        long count = 0;
        count += records.stream().filter(r -> r instanceof TxtRecord).count();
        return count > 0;
    }

    /**
     * Returns the PTR name associated with this response.
     */
    public String getPtr() {
        Optional<PtrRecord> record = records.stream().filter(r -> r instanceof PtrRecord).map(r -> (PtrRecord) r).findAny();
        if (record.isPresent()) {
            return record.get().getPtrName();
        } else {
            logger.debug("No PTR records: {}", records);
            throw new IllegalStateException("Cannot call getPtr when no PTR record is available");
        }
    }

    /**
     * Returns true if this response contains records for the given instance, false otherwise.
     */
    public boolean containsRecordsFor(String instance) {
        boolean containsRecordsForInstance = false;
        for (Record record : records) {
            if (instance.equals(record.getName())) {
                containsRecordsForInstance = true;
            }
        }
        return containsRecordsForInstance;
    }

    /**
     * Combines records from @other with this response.
     */
    public Response mergeWith(Response other) {
        if (other != null) {
            this.records.addAll(other.records);
        }
        return this;
    }

    @Override
    public String toString() {
        return "Response{" +
                "questions=" + questions +
                ", records=" + records +
                ", numQuestions=" + numQuestions +
                ", numAnswers=" + numAnswers +
                ", numNameServers=" + numNameServers +
                ", numAdditionalRecords=" + numAdditionalRecords +
                '}';
    }

    // Package-private methods for unit tests

    int getNumQuestions() {
        return numQuestions;
    }

    int getNumAnswers() {
        return numAnswers;
    }

    int getNumNameServers() {
        return numNameServers;
    }

    int getNumAdditionalRecords() {
        return numAdditionalRecords;
    }
}
