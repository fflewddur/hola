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

package net.straylightlabs.hola.sd;

import net.straylightlabs.hola.dns.Response;

import java.net.InetAddress;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Instance {
    private final String name;
    private final List<InetAddress> addresses;
    private final int port;
    private final Map<String, String> attributes;

    static Instance createFrom(Response response) {
        String name = response.getUserVisibleName();
        List<InetAddress> addresses = response.getInetAddresses();
        int port = response.getPort();
        Map<String, String> attributes = response.getAttributes();

        return new Instance(name, addresses, port, attributes);
    }

    private Instance(String name, List<InetAddress> addresses, int port, Map<String, String> attributes) {
        this.name = name;
        this.addresses = addresses;
        this.port = port;
        this.attributes = attributes;
    }

    /**
     * Get the user-visible name associated with this instance.
     *
     * This value comes from the instance's PTR record.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the list of IP addresses associated with this instance.
     *
     * These values come from the instance's A and AAAA records.
     *
     * @return list of addresses
     */
    public List<InetAddress> getAddresses() {
        return Collections.unmodifiableList(addresses);
    }

    /**
     * Get the port number associated with this instance.
     *
     * This value comes from the instance's SRV record.
     *
     * @return port number
     */
    public int getPort() {
        return port;
    }

    /**
     * Check whether this instance has the specified attribute.
     *
     * Attributes come from the instance's TXT records.
     *
     * @param attribute name of the attribute to search for
     * @return true if the instance has a value for attribute, false otherwise
     */
    public boolean hasAttribute(String attribute) {
        return attributes.containsKey(attribute);
    }

    /**
     * Get the value of the specified attribute.
     *
     * Attributes come from the instance's TXT records.
     *
     * @param attribute name of the attribute to search for
     * @return value of the given attribute, or null if the attribute doesn't exist in this Instance
     */
    public String lookupAttribute(String attribute) {
        return attributes.get(attribute);
    }

    @Override
    public String toString() {
        return "Instance{" +
                "name='" + name + '\'' +
                ", addresses=" + addresses +
                ", port=" + port +
                ", attributes=" + attributes +
                '}';
    }
}
