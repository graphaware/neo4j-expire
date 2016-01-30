package com.graphaware.neo4j.integration;

import com.graphaware.neo4j.NodeDeleter;
import com.graphaware.neo4j.config.ExpirationConfiguration;
import com.graphaware.neo4j.indexer.LegacyIndexer;
import com.graphaware.neo4j.integration.helpers.ExpirationIntegrationTest;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.tooling.GlobalGraphOperations;

import static org.junit.Assert.assertEquals;

public class NodeDeleterTest extends ExpirationIntegrationTest {

    @Test
    public void nodesShouldBeDeletedIfTheyAreAfterTimestamp() {
        getDatabase().execute("CREATE (p:Person {name: 'Dave', _expire: 123})");
        getDatabase().execute("CREATE (p:Person {name: 'Dave', _expire: 234})");

        NodeDeleter deleter = new NodeDeleter(getDatabase(), new LegacyIndexer(getDatabase(), ExpirationConfiguration.defaultConfiguration()));

        deleter.deleteNodesExpiringBefore(200);

        assertEquals(1, getNodeCount());
    }

    @Test
    public void noNodesShouldBeDeletedIfTimestampIsEarlierThanAllNodes() {
        getDatabase().execute("CREATE (p:Person {name: 'Dave', _expire: 123})");
        getDatabase().execute("CREATE (p:Person {name: 'Dave', _expire: 234})");

        NodeDeleter deleter = new NodeDeleter(getDatabase(), new LegacyIndexer(getDatabase(), ExpirationConfiguration.defaultConfiguration()));

        deleter.deleteNodesExpiringBefore(50);

        assertEquals(2, getNodeCount());
    }


}
