package com.graphaware.neo4j.indexer;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.IndexHits;

public interface ExpirationIndexer {

    void indexNode(Node node);

    IndexHits<Node> nodesExpiringBefore(Long timestamp);

    void deleteNode(Node node);
}
