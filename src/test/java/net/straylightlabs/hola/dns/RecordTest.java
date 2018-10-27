/*
 * The MIT License
 *
 * Copyright (c) 2015-2018 Todd Kulesza <todd@dropline.net>
 *
 * This file is part of Hola.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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
