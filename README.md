GraphAware Neo4j Expire
=======================

[![Build Status](https://travis-ci.org/graphaware/neo4j-expire.png)](https://travis-ci.org/graphaware/neo4j-expire) | <a href="http://graphaware.com/products/" target="_blank">Products</a> | <a href="http://products.graphaware.com" target="_blank">Downloads</a> | <a href="http://graphaware.com/site/expire/latest/apidocs/" target="_blank">Javadoc</a> | Latest Release: 3.3.2.52.4

GraphAware Expire is a simple library that automatically deletes nodes and relationships from the database when they've
reached their expiration date or time-to-live (TTL).

Getting the Software
--------------------

### Server Mode

When using Neo4j in the <a href="http://docs.neo4j.org/chunked/stable/server-installation.html" target="_blank">standalone server</a> mode,
you will need the <a href="https://github.com/graphaware/neo4j-framework" target="_blank">GraphAware Neo4j Framework</a> and GraphAware Neo4j Expire .jar files (both of which you can <a href="http://products.graphaware.com/" target="_blank">download here</a>) dropped
into the `plugins` directory of your Neo4j installation. After changing a few lines of config (read on) and restarting Neo4j, the module will do its magic.

### Embedded Mode / Java Development

Java developers that use Neo4j in <a href="http://docs.neo4j.org/chunked/stable/tutorials-java-embedded.html" target="_blank">embedded mode</a>
and those developing Neo4j <a href="http://docs.neo4j.org/chunked/stable/server-plugins.html" target="_blank">server plugins</a>,
<a href="http://docs.neo4j.org/chunked/stable/server-unmanaged-extensions.html" target="_blank">unmanaged extensions</a>,
GraphAware Runtime Modules, or Spring MVC Controllers can include the Expire Module as a dependency for their Java project.

#### Releases

Releases are synced to <a href="http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22expire%22" target="_blank">Maven Central repository</a>. When using Maven for dependency management, include the following dependency in your pom.xml.

    <dependencies>
        ...
        <dependency>
            <groupId>com.graphaware.neo4j</groupId>
            <artifactId>expire</artifactId>
            <version>3.3.2.52.4</version>
        </dependency>
        ...
    </dependencies>

#### Snapshots

To use the latest development version, just clone this repository, run `mvn clean install` and change the version in the
dependency above to 3.3.2.52.5-SNAPSHOT.

#### Note on Versioning Scheme

The version number has two parts. The first four numbers indicate compatibility with Neo4j GraphAware Framework.
 The last number is the version of the Expire library. For example, version 2.3.3.37.1 is version 1 of the Expire library
 compatible with GraphAware Neo4j Framework 2.3.3.37.

Setup and Configuration
--------------------

### Server Mode

First, please make sure that the framework is configured by adding `dbms.thirdparty_jaxrs_classes=com.graphaware.server=/graphaware` to `conf/neo4j.conf`,
as described <a href="https://github.com/graphaware/neo4j-framework#server-mode" target="_blank">here</a>.

And add this configuration to register the Expire module:

```
com.graphaware.runtime.enabled=true

#EM becomes the module ID (you will need to use this ID in other config below):
com.graphaware.module.EM.1=com.graphaware.neo4j.expire.ExpirationModuleBootstrapper

#If you want to delete nodes at a certain time, configure the node property (in this case "expire")
#that holds the expiration time in ms since epoch:
com.graphaware.module.EM.nodeExpirationProperty=expire

#Alternatively, if you want to delete nodes after some time has elapsed since they have been created,
#configure the node property (in this case "ttl") that holds the TTL in ms:
com.graphaware.module.EM.nodeTtlProperty=ttl

#If you want to delete relationships at a certain time, configure the relationships property (in this case "expire")
#that holds the expiration time in ms since epoch:
com.graphaware.module.EM.relationshipExpirationProperty=expire

#Alternatively, if you want to delete relationships after some time has elapsed since they have been created,
#configure the relationships property (in this case "ttl") that holds the TTL in ms:
com.graphaware.module.EM.relationshipTtlProperty=ttl

#If you want to delete expired nodes despite that fact they still have relationships, set the strategy to "force".
# This setting defaults to "orphan", which will only delete expired nodes with no relationships:
com.graphaware.module.EM.nodeExpirationStrategy=force

#By default, all created/updated nodes and relationships are checked for the presence of expire/ttl property.
#As with most GraphAware Modules, nodes and relationships this module applies to can be limited by the use of SPeL, e.g.:
com.graphaware.module.EM.node=hasLabel('NodeThatExpiresAtSomePoint')
com.graphaware.module.EM.relationship=isType('TEMPORARY_RELATIONSHIP')

#Optionally, configure the maximum number of nodes/relationships deleted in one transaction. Defaults to 1000.
com.graphaware.module.EM.maxExpirations=5000

```

### Embedded Mode / Java Development

To use the Expire module programmatically, register the module like this

```java
 GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(database);  //where database is an instance of GraphDatabaseService
 ExpirationModule module = new ExpirationModule("EXP", database, ExpirationConfiguration.defaultConfiguration().withNodeTtlProperty("ttl").withRelationshipTtlProperty("ttl"));
 runtime.registerModule(module);
 runtime.start();
```

Alternatively:
```java
 GraphDatabaseService database = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(pathToDb)
    .loadPropertiesFromFile(this.getClass().getClassLoader().getResource("neo4j.conf").getPath())
    .newGraphDatabase();

 //make sure neo4j.properties contain the lines mentioned in previous section
```

Using GraphAware Expire
-----------------------

Apart from the configuration described above, the GraphAware Expire module requires nothing else to function. It will
delete nodes and relationships when they've reached their expiration date or TTL. In case both expiration date and TTL are
set, the module takes into account whichever one is later. Please note a few more facts of interest:

* by default, nodes are only deleted if they have no relationship (i.e. all relationships have expired or have been manually deleted), unless the node expiration strategy is set to "force".
* when ttl property gets updated, the time-to-live is counted from the moment the node has been updated
* one of the following must be configured, otherwise it does not make sense to use the module: `nodeExpirationProperty`, `nodeTtlProperty`, `relationshipExpirationProperty`, `relationshipTtlProperty`.

Advanced Config
---------------

Nodes and relationships, along with their expiration dates, are stored in Neo4j's <a href="http://neo4j.com/docs/stable/indexing.html" target="_blank">legacy index</a>, completely transparently to the user.
A GraphAware Framework <a href="https://github.com/graphaware/neo4j-framework/tree/master/runtime#building-a-timer-driven-graphaware-runtime-module" target="_blank">Timer-Driven Runtime Module</a> checks for expired nodes and relationships every time it is asked to
perform work, and deletes the ones that are found.

Please note that the default setting for the Timer-Driven Runtime Module is and "adaptive" strategy that it slows down
background processing when the database is busy. By default, the maximum delay between invocations is 5 seconds.
If you want a shorter and/or more predictable time between a node/relationship reaching its expiration date and actually
being deleted, you can change this strategy. For example, if you wanted to check for expired elements every 100ms consistently,
you could add the following lines to neo4j.properties:

```
com.graphaware.runtime.timing.strategy=fixed
com.graphaware.runtime.timing.initialDelay=100
com.graphaware.runtime.timing.delay=100
```

License
-------

Copyright (c) 2016 GraphAware

GraphAware is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program.
If not, see <http://www.gnu.org/licenses/>.
