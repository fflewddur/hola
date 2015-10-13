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

import net.straylightlabs.hola.sd.Query;
import net.straylightlabs.hola.sd.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class Question extends Message {
    private final String qName;
    private final QType qType;
    private final QClass qClass;

    private final static Logger logger = LoggerFactory.getLogger(Question.class);

    private final static short UNICAST_RESPONSE_BIT = (short) 0x8000;

    public static Question fromBuffer(ByteBuffer buffer) {
        String name = Record.readNameFromBuffer(buffer);
        QType type = QType.fromInt(buffer.getShort() & Record.USHORT_MASK);
        QClass qClass = QClass.fromInt(buffer.getShort() & Record.USHORT_MASK);
        return new Question(name, type, qClass);
    }

    public Question(String name, QType type, QClass qClass) {
        super();
        this.qName = name;
        this.qType = type;
        this.qClass = qClass;
        build();
    }

    public Question(Service service, Domain domain) {
        super();
        this.qName = service.getName() + "." + domain.getName();
        this.qType = QType.PTR;
        this.qClass = QClass.IN;
        build();
    }

    private void build() {
        buildHeader();

        // QNAME
        for (String label : qName.split("\\.")) {
            addLabelToBuffer(label);
        }
        addLabelToBuffer("");

        // QTYPE
        buffer.putShort((short) qType.asUnsignedShort());

        // QCLASS
        // FIXME Only set unicast bit for initial queries
        buffer.putShort((short) (qClass.asUnsignedShort() | UNICAST_RESPONSE_BIT));
    }

    private void addLabelToBuffer(String label) {
        byte[] labelBytes = label.getBytes();
        buffer.put((byte) (labelBytes.length & 0xff));
        buffer.put(labelBytes);
    }

    private void buildHeader() {
//        super.buildHeader();
        buffer.putShort((short) 0x0); // ID should be 0
        buffer.put((byte) 0x0);
        buffer.put((byte) 0x0);
        buffer.putShort((short) 0x1); // 1 question
        buffer.putShort((short) 0x0); // 0 answers
        buffer.putInt(0x0); // no nameservers or additional records
    }

    public void askOn(MulticastSocket socket) throws IOException {
        logger.debug("Asking question {}", this);
        try {
            InetAddress groupIPv4 = InetAddress.getByName(Query.MDNS_IP4_ADDRESS);
            InetAddress groupIPv6 = InetAddress.getByName(Query.MDNS_IP6_ADDRESS);
            askWithGroup(groupIPv4, socket);
            askWithGroup(groupIPv6, socket);
        } catch (UnknownHostException e) {
            System.err.println("UnknownHostException " + e);
        }
    }

    private void askWithGroup(InetAddress group, MulticastSocket socket) throws IOException {
        DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.position(), group, Query.MDNS_PORT);
        packet.setAddress(group);
        socket.send(packet);
    }

    String getQName() {
        return qName;
    }

    QType getQType() {
        return qType;
    }

    QClass getQClass() {
        return qClass;
    }

    @Override
    public String toString() {
        return "Question{" +
                "qName=" + qName +
                ", qType=" + qType +
                ", qClass=" + qClass +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Question question = (Question) o;

        return qName.equals(question.qName) && qType == question.qType && qClass == question.qClass;
    }

    @Override
    public int hashCode() {
        int result = qName.hashCode();
        result = 31 * result + qType.hashCode();
        result = 31 * result + qClass.hashCode();
        return result;
    }

    public enum QType {
        A(1),
        NS(2),
        CNAME(5),
        SOA(6),
        MB(7),
        MG(8),
        MR(9),
        NULL(10),
        WKS(11),
        PTR(12),
        HINFO(13),
        MINFO(14),
        MX(15),
        TXT(16),
        SRV(33),
        ANY(255);

        private final int value;

        public static QType fromInt(int val) {
            for (QType type : values()) {
                if (type.value == val) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Can't convert " + val + " to a QType");
        }

        QType(int value) {
            this.value = value;
        }

        public int asUnsignedShort() {
            return value & Record.USHORT_MASK;
        }
    }

    public enum QClass {
        IN(1),
        ANY(255);

        private final int value;

        public static QClass fromInt(int val) {
            for (QClass c : values()) {
                if (c.value == (val & ~UNICAST_RESPONSE_BIT)) {
                    return c;
                }
            }
            throw new IllegalArgumentException("Can't convert " + val + " to a QClass");
        }

        QClass(int value) {
            this.value = value;
        }

        public int asUnsignedShort() {
            return value & Record.USHORT_MASK;
        }
    }
}
