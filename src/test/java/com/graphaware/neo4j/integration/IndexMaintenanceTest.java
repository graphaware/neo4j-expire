package com.graphaware.neo4j.integration;

import com.graphaware.neo4j.integration.helpers.ExpirationIntegrationTest;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IndexMaintenanceTest extends ExpirationIntegrationTest {


    @Test
    public void nodesWithNumericExpirationPropertiesGetIndexed() throws InterruptedException {
        getDatabase().execute("CREATE (p:Person {name: 'Dave', _expire: 123})");

        try (Transaction tx = getDatabase().beginTx()) {
            assertThat(countNodesExpiringBefore1000(), equalTo(1L));
        }
    }

    @Test
    public void nodesWithNonNumericExpirationPropertiesGetIndexed() throws InterruptedException {
        getDatabase().execute("CREATE (p:Person {name: 'Dave', _expire: 'asd'})");

        try (Transaction tx = getDatabase().beginTx()) {
            assertThat(countNodesExpiringBefore1000(), equalTo(0L));
        }
    }

    @Test
    public void nodesWithNoExpirationPropertiesDoNotGetIndexed() throws InterruptedException {
        getDatabase().execute("CREATE (p:Person {name: 'Dave'})");

        try (Transaction tx = getDatabase().beginTx()) {
            assertThat(countNodesExpiringBefore1000(), equalTo(0L));
        }
    }

    @Test
    public void nodesAreRemovedFromIndexIfTheyLoseTheirExpirationProperty() {
        getDatabase().execute("CREATE (p:Person {name: 'Dave', _expire: 123})");
        getDatabase().execute("MATCH (p:Person {name: 'Dave', _expire: 123}) SET p._expire = NULL RETURN p");

        try(Transaction tx = getDatabase().beginTx()) {
            assertThat(countNodesExpiringBefore1000(), equalTo(0L));
        }
    }

    @Test
    public void nodesAreUpdatedFromIndexIfTheyChangeTheirExpirationProperty() {
        getDatabase().execute("CREATE (p:Person {name: 'Dave', _expire: 123})");
        getDatabase().execute("CREATE (p:Person {name: 'Bill', _expire: 234})");

        getDatabase().execute("MATCH (p:Person {name: 'Dave', _expire: 123}) SET p._expire = 100000 RETURN p");

        try(Transaction tx = getDatabase().beginTx()) {
            assertThat(countNodesExpiringBefore1000(), equalTo(1L));
        }
    }

}
