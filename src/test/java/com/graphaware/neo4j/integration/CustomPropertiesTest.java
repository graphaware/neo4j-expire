package com.graphaware.neo4j.integration;

import com.graphaware.neo4j.integration.helpers.ExpirationIntegrationTest;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class CustomPropertiesTest extends ExpirationIntegrationTest {

    @Override
    protected String propertiesFile() {
        try {
            return new ClassPathResource("neo4j-expire-custom-properties.properties").getFile().getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void customPropertiesGetPickedUpBy() throws InterruptedException {
        getDatabase().execute(
                String.format("CREATE (p:Person {name: 'Dave', customProperty: 123})"));

        try(Transaction tx = getDatabase().beginTx()) {
            assertTrue(getDatabase().index().existsForNodes("customIndexName"));
            tx.success();
        }


    }
}
