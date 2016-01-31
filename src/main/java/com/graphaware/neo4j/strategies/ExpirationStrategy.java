package com.graphaware.neo4j.strategies;

import com.graphaware.neo4j.NodeDeleter;
import org.neo4j.graphdb.GraphDatabaseService;

public abstract class ExpirationStrategy {
    protected final GraphDatabaseService database;
    protected final NodeDeleter deleter;

    protected ExpirationStrategy(GraphDatabaseService databaseService, NodeDeleter deleter) {
        this.database = databaseService;
        this.deleter = deleter;
    }

    public abstract void expireNodes();
}
