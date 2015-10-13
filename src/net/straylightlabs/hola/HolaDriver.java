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

package net.straylightlabs.hola;

import net.straylightlabs.hola.dns.Domain;
import net.straylightlabs.hola.sd.Instance;
import net.straylightlabs.hola.sd.Query;
import net.straylightlabs.hola.sd.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

/**
 * A minimal implementation of mDNS-SD, as described in RFCs 6762 & 6763.
 */

public class HolaDriver {
    final static Logger logger = LoggerFactory.getLogger(HolaDriver.class);

    public static void main(String[] args) {
        try {
            Service service = Service.fromName("_tivo-mindrpc._tcp");
//            Service service = Service.fromName("_appletv-v2._tcp");
//            Service service = Service.fromName("_airport._tcp");
            Query query = Query.createFor(service, Domain.LOCAL);
            List<Instance> instances = query.runOnce();
            instances.stream().forEach(System.out::println);
        } catch (UnknownHostException e) {
            logger.error("Unknown host: ", e);
        } catch (IOException e) {
            logger.error("IO error: ", e);
        }
    }
}
