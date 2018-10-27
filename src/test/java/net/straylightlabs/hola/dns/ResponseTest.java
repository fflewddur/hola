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

import java.net.DatagramPacket;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertTrue;

public class ResponseTest {
    @Test
    public void testParser() {
        Response response = buildResponse();

        assertTrue("User visible name = Zelda: " + response.getUserVisibleName(), response.getUserVisibleName().equals("Zelda"));
        assertTrue("No questions", response.getNumQuestions() == 0);
        assertTrue("One answer", response.getNumAnswers() == 1);
        assertTrue("No name serves", response.getNumNameServers() == 0);
        assertTrue("No additional records", response.getNumAdditionalRecords() == 0);
    }

    @Test
    public void testToStringForExceptions() {
        Response response = buildResponse();
        String string = response.toString();
        assertTrue("toString() is not null", string != null);
        assertTrue("toString() is not empty", string.length() > 0);
    }

    public static Response buildResponse() {
        ByteBuffer buffer = ByteBuffer.allocate(9000);
        buffer.putShort((short) 0);
        buffer.putShort((short) 0x8400); // Response bit + Authoritative answer
        buffer.putShort((short) 0); // 0 questions
        buffer.putShort((short) 1); // 1 answer
        buffer.putShort((short) 0); // 0 name servers
        buffer.putShort((short) 0); // 0 additional records
        RecordTest.addNameToBuffer("Zelda._http._tcp.local", buffer);
        buffer.putShort((short) Record.Type.PTR.asUnsignedShort());
        buffer.putShort((short) Record.Class.IN.asUnsignedShort());
        buffer.putInt(2600);
        buffer.putShort((short) 2);
        buffer.putShort((short) 0xC00C);
        buffer.limit(buffer.position());
        buffer.rewind();

        DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.limit());
        return Response.createFrom(packet);
    }
}
