package com.graphaware.neo4j.expire;

import com.graphaware.test.integration.GraphAwareApiTest;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static com.graphaware.test.unit.GraphUnit.assertEmpty;
import static com.graphaware.test.unit.GraphUnit.assertSameGraph;
import static com.graphaware.test.util.TestUtils.waitFor;

public class FullDefaultConfigExpiryTest extends GraphAwareApiTest {

    private static final long SECOND = 1_000;

    @Override
    protected String propertiesFile() {
        try {
            return new ClassPathResource("neo4j-expire-full-default.properties").getFile().getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void shouldExpireNodesAndRelationshipsWhenExpiryDateReached() {
        long now = System.currentTimeMillis();
        long twoSecondsFromNow = now + 2 * SECOND;
        long threeSecondsFromNow = now + 3 * SECOND;

        getDatabase().execute("CREATE (s1:State {name:'Cloudy', expire:" + twoSecondsFromNow + "})-[:THEN {expire:" + twoSecondsFromNow + "}]->(s2:State {name:'Windy', expire:" + threeSecondsFromNow + "})");

        assertSameGraph(getDatabase(), "CREATE (s1:State {name:'Cloudy', expire:" + twoSecondsFromNow + "})-[:THEN {expire:" + twoSecondsFromNow + "}]->(s2:State {name:'Windy', expire:" + threeSecondsFromNow + "})");

        waitFor(2100 - (System.currentTimeMillis() - now));

        assertSameGraph(getDatabase(), "CREATE (s2:State {name:'Windy', expire:" + threeSecondsFromNow + "})");

        waitFor(3100 - (System.currentTimeMillis() - now));

        assertEmpty(getDatabase());
    }
}
