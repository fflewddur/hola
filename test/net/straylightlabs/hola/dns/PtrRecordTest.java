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

public class PtrRecordTest {
    @Test
    public void testParser() throws UnknownHostException {
        Record record = buildRecord();

        assertTrue("name = Zelda._http._tcp.local.: " + record.getName(), record.getName().equals("Zelda._http._tcp.local."));
        assertTrue("type = PTR", record instanceof PtrRecord);
        assertTrue("ttl = 2600", record.getTTL() == 2600);
        PtrRecord ptrRecord = (PtrRecord) record;
        assertTrue("user visible name = Zelda", ptrRecord.getUserVisibleName().equals("Zelda"));
    }

    @Test
    public void testParserWithEmptyName() throws UnknownHostException {
        Record record = buildRecordWithEmptyName();

        assertTrue("name = Zelda._http._tcp.local.: " + record.getName(), record.getName().equals("Zelda._http._tcp.local."));
        assertTrue("type = PTR", record instanceof PtrRecord);
        assertTrue("ttl = 2600", record.getTTL() == 2600);
        PtrRecord ptrRecord = (PtrRecord) record;
        assertTrue("user visible name = Untitled: " + ptrRecord.getUserVisibleName(), ptrRecord.getUserVisibleName().equals(PtrRecord.UNTITLED_NAME));
    }

    @Test
    public void testParserWithSingleName() throws UnknownHostException {
        Record record = buildRecordWithSingleName();

        assertTrue("name = Zelda._http._tcp.local.: " + record.getName(), record.getName().equals("Zelda._http._tcp.local."));
        assertTrue("type = PTR", record instanceof PtrRecord);
        assertTrue("ttl = 2600", record.getTTL() == 2600);
        PtrRecord ptrRecord = (PtrRecord) record;
        assertTrue("user visible name = Zelda", ptrRecord.getUserVisibleName().equals("Zelda"));
    }

    @Test
    public void testToStringForExceptions() {
        Record record = buildRecord();
        String string = record.toString();
        assertTrue("toString() is not null", string != null);
        assertTrue("toString() is not empty", string.length() > 0);
    }

    private Record buildRecord() {
        ByteBuffer buffer = buildBuffer();
        buffer.putShort((short) 2);
        buffer.putShort((short) 0xC000);
        buffer.limit(buffer.position());
        buffer.rewind();
        return Record.fromBuffer(buffer);
    }

    private Record buildRecordWithSingleName() {
        ByteBuffer buffer = buildBuffer();
        String name = "Zelda";
        buffer.putShort((short) (name.length() + 1));
        RecordTest.addNameToBuffer(name, buffer);
        buffer.limit(buffer.position());
        buffer.rewind();
        return Record.fromBuffer(buffer);
    }

    private Record buildRecordWithEmptyName() {
        ByteBuffer buffer = buildBuffer();
        buffer.putShort((short) 0);
        buffer.limit(buffer.position());
        buffer.rewind();
        return Record.fromBuffer(buffer);
    }

    private ByteBuffer buildBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(500);
        RecordTest.addNameToBuffer("Zelda._http._tcp.local.", buffer);
        buffer.putShort((short) Record.Type.PTR.asUnsignedShort());
        buffer.putShort((short) Record.Class.IN.asUnsignedShort());
        buffer.putInt(2600);
        return buffer;
    }
}
