package com.graphaware.neo4j.integration;

import com.graphaware.neo4j.config.ExpirationConfiguration;
import com.graphaware.neo4j.indexer.LegacyIndexer;
import com.graphaware.test.integration.GraphAwareApiTest;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IndexReaderTest extends GraphAwareApiTest {

    @Override
    protected String propertiesFile() {
        try {
            return new ClassPathResource("neo4j-expire-all.properties").getFile().getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void canFindNodesExpiringBeforeATimeStamp() throws InterruptedException {
        getDatabase().execute("CREATE (p:Person {name: 'Dave', _expire: 123})");
        getDatabase().execute("CREATE (p:Person {name: 'Bill', _expire: 234})");

        LegacyIndexer indexer = new LegacyIndexer(getDatabase(), ExpirationConfiguration.defaultConfiguration());

        assertEquals(1, indexer.nodesExpiringBefore(200L).size());
    }

    @Test
    public void canNoticeWhenNoNodesAreToExpire() throws InterruptedException {
        getDatabase().execute("CREATE (p:Person {name: 'Dave', _expire: 123})");
        getDatabase().execute("CREATE (p:Person {name: 'Bill', _expire: 234})");

        LegacyIndexer indexer = new LegacyIndexer(getDatabase(), ExpirationConfiguration.defaultConfiguration());

        assertEquals(0, indexer.nodesExpiringBefore(50L).size());
    }

}
