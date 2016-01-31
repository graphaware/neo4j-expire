package com.graphaware.neo4j.strategies;

import com.graphaware.neo4j.NodeDeleter;
import org.neo4j.graphdb.GraphDatabaseService;

public class ExpirationStrategyFactory {

    private final GraphDatabaseService database;
    private final NodeDeleter deleter;

    public ExpirationStrategyFactory(GraphDatabaseService database, NodeDeleter deleter) {
        this.database = database;
        this.deleter = deleter;
    }

    public ExpirationStrategy build(String strategyConfiguration) throws InvalidExpirationStrategyException {
        switch (strategyConfiguration) {
            case "manual":
                return new ManualExpirationStrategy(database, deleter);
            case "deleteOrphans":
                return new DeleteOrphanedRelationsStrategy(database, deleter);
            default:
                throw new InvalidExpirationStrategyException("The expiration strategy is not a recognised strategy.");
        }
    }

}
