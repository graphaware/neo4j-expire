package com.graphaware.neo4j.integration;

import com.graphaware.neo4j.NodeDeleter;
import com.graphaware.neo4j.config.ExpirationConfiguration;
import com.graphaware.neo4j.indexer.LegacyIndexer;
import com.graphaware.neo4j.integration.helpers.ExpirationIntegrationTest;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class NodeDeleterTest extends ExpirationIntegrationTest {

    @Test
    public void nodesShouldBeDeletedIfTheyAreAfterTimestamp() {
        getDatabase().execute("CREATE (p:Person {name: 'Dave', _expire: 123})");
        getDatabase().execute("CREATE (p:Person {name: 'Dave', _expire: 234})");

        NodeDeleter deleter = new NodeDeleter(getDatabase(), new LegacyIndexer(getDatabase(), ExpirationConfiguration.defaultConfiguration()));

        deleter.deleteNodesIncludingAdjoiningEdgesExpiringBefore(200);

        assertThat(getNodeCount(), equalTo(1L));
    }

    @Test
    public void noNodesShouldBeDeletedIfTimestampIsEarlierThanAllNodes() {
        getDatabase().execute("CREATE (p:Person {name: 'Dave', _expire: 123})");
        getDatabase().execute("CREATE (p:Person {name: 'Dave', _expire: 234})");

        NodeDeleter deleter = new NodeDeleter(getDatabase(), new LegacyIndexer(getDatabase(), ExpirationConfiguration.defaultConfiguration()));

        deleter.deleteNodesIncludingAdjoiningEdgesExpiringBefore(50);

        assertThat(getNodeCount(), equalTo(2L));
    }


}
