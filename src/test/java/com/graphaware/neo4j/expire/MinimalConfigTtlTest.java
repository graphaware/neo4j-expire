package com.graphaware.neo4j.expire;

import com.graphaware.test.integration.GraphAwareApiTest;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static com.graphaware.test.unit.GraphUnit.assertEmpty;
import static com.graphaware.test.unit.GraphUnit.assertSameGraph;
import static com.graphaware.test.util.TestUtils.waitFor;

public class MinimalConfigTtlTest extends GraphAwareApiTest {

    @Override
    protected String propertiesFile() {
        try {
            return new ClassPathResource("neo4j-ttl-minimal.properties").getFile().getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void shouldExpireNodesAndRelationshipsWhenExpiryDateReached() {
        getDatabase().execute("CREATE (s1:State {name:'Cloudy', timeToLive:2000})-[:THEN {timeToLive:2000}]->(s2:State {name:'Windy', timeToLive:3000})");

        assertSameGraph(getDatabase(), "CREATE (s1:State {name:'Cloudy', timeToLive:2000})-[:THEN {timeToLive:2000}]->(s2:State {name:'Windy', timeToLive:3000})");

        waitFor(2100);

        assertSameGraph(getDatabase(), "CREATE (s2:State {name:'Windy', timeToLive:3000})");

        waitFor(1100);

        assertEmpty(getDatabase());
    }
}
