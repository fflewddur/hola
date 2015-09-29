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
            throw new IllegalStateException("Cannot call getName when no PTR record is available");
        }
    }

    public List<InetAddress> getInetAddresses() {
        List<InetAddress> addresses = new ArrayList<>();
        addresses.addAll(records.stream().filter(r -> r instanceof ARecord).map(r -> ((ARecord) r).getAddress()).collect(Collectors.toList()));
        addresses.addAll(records.stream().filter(r -> r instanceof AaaaRecord).map(r -> ((AaaaRecord) r).getAddress()).collect(Collectors.toList()));
        if (addresses.size() == 0) {
            throw new IllegalStateException("Cannot call getInetAddresses when no address records are available");
        }
        return addresses;
    }

    public int getPort() {
        Optional<SrvRecord> record = records.stream().filter(r -> r instanceof SrvRecord).map(r -> (SrvRecord) r).findAny();
        if (record.isPresent()) {
            return record.get().getPort();
        } else {
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
