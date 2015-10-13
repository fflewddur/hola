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

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Domain {
    private final String name;
    private final List<String> labels;

    public static final Domain LOCAL = new Domain("local.");

    private static final Pattern DOMAIN_PATTERN = Pattern.compile("((.*)_(tcp|udp)\\.)?(.*?)\\.?");

    public static Domain fromName(String name) {
        Matcher matcher = DOMAIN_PATTERN.matcher(name);
        if (matcher.matches()) {
            return new Domain(matcher.group(4));
        } else {
            throw new IllegalArgumentException("Name does not match domain syntax");
        }
    }

    private Domain(String name) {
        this.name = name;
        labels = Arrays.asList(name.split("\\."));
    }

    public String getName() {
        return name;
    }

    public List<String> getLabels() {
        return labels;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Domain domain = (Domain) o;

        return name.equals(domain.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "Domain{" +
                "name='" + name + '\'' +
                ", labels=" + labels +
                '}';
    }
}
