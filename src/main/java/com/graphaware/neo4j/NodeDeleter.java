package com.graphaware.neo4j;

import com.graphaware.neo4j.indexer.ExpirationIndexer;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.IndexHits;

public class NodeDeleter {


    private final GraphDatabaseService database;
    private final ExpirationIndexer indexer;

    public NodeDeleter(GraphDatabaseService database, ExpirationIndexer indexer) {
        this.indexer = indexer;
        this.database = database;
    }

    public void deleteNodesExpiringBefore(long timestamp) {
        IndexHits<Node> expiringNodes = indexer.nodesExpiringBefore(timestamp);

        try (Transaction tx = database.beginTx()) {
            for (Node node : expiringNodes) {
                node.delete();
            }
            tx.success();
        }

    }

}
