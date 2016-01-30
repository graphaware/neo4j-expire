package com.graphaware.neo4j.indexer;

import com.graphaware.neo4j.config.ExpirationConfiguration;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
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
            database.index().forNodes(configuration.getExpirationIndex()).add(node, expirationProperty, node.getProperties(expirationProperty));
        }
    }
}
