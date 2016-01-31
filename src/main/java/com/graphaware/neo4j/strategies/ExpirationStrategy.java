package com.graphaware.neo4j.strategies;

import com.graphaware.neo4j.NodeDeleter;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Abstract Strategy to determine how nodes are expired in an automated manner, used by {@link com.graphaware.neo4j.ExpirationIndexModule}
 */
public abstract class ExpirationStrategy {
    protected final GraphDatabaseService database;
    protected final NodeDeleter deleter;

    protected ExpirationStrategy(GraphDatabaseService databaseService, NodeDeleter deleter) {
        this.database = databaseService;
        this.deleter = deleter;
    }

    /**
     * Method called by {@link com.graphaware.neo4j.ExpirationIndexModule} at regular intervals
     * Intended to work to expire any nodes that have expired.
     */
    public abstract void expireNodes();
}
