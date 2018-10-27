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
