package com.graphaware.neo4j.integration;

import com.graphaware.neo4j.config.ExpirationConfiguration;
import com.graphaware.neo4j.indexer.LegacyIndexer;
import com.graphaware.neo4j.integration.helpers.ExpirationIntegrationTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IndexReaderTest extends ExpirationIntegrationTest {

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
