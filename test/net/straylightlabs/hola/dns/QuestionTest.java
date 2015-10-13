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

import net.straylightlabs.hola.sd.Service;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertTrue;

public class QuestionTest {
    @Test
    public void testParser() {
        String name = "_http._tcp.local.";
        ByteBuffer buffer = RecordTest.createBufferForNames(name);
        buffer.putShort((short) Question.QType.PTR.asUnsignedShort());
        buffer.putShort((short) Question.QClass.IN.asUnsignedShort());
        buffer.limit(buffer.position());
        buffer.rewind();

        Question question = Question.fromBuffer(buffer);
        assertTrue("QName = _http._tcp.local.", question.getQName().equals(name));
        assertTrue("QType = PTR", question.getQType() == Question.QType.PTR);
        assertTrue("QClass = IN", question.getQClass() == Question.QClass.IN);
    }

    @Test
    public void testBuilder() {
        String name = "_http._tcp.local.";
        Service service = Service.fromName("_http._tcp");
        Question question = new Question(service, Domain.LOCAL);
        assertTrue("QName = _http._tcp.local.", question.getQName().equals(name));
        assertTrue("QType = PTR", question.getQType() == Question.QType.PTR);
        assertTrue("QClass = IN", question.getQClass() == Question.QClass.IN);
    }

    @Test
    public void testToStringForExceptions() {
        Service service = Service.fromName("_http._tcp");
        Question question = new Question(service, Domain.LOCAL);
        String string = question.toString();
        assertTrue("toString() is not null", string != null);
        assertTrue("toString() is not empty", string.length() > 0);
        String buffer = question.dumpBuffer();
        assertTrue("dumpBuffer() is not null", buffer != null);
        assertTrue("dumpBuffer() is not empty", buffer.length() > 0);
    }
}
