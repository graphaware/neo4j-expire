package com.graphaware.neo4j.integration;

import com.graphaware.neo4j.integration.helpers.ExpirationIntegrationTest;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class EndToEndNodeDeletion extends ExpirationIntegrationTest {

    private static final long ONE_SECOND = 1000L;

    @Override
    protected String propertiesFile() {
        try {
            return new ClassPathResource("neo4j-expire-delete-orphans.properties").getFile().getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void nodeGetsDeletedAfterExpirationTime() throws InterruptedException {
        Long inOneSecond = new Date().getTime(); //+ ONE_SECOND;
        getDatabase().execute(
                String.format("CREATE (p:Person {name: 'Dave', _expire: %d})", inOneSecond));

        Thread.sleep(3000);
        assertEquals(0, getNodeCount());
    }
}
