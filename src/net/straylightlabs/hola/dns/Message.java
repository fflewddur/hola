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

public abstract class Message {
    protected final ByteBuffer buffer;

    public final static int MAX_LENGTH = 9000; // max size of mDNS packets, in bytes

    private final static int USHORT_MASK = 0xFFFF;

    protected Message() {
        buffer = ByteBuffer.allocate(MAX_LENGTH);
    }

    protected int readUnsignedShort() {
        return buffer.getShort() & USHORT_MASK;
    }

    public String dumpBuffer() {
        StringBuilder sb = new StringBuilder();
        int length = buffer.position();
        if (length == 0) {
            length = buffer.limit();
        }
        for (int i = 0; i < length; i++) {
            sb.append(String.format("%02x", buffer.get(i)));
            if ((i + 1) % 8 == 0) {
                sb.append('\n');
            } else if ((i + 1) % 2 == 0) {
                sb.append(' ');
            }
        }
        return sb.toString();
    }
}
