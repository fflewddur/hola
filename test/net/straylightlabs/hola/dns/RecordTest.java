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

import java.nio.ByteBuffer;

import static org.junit.Assert.assertTrue;

public class RecordTest {
    @Test
    public void testReadNameFromBuffer() {
        ByteBuffer buffer = createBufferForNames("_http._tcp.local.");
        buffer.rewind();
        String name = Record.readNameFromBuffer(buffer);
        assertTrue("name = _http._tcp.local.: " + name, name.equals("_http._tcp.local."));
        assertTrue("position = 18: " + buffer.position(), buffer.position() == 18);
    }

    @Test
    public void testReadNameFromCompressedBuffer() {
        ByteBuffer buffer = createBufferForNames("_http._tcp.local.", "_www");
        buffer.reset();
        int offset = 6 | 0xC000;
        buffer.putShort((short) (offset & 0xFFFF));
        buffer.rewind();

        String name = Record.readNameFromBuffer(buffer);
        assertTrue("name = _http._tcp.local.: " + name, name.equals("_http._tcp.local."));
        assertTrue("position = 18: " + buffer.position(), buffer.position() == 18);
        name = Record.readNameFromBuffer(buffer);
        assertTrue("name = _www._tcp.local.: " + name, name.equals("_www._tcp.local."));
        assertTrue("position = 25: " + buffer.position(), buffer.position() == 25);
    }

    @Test
    public void testParserNoRData() {
        ByteBuffer buffer = createBufferForNames("_http._tcp.local.");
        buffer.putShort((short) Record.Type.PTR.asUnsignedShort());
        buffer.putShort((short) Record.Class.IN.asUnsignedShort());
        buffer.putInt(3600);
        buffer.putShort((short) 0);
        buffer.rewind();

        Record record = Record.fromBuffer(buffer);
        assertTrue("is instanceof PtrRecord", record instanceof PtrRecord);
        assertTrue("name == _http._tcp.local.: " + record.getName(), record.getName().equals("_http._tcp.local."));
        assertTrue("TTL == 3600", record.getTTL() == 3600);
    }

    public static ByteBuffer createBufferForNames(String... names) {
        ByteBuffer buffer = ByteBuffer.allocate(9000);
        for (String name : names) {
            String[] labels = name.split("\\.");
            for (String label : labels) {
                byte[] bytes = label.getBytes();
                buffer.put((byte) bytes.length);
                buffer.put(bytes);
            }
            buffer.mark();
            buffer.put((byte) 0);
        }
        return buffer;
    }

    public static void addNameToBuffer(String name, ByteBuffer buffer) {
        String[] labels = name.split("\\.");
        for (String label : labels) {
            byte[] bytes = label.getBytes();
            buffer.put((byte) bytes.length);
            buffer.put(bytes);
        }
        buffer.mark();
        buffer.put((byte) 0);
    }
}
