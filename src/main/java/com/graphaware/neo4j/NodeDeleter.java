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

package com.graphaware.neo4j;

import com.graphaware.neo4j.indexer.ExpirationIndexer;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.IndexHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates deletion of nodes when using the {@link com.graphaware.neo4j.indexer.LegacyIndexer}
 */
public class NodeDeleter {

    private static final Logger LOG = LoggerFactory.getLogger(NodeDeleter.class);

    private final GraphDatabaseService database;
    private final ExpirationIndexer indexer;

    public NodeDeleter(GraphDatabaseService database, ExpirationIndexer indexer) {
        this.indexer = indexer;
        this.database = database;
    }

    /**
     * Use the {@link com.graphaware.neo4j.indexer.LegacyIndexer} to determine nodes which have expired and remove them,
     * removing all edges it is adjacent to
     *
     * @param timestamp The timestamp to query for, given as milliseconds since Jan 1 1970
     */
    public void deleteNodesIncludingAdjoiningEdgesExpiringBefore(long timestamp) {

        try (Transaction tx = database.beginTx()) {
            IndexHits<Node> expiringNodes = indexer.nodesExpiringBefore(timestamp);

            for (Node node : expiringNodes) {
                deleteRelationshipsOn(node);
                node.delete();
                LOG.info("Node deleted: %s", node.toString());
            }

            tx.success();
        }

    }

    private void deleteRelationshipsOn(Node node) {
        try (Transaction tx = database.beginTx()) {
            for (Relationship relationship : node.getRelationships()) {
                relationship.delete();
                LOG.info("Relationship deleted: %s", relationship.toString());
            }

            tx.success();
        }

    }

}
