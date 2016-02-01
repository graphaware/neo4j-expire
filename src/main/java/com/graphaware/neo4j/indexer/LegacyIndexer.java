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

import com.graphaware.neo4j.config.ExpirationConfiguration;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.index.lucene.QueryContext;
import org.neo4j.index.lucene.ValueContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class LegacyIndexer implements ExpirationIndexer {
    private static final Logger LOG = LoggerFactory.getLogger(LegacyIndexer.class);

    private GraphDatabaseService database;
    private ExpirationConfiguration configuration;

    @Autowired
    public LegacyIndexer(GraphDatabaseService database, ExpirationConfiguration configuration) {
        this.database = database;
        this.configuration = configuration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void indexNode(Node node) {
        String expirationProperty = configuration.getExpirationProperty();

        if (node.hasProperty(expirationProperty)) {
            Long timestamp;

            try {
                timestamp = Long.parseLong(node.getProperty(expirationProperty).toString(), 10);
            } catch (NumberFormatException e) {
                LOG.warn("Node with expiration property not indexed as the property is non-numeric: %s", node.toString());
                return;
            }

            database.index().forNodes(configuration.getExpirationIndex())
                    .add(node, expirationProperty, new ValueContext(timestamp).indexNumeric());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IndexHits<Node> nodesExpiringBefore(Long timestamp) {

        IndexHits<Node> expiringNodes;

        try (Transaction tx = database.beginTx()) {
            Index<Node> index = database.index().forNodes(configuration.getExpirationIndex());
            expiringNodes = index.query(QueryContext.numericRange(configuration.getExpirationProperty(), 0L, timestamp));
            tx.success();
        }

        return expiringNodes;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeNode(Node node) {
        try (Transaction tx = database.beginTx()) {
            Index<Node> index = database.index().forNodes(configuration.getExpirationIndex());

            index.remove(node, configuration.getExpirationProperty());

            tx.success();
        }
    }
}
