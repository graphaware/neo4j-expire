package com.graphaware.neo4j.strategies;

import com.graphaware.neo4j.NodeDeleter;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.Date;

public class DeleteOrphanedRelationsStrategy extends ExpirationStrategy {

    public DeleteOrphanedRelationsStrategy(GraphDatabaseService databaseService, NodeDeleter nodeDeleter) {
        super(databaseService, nodeDeleter);
    }

    public void expireNodes() {
        long millisecondsFromEpoch = new Date().getTime();

        deleter.deleteNodesIncludingAdjoiningEdgesExpiringBefore(millisecondsFromEpoch);
    }
}
