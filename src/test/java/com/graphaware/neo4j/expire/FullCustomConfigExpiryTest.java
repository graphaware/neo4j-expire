package com.graphaware.neo4j.expire;

import com.graphaware.test.integration.GraphAwareApiTest;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static com.graphaware.test.unit.GraphUnit.assertEmpty;
import static com.graphaware.test.unit.GraphUnit.assertSameGraph;
import static com.graphaware.test.util.TestUtils.waitFor;
import static org.junit.Assert.assertTrue;

public class FullCustomConfigExpiryTest extends GraphAwareApiTest {

    private static final long SECOND = 1_000;

    @Override
    protected String propertiesFile() {
        try {
            return new ClassPathResource("neo4j-expire-full-custom.properties").getFile().getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void shouldExpireNodesAndRelationshipsWhenExpiryDateReached() {
        long now = System.currentTimeMillis();
        long twoSecondsFromNow = now + 2 * SECOND;
        long threeSecondsFromNow = now + 3 * SECOND;

        getDatabase().execute("CREATE (s1:State {name:'Cloudy', _expire:" + twoSecondsFromNow + "})-[:THEN]->(s2:NotAState {name:'Windy', _expire:" + threeSecondsFromNow + "})");

        assertSameGraph(getDatabase(), "CREATE (s1:State {name:'Cloudy', _expire:" + twoSecondsFromNow + "})-[:THEN]->(s2:NotAState {name:'Windy', _expire:" + threeSecondsFromNow + "})");

        waitFor(2100 - (System.currentTimeMillis() - now));

        //force deleted relationship
        assertSameGraph(getDatabase(), "CREATE (s2:NotAState {name:'Windy', _expire:" + threeSecondsFromNow + "})");

        waitFor(3100 - (System.currentTimeMillis() - now));

        //not deleted because of inclusion policies
        assertSameGraph(getDatabase(), "CREATE (s2:NotAState {name:'Windy', _expire:" + threeSecondsFromNow + "})");

        try (Transaction tx = getDatabase().beginTx()) {
            assertTrue(getDatabase().index().existsForNodes("nodeExp"));
            assertTrue(getDatabase().index().existsForRelationships("relExp"));
            tx.success();
        }
    }
}
