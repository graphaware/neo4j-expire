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

    /**
     * Simple factory method to return the requested strategy from the string config
     *
     * @param strategyConfiguration Configuration read from neo4j.properties and passed in by {@link com.graphaware.neo4j.ExpirationBootstrapper}
     * @return the requested expiration strategy
     * @throws InvalidExpirationStrategyException when the properties do not match any known strategy
     */
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
