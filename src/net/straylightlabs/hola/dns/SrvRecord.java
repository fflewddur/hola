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

import java.nio.ByteBuffer;

public class SrvRecord extends Record {
    private final int priority;
    private final int weight;
    private final int port;
    private final String target;

    public SrvRecord(ByteBuffer buffer, String name, Record.Class recordClass, long ttl) {
        super(name, recordClass, ttl);
        priority = buffer.getShort() & USHORT_MASK;
        weight = buffer.getShort() & USHORT_MASK;
        port = buffer.getShort() & USHORT_MASK;
        target = readNameFromBuffer(buffer);
    }

    public int getPriority() {
        return priority;
    }

    public int getWeight() {
        return weight;
    }

    public int getPort() {
        return port;
    }

    public String getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "SrvRecord{" +
                "name='" + name + '\'' +
                ", recordClass=" + recordClass +
                ", ttl=" + ttl +
                ", priority=" + priority +
                ", weight=" + weight +
                ", port=" + port +
                ", target='" + target + '\'' +
                '}';
    }
}
