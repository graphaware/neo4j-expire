/*
 * Copyright (c) 2013-2016 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.neo4j.expire.indexer;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.IndexHits;

/**
 * A component responsible for handling the indexing of expiry dates on nodes and relationships.
 */
public interface ExpirationIndexer {

    /**
     * Add a given node with any relevant expiration/ttl property to the expiration index.
     * If there is no, or a non-numeric expiration/ttl property, do nothing.
     *
     * @param node to index.
     */
    void indexNode(Node node);

    /**
     * Add a given relationship with any relevant expiration/ttl property to the expiration index.
     * If there is no, or a non-numeric expiration/ttl property, do nothing.
     *
     * @param relationship to index.
     */
    void indexRelationship(Relationship relationship);

    /**
     * Finds all indexed nodes that expire before a particular time.
     *
     * @param timestamp The timestamp to query for, given as milliseconds since epoch.
     * @return Iterable of all nodes expiring before timestamp.
     */
    IndexHits<Node> nodesExpiringBefore(long timestamp);

    /**
     * Finds all indexed relationships that expire before a particular time.
     *
     * @param timestamp The timestamp to query for, given as milliseconds since epoch.
     * @return Iterable of all relationships expiring before timestamp.
     */
    IndexHits<Relationship> relationshipsExpiringBefore(long timestamp);

    /**
     * Removes node from expiration index. If node is not in the index, it does nothing.
     *
     * @param node Node to remove from index.
     */
    void removeNode(Node node);

    /**
     * Removes relationship from expiration index. If relationship is not in the index, it does nothing.
     *
     * @param relationship Relationship to remove from index.
     */
    void removeRelationship(Relationship relationship);
}
