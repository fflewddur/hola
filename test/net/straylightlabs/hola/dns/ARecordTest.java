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

public class ARecordTest {
    @Test
    public void testParser() throws UnknownHostException {
        Record record = buildRecord();

        assertTrue("name = Zelda._http._tcp.local.: " + record.getName(), record.getName().equals("Zelda._http._tcp.local."));
        assertTrue("type = A", record instanceof ARecord);
        assertTrue("ttl = 2600", record.getTTL() == 2600);
        ARecord aRecord = (ARecord) record;
        InetAddress address = InetAddress.getByAddress(new byte[]{10, 0, 1, 100});
        assertTrue("address = 10.0.1.100", aRecord.getAddress().equals(address));
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
        buffer.putShort((short) Record.Type.A.asUnsignedShort());
        buffer.putShort((short) Record.Class.IN.asUnsignedShort());
        buffer.putInt(2600);
        buffer.putShort((short) 4);
        buffer.put((byte) 10);
        buffer.put((byte) 0);
        buffer.put((byte) 1);
        buffer.put((byte) 100);
        buffer.limit(buffer.position());
        buffer.rewind();
        return Record.fromBuffer(buffer);
    }
}
