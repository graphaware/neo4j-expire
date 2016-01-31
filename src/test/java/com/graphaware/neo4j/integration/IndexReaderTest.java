package com.graphaware.neo4j.integration;

import com.graphaware.neo4j.config.ExpirationConfiguration;
import com.graphaware.neo4j.indexer.LegacyIndexer;
import com.graphaware.neo4j.integration.helpers.ExpirationIntegrationTest;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class IndexReaderTest extends ExpirationIntegrationTest {

    @Test
    public void canFindNodesExpiringBeforeATimeStamp() throws InterruptedException {
        getDatabase().execute("CREATE (p:Person {name: 'Dave', _expire: 123})");
        getDatabase().execute("CREATE (p:Person {name: 'Bill', _expire: 234})");

        LegacyIndexer indexer = new LegacyIndexer(getDatabase(), ExpirationConfiguration.defaultConfiguration());

        assertThat(indexer.nodesExpiringBefore(200L).size(), equalTo(1));
    }

    @Test
    public void canNoticeWhenNoNodesAreToExpire() throws InterruptedException {
        getDatabase().execute("CREATE (p:Person {name: 'Dave', _expire: 123})");
        getDatabase().execute("CREATE (p:Person {name: 'Bill', _expire: 234})");

        LegacyIndexer indexer = new LegacyIndexer(getDatabase(), ExpirationConfiguration.defaultConfiguration());

        assertThat(indexer.nodesExpiringBefore(50L).size(), equalTo(0));
    }



}
