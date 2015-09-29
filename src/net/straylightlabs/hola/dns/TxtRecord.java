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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TxtRecord extends Record {
    private Map<String, String> attributes;

    public TxtRecord(ByteBuffer buffer, String name, Record.Class recordClass, long ttl, int length) {
        super(name, recordClass, ttl);
        List<String> strings = readStringsFromBuffer(buffer, length);
        attributes = parseDataStrings(strings);
    }

    private Map<String, String> parseDataStrings(List<String> strings) {
        Map<String, String> pairs = new HashMap<>();
        strings.stream().forEach(s -> {
            String[] parts = s.split("=");
            pairs.put(parts[0], parts[1]);
        });
        return pairs;
    }

    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    @Override
    public String toString() {
        return "TxtRecord{" +
                "name='" + name + '\'' +
                ", recordClass=" + recordClass +
                ", ttl=" + ttl +
                ", attributes=" + attributes +
                '}';
    }
}
