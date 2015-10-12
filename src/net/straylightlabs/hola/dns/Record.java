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

import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

abstract class Record {
    protected final String name;
    protected final long ttl;

    protected final Class recordClass;

    private final static Logger logger = LoggerFactory.getLogger(Record.class);

    public final static int USHORT_MASK = 0xFFFF;
    public final static long UINT_MASK = 0xFFFFFFFFL;
    public final static String NAME_CHARSET = "UTF-8";

    public static Record fromBuffer(ByteBuffer buffer) {
        String name = readNameFromBuffer(buffer);
        Type type = Type.fromInt(buffer.getShort() & USHORT_MASK);
//        int rrClassByte = buffer.getShort() & 0x7FFF;
        int tmp = buffer.getShort() & 0xFFFF;
        // FIXME allow the user to see that cache's should be flushed?
        boolean flushCache = (tmp & 0x8000) == 0x8000;
        int rrClassByte = tmp & 0x7FFF;
        Class recordClass = Class.fromInt(rrClassByte);
        long ttl = buffer.getInt() & UINT_MASK;
        int rdLength = buffer.getShort() & USHORT_MASK;

        switch (type) {
            case A:
                try {
                    return new ARecord(buffer, name, recordClass, ttl);
                } catch (UnknownHostException e) {
                    throw new IllegalArgumentException("Buffer does not represent a valid A record");
                }
            case AAAA:
                try {
                    return new AaaaRecord(buffer, name, recordClass, ttl);
                } catch (UnknownHostException e) {
                    throw new IllegalArgumentException("Buffer does not represent a valid AAAA record");
                }
            case PTR:
                return new PtrRecord(buffer, name, recordClass, ttl, rdLength);
            case SRV:
                return new SrvRecord(buffer, name, recordClass, ttl);
            case TXT:
                return new TxtRecord(buffer, name, recordClass, ttl, rdLength);
            default:
                throw new IllegalArgumentException("Buffer represents an unsupported record type");
        }
    }

    protected Record(String name, Class recordClass, long ttl) {
        this.name = name;
        this.recordClass = recordClass;
        this.ttl = ttl;
    }

    public static String readNameFromBuffer(ByteBuffer buffer) {
        List<String> labels = new ArrayList<>();
        int labelLength;
        int continueFrom = -1;
        do {
            buffer.mark();
            labelLength = buffer.get() & 0xFF;
            if (isPointer(labelLength)) {
                buffer.reset();
                int offset = buffer.getShort() & 0x3FFF;
                if (continueFrom < 0) {
                    continueFrom = buffer.position();
                }
                buffer.position(offset);
            } else {
                String label = readLabel(buffer, labelLength);
                labels.add(label);
            }
        } while (labelLength != 0);

        if (continueFrom >= 0) {
            buffer.position(continueFrom);
        }

        return labels.stream().collect(Collectors.joining("."));
    }

    private static boolean isPointer(int octet) {
        return (octet & 0xC0) == 0xC0;
    }

    private static String readLabel(ByteBuffer buffer, int length) {
        String label = "";
        if (length > 0) {
            byte[] labelBuffer = new byte[length];
            buffer.get(labelBuffer);
            try {
                label = new String(labelBuffer, NAME_CHARSET);
            } catch (UnsupportedEncodingException e) {
                System.err.println("UnsupportedEncoding: " + e);
            }
        }
        return label;
    }

    public static List<String> readStringsFromBuffer(ByteBuffer buffer, int length) {
        List<String> strings = new ArrayList<>();
        int bytesRead = 0;
        do {
            int stringLength = buffer.get() & 0xFF;
            String label = readLabel(buffer, stringLength);
            bytesRead += label.length() + 1;
            strings.add(label);
        } while (bytesRead < length);
        return strings;
    }

    String getName() {
        return name;
    }

    long getTTL() {
        return ttl;
    }

    @Override
    public String toString() {
        return "Record{" +
                "name='" + name + '\'' +
                ", recordClass=" + recordClass +
                ", ttl=" + ttl +
                '}';
    }

    enum Type {
        A(1),
        NS(2),
        CNAME(5),
        SOA(6),
        NULL(10),
        WKS(11),
        PTR(12),
        HINFO(13),
        MINFO(14),
        MX(15),
        TXT(16),
        AAAA(28),
        SRV(33);

        private final int value;

        public static Type fromInt(int val) {
            for (Type type : values()) {
                if (type.value == val) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.format("Can't convert 0x%04x to a Type", val));
        }

        Type(int value) {
            this.value = value;
        }

        public int asUnsignedShort() {
            return value & USHORT_MASK;
        }
    }

    enum Class {
        IN(1);

        private final int value;

        public static Class fromInt(int val) {
            for (Class c : values()) {
                if (c.value == val) {
                    return c;
                }
            }
            throw new IllegalArgumentException(String.format("Can't convert 0x%04x to a Class", val));
        }

        Class(int value) {
            this.value = value;
        }

        public int asUnsignedShort() {
            return value & USHORT_MASK;
        }
    }
}
