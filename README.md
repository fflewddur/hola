Hola
====

Hola is a minimalist Java implementation of Multicast DNS Service Discovery (mDNS-SD). The purpose of Hola is to give Java developers a dead-simple API for finding Zeroconf-enabled services on a local network. It follows RFCs [6762](https://tools.ietf.org/html/rfc6762) and [6763](https://tools.ietf.org/html/rfc6763) and is compatible with Apple's [Bonjour](https://developer.apple.com/bonjour/) mDNS-SD implementation.

# Features

Hola is a work-in-progress. The following features are currently supported:

 - Browse (synchronously) for instances of services on a local network
 - Retrieve information about discovered services, including network addresses, ports, and user-friendly names
 - Supports both IPv4 and IPv6 networks

# API Example

To search for services, create a `Query` specifying the type of service you're looking for and the domain to search. You can execute a blocking search with the `runOnce()` method; this will return a list of `Instance` objects representing the discovered instances. As an example, the following code  will search for TiVo devices on the user's local network:

    public class TivoFinder {
        public static void main(String[] args) {
            try {
                Service service = Service.fromName("_tivo-mindrpc._tcp");
                Query query = Query.createFor(service, Domain.LOCAL);
                List<Instance> instances = query.runOnce();
                instances.stream().forEach(System.out::println);
            } catch (UnknownHostException e) {
                System.err.println("Unknown host: " + e);
            } catch (IOException e) {
                System.err.println("IO error: " + e);
            }
        }
    }

Each `Instance` will have a user-visible name, a list of IP addresses, a port number, and a set of attributes:

    String userVisibleName = instance.getName();
    List<InetAddress> addresses = instance.getAddresses();
    int port = instance.getPort();
    if (instance.hasAttribute("platform")) {
        String platform = instance.lookupAttribute("platform");
    }

An asynchronous `run()` method is planned for performing a continuous service discovery operation, but this feature is not yet implemented.

# Requirements

Hola requires Java 8 or higher. It handles logging via SLF4J, so the slf4j-api.jar must also be in your Hola-enabled project's class path.