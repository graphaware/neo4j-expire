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

import com.graphaware.common.log.LoggerFactory;
import com.graphaware.neo4j.expire.config.ExpirationConfiguration;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.index.lucene.QueryContext;
import org.neo4j.index.lucene.ValueContext;
import org.neo4j.logging.Log;

import static com.graphaware.common.util.PropertyContainerUtils.*;

/**
 * {@link ExpirationIndexer} that uses the legacy index of Neo4j.
 */
public class LegacyExpirationIndexer implements ExpirationIndexer {
    private static final Log LOG = LoggerFactory.getLogger(LegacyExpirationIndexer.class);
    private static final String EXPIRE = "_expire";


    private GraphDatabaseService database;
    private ExpirationConfiguration configuration;

    public LegacyExpirationIndexer(GraphDatabaseService database, ExpirationConfiguration configuration) {
        this.database = database;
        this.configuration = configuration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void indexNode(Node node) {
        Long expiryDate = getExpirationDate(node);

        if (expiryDate != null) {
            database.index().forNodes(configuration.getNodeExpirationIndex()).add(node, EXPIRE, new ValueContext(expiryDate).indexNumeric());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void indexRelationship(Relationship relationship) {
        Long expiryDate = getExpirationDate(relationship);

        if (expiryDate != null) {
            database.index().forRelationships(configuration.getRelationshipExpirationIndex()).add(relationship, EXPIRE, new ValueContext(expiryDate).indexNumeric());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IndexHits<Node> nodesExpiringBefore(long timestamp) {
        if (configuration.getNodeExpirationIndex() == null) {
            return null;
        }

        IndexHits<Node> result;

        try (Transaction tx = database.beginTx()) {
            Index<Node> index = database.index().forNodes(configuration.getNodeExpirationIndex());
            result = index.query(QueryContext.numericRange(EXPIRE, 0L, timestamp));
            tx.success();
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IndexHits<Relationship> relationshipsExpiringBefore(long timestamp) {
        if (configuration.getRelationshipExpirationIndex() == null) {
            return null;
        }

        IndexHits<Relationship> result;

        try (Transaction tx = database.beginTx()) {
            Index<Relationship> index = database.index().forRelationships(configuration.getRelationshipExpirationIndex());
            result = index.query(QueryContext.numericRange(EXPIRE, 0L, timestamp));
            tx.success();
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeNode(Node node) {
        try (Transaction tx = database.beginTx()) {
            Index<Node> index = database.index().forNodes(configuration.getNodeExpirationIndex());

            index.remove(node, EXPIRE);

            tx.success();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeRelationship(Relationship relationship) {
        try (Transaction tx = database.beginTx()) {
            Index<Relationship> index = database.index().forRelationships(configuration.getRelationshipExpirationIndex());

            index.remove(relationship, EXPIRE);

            tx.success();
        }
    }

    private Long getExpirationDate(Node node) {
        return getExpirationDate(node, configuration.getNodeExpirationProperty(), configuration.getNodeTtlProperty());
    }

    private Long getExpirationDate(Relationship relationship) {
        return getExpirationDate(relationship, configuration.getRelationshipExpirationProperty(), configuration.getRelationshipTtlProperty());
    }

    private Long getExpirationDate(PropertyContainer pc, String expirationProperty, String ttlProperty) {
        if (!hasExpirationProperty(pc, expirationProperty, ttlProperty)) {
            return null;
        }

        Long result = null;

        if (pc.hasProperty(expirationProperty)) {
            try {
                result = Long.parseLong(pc.getProperty(expirationProperty).toString());
            } catch (NumberFormatException e) {
                LOG.warn("%s expiration property is non-numeric: %s", id(pc), pc.getProperty(expirationProperty));
            }
        }

        if (pc.hasProperty(ttlProperty)) {
            try {
                long newResult = System.currentTimeMillis() + Long.parseLong(pc.getProperty(ttlProperty).toString());

                if (result != null) {
                    LOG.warn("%s has both expiry date and a ttl.", id(pc));

                    if (newResult > result) {
                        LOG.warn("Using ttl as it is later.");
                        result = newResult;
                    } else {
                        LOG.warn("Using expiry date as it is later.");
                    }
                }
                else {
                    result = newResult;
                }
            } catch (NumberFormatException e) {
                LOG.warn("%s ttl property is non-numeric: %s", id(pc), pc.getProperty(ttlProperty));
            }
        }

        return result;
    }

    private boolean hasExpirationProperty(PropertyContainer pc, String expirationProperty, String ttlProperty) {
        return (pc.hasProperty(expirationProperty) || pc.hasProperty(ttlProperty));
    }
}
