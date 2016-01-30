package com.graphaware.neo4j.indexer;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.IndexHits;

public interface ExpirationIndexer {

    public void indexNode(Node node);

    public IndexHits<Node> nodesExpiringBefore(Long timestamp);
}
