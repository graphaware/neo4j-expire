package com.graphaware.neo4j.strategies;

import com.graphaware.neo4j.NodeDeleter;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.Date;

/**
 * {@link ExpirationStrategy} that will remove all nodes that have expired, and all relationships
 * that are attached to these nodes
 */
public class DeleteOrphanedRelationsStrategy extends ExpirationStrategy {

    public DeleteOrphanedRelationsStrategy(GraphDatabaseService databaseService, NodeDeleter nodeDeleter) {
        super(databaseService, nodeDeleter);
    }

    /**
     * {@inheritDoc}
     */
    public void expireNodes() {
        long millisecondsFromEpoch = new Date().getTime();

        deleter.deleteNodesIncludingAdjoiningEdgesExpiringBefore(millisecondsFromEpoch);
    }
}
