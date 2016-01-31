package com.graphaware.neo4j.integration.helpers;

import com.graphaware.test.integration.GraphAwareApiTest;
import org.neo4j.graphdb.Transaction;
import org.neo4j.index.lucene.QueryContext;
import org.neo4j.tooling.GlobalGraphOperations;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.neo4j.helpers.collection.Iterables.*;

public abstract class ExpirationIntegrationTest extends GraphAwareApiTest {


    @Override
    protected String propertiesFile() {
        try {
            return new ClassPathResource("neo4j-expire-manual.properties").getFile().getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected long getNodeCount() {
        try(Transaction tx = getDatabase().beginTx()) {
            return count(GlobalGraphOperations.at(getDatabase()).getAllNodes());
        }
    }

    protected long countNodesExpiringBefore1000() {
        if(!getDatabase().index().existsForNodes("expirationIndex")) {
            return 0L;
        }
        return getDatabase().index().forNodes("expirationIndex").query(QueryContext.numericRange("_expire", 0L, 1000L)).size();
    }
}
