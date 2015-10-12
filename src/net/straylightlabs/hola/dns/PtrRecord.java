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

class PtrRecord extends Record {
    private final String userVisibleName;
    private final String ptrName;

    public final static String UNTITLED_NAME = "Untitled";

    public PtrRecord(ByteBuffer buffer, String name, Class recordClass, long ttl, int rdLength) {
        super(name, recordClass, ttl);
        if (rdLength > 0) {
            ptrName = readNameFromBuffer(buffer);
        } else {
            ptrName = "";
        }
        userVisibleName = buildUserVisibleName();
    }

    public String getPtrName() {
        return ptrName;
    }

    public String getUserVisibleName() {
        return userVisibleName;
    }

    private String buildUserVisibleName() {
        String[] parts = ptrName.split("\\.");
        if (parts[0].length() > 0) {
            return parts[0];
        } else {
            return UNTITLED_NAME;
        }
    }

    @Override
    public String toString() {
        return "PtrRecord{" +
                "name='" + name + '\'' +
                ", recordClass=" + recordClass +
                ", ttl=" + ttl +
                ", ptrName='" + ptrName + '\'' +
                '}';
    }
}
