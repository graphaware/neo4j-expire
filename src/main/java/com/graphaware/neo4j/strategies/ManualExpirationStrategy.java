package com.graphaware.neo4j.strategies;

import com.graphaware.neo4j.NodeDeleter;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * {@link ExpirationStrategy} designed to do no expirations automatically allowing manual interaction with legacy indexes
 */
public class ManualExpirationStrategy extends ExpirationStrategy {

    public ManualExpirationStrategy(GraphDatabaseService databaseService, NodeDeleter nodeDeleter) {
        super(databaseService, nodeDeleter);
    }

    /**
     * {@inheritDoc}
     */
    public void expireNodes() {
        //Nodes are only expired manually in this strategy, so work is not done in the timer-driven module
    }

}
