package com.graphaware.neo4j.indexer;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.IndexHits;

public interface ExpirationIndexer {

    /**
     * Add a given node with any relevant expiration property to the expiration index.
     * If there is no, or a non-numeric expiration policy, it does nothing.
     * @param node Node to index
     */
    void indexNode(Node node);

    /**
     * Finds all indexed nodes that expire before a particular time
     * @param timestamp The timestamp to query for, given as milliseconds since Jan 1 1970
     * @return Iterable of all nodes expiring before timestamp
     */
    IndexHits<Node> nodesExpiringBefore(Long timestamp);

    /**
     * Removes node from expiration index. If node is not in the index, it does nothing.
     * @param node Node to remove from index
     */
    void removeNode(Node node);
}
