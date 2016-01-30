package com.graphaware.neo4j.integration;

import com.graphaware.neo4j.integration.helpers.ExpirationIntegrationTest;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IndexMaintenanceTest extends ExpirationIntegrationTest {


    @Test
    public void nodesWithNumericExpirationPropertiesGetIndexed() throws InterruptedException {
        getDatabase().execute("CREATE (p:Person {name: 'Dave', _expire: 123})");

        try (Transaction tx = getDatabase().beginTx()) {
            assertTrue(getDatabase().index().existsForNodes("expirationIndex"));
        }
    }

    @Test
    public void nodesWithNonNumericExpirationPropertiesGetIndexed() throws InterruptedException {
        getDatabase().execute("CREATE (p:Person {name: 'Dave', _expire: 'asd'})");

        try (Transaction tx = getDatabase().beginTx()) {
            assertFalse(getDatabase().index().existsForNodes("expirationIndex"));
        }
    }

    @Test
    public void nodesWithNoExpirationPropertiesDoNotGetIndexed() throws InterruptedException {
        getDatabase().execute("CREATE (p:Person {name: 'Dave'})");

        try (Transaction tx = getDatabase().beginTx()) {
            assertFalse(getDatabase().index().existsForNodes("expirationIndex"));
        }
    }

}
