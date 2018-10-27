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

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DomainTest {
    @Test
    public void testDomainFromValidString() {
        Domain domain;
        domain = Domain.fromName("local");
        assertTrue("fromName(local) == local: " + domain.getName(), domain.getName().equals("local"));
        domain = Domain.fromName("local.");
        assertTrue("fromName(local.) == local: " + domain.getName(), domain.getName().equals("local"));
    }

    @Test
    public void testDomainFromStringWithService() {
        Domain domain;
        domain = Domain.fromName("_http._tcp.local");
        assertTrue("fromName(_http._tcp.local) == local: " + domain.getName(), domain.getName().equals("local"));
    }

    @Test
    public void testShortLabel() {
        Domain domain = Domain.fromName("_http._tcp.local");
        List<String> labels = domain.getLabels();
        assertTrue("labels.size() = 1", labels.size() == 1);
        assertTrue("labels.get(0) = local", labels.get(0).equals("local"));
    }

    @Test
    public void testLongerLabel() {
        Domain domain = Domain.fromName("_http._tcp.straylightlabs.net");
        List<String> labels = domain.getLabels();
        assertTrue("labels.size() = 2", labels.size() == 2);
        assertTrue("labels.get(0) = straylightlabs", labels.get(0).equals("straylightlabs"));
        assertTrue("labels.get(1) = net", labels.get(1).equals("net"));
    }

    @Test
    public void testEquals() {
        Domain a = Domain.fromName("_http._tcp.straylightlabs.net");
        Domain b = Domain.fromName("_http._tcp.straylightlabs.net");
        Domain c = Domain.fromName("local");
        Domain d = Domain.fromName("local");

        assertTrue("a == b", a.equals(b));
        assertTrue("b == a", b.equals(a));
        assertTrue("c == d", c.equals(d));
        assertTrue("d == c", d.equals(c));
        assertFalse("a != c", a.equals(c));
        assertFalse("a != d", a.equals(d));
        assertFalse("b != c", b.equals(c));
        assertFalse("b != d", b.equals(d));
    }

    @Test
    public void testToStringForExceptions() {
        Domain a = Domain.fromName("_http._tcp.straylightlabs.net");
        String string = a.toString();
        assertTrue("toString() is not null", string != null);
        assertTrue("toString() is not empty", string.length() > 0);
    }

    @Test
    public void testHashCodes() {
        Domain a = Domain.fromName("_http._tcp.straylightlabs.net");
        Domain b = Domain.fromName("_http._tcp.straylightlabs.net");
        Domain c = Domain.fromName("local");
        Domain d = Domain.fromName("local");

        assertTrue("a == b", a.hashCode() == b.hashCode());
        assertTrue("b == a", b.hashCode() == a.hashCode());
        assertTrue("c == d", c.hashCode() == d.hashCode());
        assertTrue("d == c", d.hashCode() == c.hashCode());
        assertFalse("a != c", a.hashCode() == c.hashCode());
        assertFalse("a != d", a.hashCode() == d.hashCode());
        assertFalse("b != c", b.hashCode() == c.hashCode());
        assertFalse("b != d", b.hashCode() == d.hashCode());
    }

    @Test
    public void testEqualsNull() {
        Domain a = Domain.fromName("_http._tcp.straylightlabs.net");
        Domain b = null;

        assertFalse("a != b", a.equals(b));
    }
}
