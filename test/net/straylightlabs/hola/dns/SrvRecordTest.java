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

import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertTrue;

public class SrvRecordTest {
    @Test
    public void testParser() throws UnknownHostException {
        Record record = buildRecord();

        assertTrue("name = Zelda._http._tcp.local.: " + record.getName(), record.getName().equals("Zelda._http._tcp.local."));
        assertTrue("type = SRV", record instanceof SrvRecord);
        assertTrue("ttl = 2600", record.getTTL() == 2600);
        SrvRecord srvRecord = (SrvRecord) record;
        assertTrue("priority = 500", srvRecord.getPriority() == 500);
        assertTrue("weight = 100", srvRecord.getWeight() == 100);
        assertTrue("port = 80", srvRecord.getPort() == 80);
        assertTrue("target = zelda.local.", srvRecord.getTarget().equals("zelda.local."));
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
        buffer.putShort((short) Record.Type.SRV.asUnsignedShort());
        buffer.putShort((short) Record.Class.IN.asUnsignedShort());
        buffer.putInt(2600);
        String target = "zelda.local.";
        buffer.putShort((short) (target.length() + 1 + 6));
        buffer.putShort((short) 500);
        buffer.putShort((short) 100);
        buffer.putShort((short) 80);
        RecordTest.addNameToBuffer(target, buffer);
        buffer.limit(buffer.position());
        buffer.rewind();
        return Record.fromBuffer(buffer);
    }
}
