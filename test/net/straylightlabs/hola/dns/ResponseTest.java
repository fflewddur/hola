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
