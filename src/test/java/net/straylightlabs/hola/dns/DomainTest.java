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
