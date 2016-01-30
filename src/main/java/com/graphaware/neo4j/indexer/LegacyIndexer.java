package com.graphaware.neo4j.indexer;

import com.graphaware.neo4j.config.ExpirationConfiguration;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.index.lucene.QueryContext;
import org.neo4j.index.lucene.ValueContext;
import org.springframework.beans.factory.annotation.Autowired;

public class LegacyIndexer implements ExpirationIndexer {
    private GraphDatabaseService database;
    private ExpirationConfiguration configuration;

    @Autowired
    public LegacyIndexer(GraphDatabaseService database, ExpirationConfiguration configuration) {
        this.database = database;
        this.configuration = configuration;
    }

    @Override
    public void indexNode(Node node) {
        String expirationProperty = configuration.getExpirationProperty();

        if(node.hasProperty(expirationProperty)) {
            Long timestamp;

            try {
                timestamp = Long.parseLong(node.getProperty(expirationProperty).toString(), 10);
            } catch (NumberFormatException e ) {
                //TODO: Logging
                return;
            }

            database.index().forNodes(configuration.getExpirationIndex()).add(node, expirationProperty, new ValueContext(timestamp).indexNumeric());
        }
    }

    @Override
    public IndexHits<Node> nodesExpiringBefore(Long timestamp) {

        try (Transaction tx = database.beginTx()) {
            Index<Node> index = database.index().forNodes(configuration.getExpirationIndex());
            IndexHits<Node> expiringNodes = index.query(QueryContext.numericRange(configuration.getExpirationProperty(), 0L, timestamp));
            return expiringNodes;
        }

    }

    @Override
    public void deleteNode(Node node) {
        try(Transaction tx = database.beginTx()) {
            Index<Node> index = database.index().forNodes(configuration.getExpirationIndex());
            index.remove(node, configuration.getExpirationProperty());
        }
    }
}
