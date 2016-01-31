package com.graphaware.neo4j;

import com.graphaware.neo4j.indexer.ExpirationIndexer;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.IndexHits;

public class NodeDeleter {


    private final GraphDatabaseService database;
    private final ExpirationIndexer indexer;

    public NodeDeleter(GraphDatabaseService database, ExpirationIndexer indexer) {
        this.indexer = indexer;
        this.database = database;
    }

    public void deleteNodesIncludingAdjoiningEdgesExpiringBefore(long timestamp) {

        try (Transaction tx = database.beginTx()) {
            IndexHits<Node> expiringNodes = indexer.nodesExpiringBefore(timestamp);

            for (Node node : expiringNodes) {
                deleteRelationshipsOn(node);
                node.delete();
            }

            tx.success();
        }

    }

    private void deleteRelationshipsOn(Node node) {
        try (Transaction tx = database.beginTx()) {
            for (Relationship relationship : node.getRelationships()) {
                relationship.delete();
            }

            tx.success();
        }

    }

}
