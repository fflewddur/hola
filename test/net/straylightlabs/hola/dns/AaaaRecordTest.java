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

import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertTrue;

public class AaaaRecordTest {
    @Test
    public void testParser() throws UnknownHostException {
        Record record = buildRecord();

        assertTrue("name = Zelda._http._tcp.local.: " + record.getName(), record.getName().equals("Zelda._http._tcp.local."));
        assertTrue("type = AAAA", record instanceof AaaaRecord);
        assertTrue("ttl = 0", record.getTTL() == 0);
        AaaaRecord aRecord = (AaaaRecord) record;
        InetAddress address = InetAddress.getByAddress(new byte[]{(byte) 0xfe, (byte) 0x80, 0x60, 0x40, 0x20, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xda, (byte) 0xde});
        assertTrue("address = fe80:6040:2000::dade", aRecord.getAddress().equals(address));
    }

    @Test
    public void testToStringForExceptions() {
        Record record = buildRecord();
        String string = record.toString();
        assertTrue("toString() is not null", string != null);
        assertTrue("toString() is not empty", string.length() > 0);
    }

    private Record buildRecord() {
        ByteBuffer buffer = ByteBuffer.allocate(500);
        RecordTest.addNameToBuffer("Zelda._http._tcp.local.", buffer);
        buffer.putShort((short) Record.Type.AAAA.asUnsignedShort());
        buffer.putShort((short) Record.Class.IN.asUnsignedShort());
        buffer.putInt(0);
        buffer.putShort((short) 16);
        buffer.put((byte) 0xfe);
        buffer.put((byte) 0x80);
        buffer.put((byte) 0x60);
        buffer.put((byte) 0x40);
        buffer.put((byte) 0x20);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0xda);
        buffer.put((byte) 0xde);
        buffer.limit(buffer.position());
        buffer.rewind();
        return Record.fromBuffer(buffer);
    }
}
