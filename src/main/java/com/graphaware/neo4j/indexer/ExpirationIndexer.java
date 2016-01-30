package com.graphaware.neo4j.indexer;

import org.neo4j.graphdb.Node;

public interface ExpirationIndexer {

    public void indexNode(Node node);
}
