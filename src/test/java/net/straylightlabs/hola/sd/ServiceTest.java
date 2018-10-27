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

package net.straylightlabs.hola.sd;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ServiceTest {
    @Test
    public void testServiceFromValidString() {
        Service service;
        service = Service.fromName("_tcp");
        assertTrue("fromName(_tcp) == _tcp: " + service.getName(), service.getName().equals("_tcp"));
        service = Service.fromName("_tcp.");
        assertTrue("fromName(_tcp.) == _tcp: " + service.getName(), service.getName().equals("_tcp"));
        service = Service.fromName("_udp");
        assertTrue("fromName(_udp) == _udp: " + service.getName(), service.getName().equals("_udp"));
        service = Service.fromName("_udp.");
        assertTrue("fromName(_udp.) == _udp: " + service.getName(), service.getName().equals("_udp"));
        service = Service.fromName("_http._tcp");
        assertTrue("fromName(_http._tcp) == _http._tcp: " + service.getName(), service.getName().equals("_http._tcp"));
        service = Service.fromName("_http._tcp.");
        assertTrue("fromName(_http._tcp.) == _http._tcp: " + service.getName(), service.getName().equals("_http._tcp"));
        service = Service.fromName("_my_service._tcp");
        assertTrue("fromName(_my_service._tcp) == _my_service._tcp: " + service.getName(), service.getName().equals("_my_service._tcp"));
        service = Service.fromName("_my_service._tcp.");
        assertTrue("fromName(_my_service._tcp.) == _my_service._tcp: " + service.getName(), service.getName().equals("_my_service._tcp"));
        service = Service.fromName("_my-service._tcp");
        assertTrue("fromName(_my-service._tcp) == _my-service._tcp: " + service.getName(), service.getName().equals("_my-service._tcp"));
        service = Service.fromName("_my-service._tcp.");
        assertTrue("fromName(_my-service._tcp.) == _my-service._tcp: " + service.getName(), service.getName().equals("_my-service._tcp"));
    }

    @Test
    public void testServiceFromStringWithDomain() {
        Service service;
        service = Service.fromName("_http._tcp.local.");
        assertTrue("fromName(_http._tcp.local.) = _http._tcp", service.getName().equals("_http._tcp"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testServiceFromInvalidString() {
        Service.fromName("invalidname._tcp");
    }

    @Test
    public void testEquals() {
        Service a = Service.fromName("_http._tcp.straylightlabs.net");
        Service b = Service.fromName("_http._tcp.straylightlabs.net");
        Service c = Service.fromName("_airport._udp");
        Service d = Service.fromName("_airport._udp");

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
        Service a = Service.fromName("_http._tcp.straylightlabs.net");
        String string = a.toString();
        assertTrue("toString() is not null", string != null);
        assertTrue("toString() is not empty", string.length() > 0);
    }
}
