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
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TxtRecordTest {
    @Test
    public void testParser() throws UnknownHostException {
        Record record = buildRecord();

        assertTrue("name = Zelda._http._tcp.local.: " + record.getName(), record.getName().equals("Zelda._http._tcp.local."));
        assertTrue("type = TXT", record instanceof TxtRecord);
        assertTrue("ttl = 2600", record.getTTL() == 2600);
        TxtRecord txtRecord = (TxtRecord) record;
        Map<String, String> attributes = txtRecord.getAttributes();
        assertTrue("attribute 'software' == 'Hola'", attributes.get("software").equals("Hola"));
        assertTrue("attribute 'platform' == 'Mac OS OX'", attributes.get("platform").equals("Mac OS X"));
        assertFalse("attribute 'version' doesn't exist", attributes.containsKey("version"));
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
        buffer.putShort((short) Record.Type.TXT.asUnsignedShort());
        buffer.putShort((short) Record.Class.IN.asUnsignedShort());
        buffer.putInt(2600);
        String txt = "software=Hola.platform=Mac OS X";
        buffer.putShort((short) (txt.length() + 1));
        RecordTest.addNameToBuffer(txt, buffer);
        buffer.limit(buffer.position());
        buffer.rewind();
        return Record.fromBuffer(buffer);
    }
}
