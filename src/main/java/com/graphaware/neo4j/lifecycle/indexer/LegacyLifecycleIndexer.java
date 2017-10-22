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

package com.graphaware.neo4j.lifecycle.indexer;

import static com.graphaware.common.util.PropertyContainerUtils.id;
import static com.graphaware.neo4j.lifecycle.LifecycleEvent.EXPIRY;

import com.graphaware.common.log.LoggerFactory;
import com.graphaware.neo4j.lifecycle.LifecycleEvent;
import com.graphaware.neo4j.lifecycle.config.LifecycleConfiguration;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.index.lucene.QueryContext;
import org.neo4j.index.lucene.ValueContext;
import org.neo4j.logging.Log;

public class LegacyLifecycleIndexer implements LifecycleIndexer {

	private static final Log LOG = LoggerFactory.getLogger(LegacyLifecycleIndexer.class);


	private GraphDatabaseService database;
	private LifecycleConfiguration config;

	public LegacyLifecycleIndexer(GraphDatabaseService database, LifecycleConfiguration config) {
		this.database = database;
		this.config = config;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void indexNode(LifecycleEvent event, Node node) {
		//TODO: Replace line below with polymorphism
		Long effectiveDate = event == EXPIRY ? getExpirationDate(node) : getRevivalDate(node);

		if (effectiveDate != null) {
			database.index().forNodes(config.nodeIndexFor(event))
					.add(node, event.name(), new ValueContext(effectiveDate).indexNumeric());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void indexRelationship(LifecycleEvent event, Relationship relationship) {
		//TODO: Replace line below with polymorphism
		Long effectiveDate = event == EXPIRY ? getExpirationDate(relationship) : getRevivalDate(relationship);

		if (effectiveDate != null) {
			database.index().forRelationships(config.relationshipIndexFor(event))
					.add(relationship, event.name(), new ValueContext(effectiveDate).indexNumeric());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndexHits<Node> nodesEligibleFor(LifecycleEvent event, long timestamp) {
		String indexName = config.nodeIndexFor(event);
		if (indexName == null) {
			return null;
		}

		IndexHits<Node> result;

		try (Transaction tx = database.beginTx()) {
			Index<Node> index = database.index().forNodes(indexName);
			result = index.query(QueryContext.numericRange(event.name(), 0L, timestamp));
			tx.success();
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndexHits<Relationship> relationshipsEligibleFor(LifecycleEvent event, long timestamp) {
		String indexName = config.relationshipIndexFor(event);
		if (indexName == null) {
			return null;
		}

		IndexHits<Relationship> result;

		try (Transaction tx = database.beginTx()) {
			Index<Relationship> index = database.index().forRelationships(indexName);
			result = index.query(QueryContext.numericRange(event.name(), 0L, timestamp));
			tx.success();
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeNode(LifecycleEvent event, Node node) {
		try (Transaction tx = database.beginTx()) {
			Index<Node> index = database.index().forNodes(config.nodeIndexFor(event));
			index.remove(node, event.name());
			tx.success();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeRelationship(LifecycleEvent event, Relationship relationship) {
		try (Transaction tx = database.beginTx()) {
			Index<Relationship> index = database.index().forRelationships(config.relationshipIndexFor(event));
			index.remove(relationship, event.name());
			tx.success();
		}
	}

	private Long getExpirationDate(Node node) {
		return getExpirationDate(node, config.getNodeExpirationProperty(), config.getNodeTtlProperty());
	}

	private Long getExpirationDate(Relationship relationship) {
		return getExpirationDate(relationship, config.getRelationshipExpirationProperty(), config.getRelationshipTtlProperty());
	}

	private Long getExpirationDate(PropertyContainer pc, String expirationProperty, String ttlProperty) {
		if (!hasExpirationProperty(pc, expirationProperty, ttlProperty)) {
			return null;
		}

		Long result = null;

		if (pc.hasProperty(expirationProperty)) {
			try {
				long expiryOffset = config.getExpiryOffset();
				result = Long.parseLong(pc.getProperty(expirationProperty).toString()) + expiryOffset;
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
				} else {
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

	private Long getRevivalDate(Node node) {
		return getRevivalDate(node, config.getNodeRevivalProperty());
	}

	private Long getRevivalDate(Relationship relationship) {
		return getRevivalDate(relationship, config.getRelationshipRevivalProperty());
	}

	private Long getRevivalDate(PropertyContainer pc, String revivalProperty) {

		Long result = null;

		if (pc.hasProperty(revivalProperty)) {
			try {
				long revivalOffset = config.getRevivalOffset();
				result = Long.parseLong(pc.getProperty(revivalProperty).toString()) + revivalOffset;
			} catch (NumberFormatException e) {
				LOG.warn("%s revival property is non-numeric: %s", id(pc), pc.getProperty(revivalProperty));
			}
		}

		return result;
	}


}
