package com.graphaware.neo4j.integration;

import com.graphaware.test.integration.GraphAwareApiTest;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IndexMaintenanceTest extends GraphAwareApiTest {

    @Override
    protected String propertiesFile() {
        try {
            return new ClassPathResource("neo4j-expire-all.properties").getFile().getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


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
