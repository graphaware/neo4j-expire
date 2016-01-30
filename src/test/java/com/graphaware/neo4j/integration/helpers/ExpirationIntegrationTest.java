package com.graphaware.neo4j.integration.helpers;

import com.graphaware.test.integration.GraphAwareApiTest;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.tooling.GlobalGraphOperations;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.neo4j.helpers.collection.Iterables.*;

public abstract class ExpirationIntegrationTest extends GraphAwareApiTest {

    @Override
    protected String propertiesFile() {
        try {
            return new ClassPathResource("neo4j-expire-all.properties").getFile().getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected long getNodeCount() {
        try(Transaction tx = getDatabase().beginTx()) {
            return count(GlobalGraphOperations.at(getDatabase()).getAllNodes());
        }
    }
}
