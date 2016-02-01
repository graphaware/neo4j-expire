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
