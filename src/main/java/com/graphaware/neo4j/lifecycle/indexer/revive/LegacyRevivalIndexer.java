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

package com.graphaware.neo4j.lifecycle.indexer.revive;

import static com.graphaware.common.util.PropertyContainerUtils.id;

import com.graphaware.common.log.LoggerFactory;
import com.graphaware.neo4j.lifecycle.config.LifecycleConfiguration;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.index.lucene.QueryContext;
import org.neo4j.index.lucene.ValueContext;
import org.neo4j.logging.Log;

//TODO: Merge indexers into a single class?
public class LegacyRevivalIndexer implements RevivalIndexer {

	private static final Log LOG = LoggerFactory.getLogger(LegacyRevivalIndexer.class);
	private static final String REVIVE = "_revive";


	private GraphDatabaseService database;
	private LifecycleConfiguration configuration;

	public LegacyRevivalIndexer(GraphDatabaseService database, LifecycleConfiguration configuration) {
		this.database = database;
		this.configuration = configuration;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void indexNode(Node node) {
		Long revivalDate = getRevivalDate(node);

		if (revivalDate != null) {
			database.index().forNodes(configuration.getNodeExpirationIndex()).add(node, REVIVE,
					new ValueContext(revivalDate).indexNumeric());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void indexRelationship(Relationship relationship) {
		Long revivalDate = getRevivalDate(relationship);

		if (revivalDate != null) {
			database.index().forRelationships(configuration.getRelationshipExpirationIndex()).add(relationship, REVIVE,
					new ValueContext(revivalDate).indexNumeric());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndexHits<Node> candidateNodesRevivingBefore(long timestamp) {
		if (configuration.getNodeExpirationIndex() == null) {
			return null;
		}

		IndexHits<Node> result;

		try (Transaction tx = database.beginTx()) {
			Index<Node> index = database.index().forNodes(configuration.getNodeExpirationIndex());
			result = index.query(QueryContext.numericRange(REVIVE, 0L, timestamp));
			tx.success();
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndexHits<Relationship> candidateRelsRevivingBefore(long timestamp) {

		if (configuration.getRelationshipExpirationIndex() == null) {
			return null;
		}

		IndexHits<Relationship> result;

		try (Transaction tx = database.beginTx()) {
			Index<Relationship> index = database.index().forRelationships(configuration.getRelationshipExpirationIndex());
			result = index.query(QueryContext.numericRange(REVIVE, 0L, timestamp));
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

			index.remove(node, REVIVE);

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

			index.remove(relationship, REVIVE);

			tx.success();
		}
	}

	private Long getRevivalDate(Node node) {
		return getRevivalDate(node, configuration.getNodeRevivalProperty());
	}

	private Long getRevivalDate(Relationship relationship) {
		return getRevivalDate(relationship, configuration.getRelationshipRevivalProperty());
	}

	private Long getRevivalDate(PropertyContainer pc, String revivalProperty) {

		Long result = null;

		if (pc.hasProperty(revivalProperty)) {
			try {
				result = Long.parseLong(pc.getProperty(revivalProperty).toString()) + configuration.getRevivalOffset();
			} catch (NumberFormatException e) {
				LOG.warn("%s revival property is non-numeric: %s", id(pc), pc.getProperty(revivalProperty));
			}
		}

		return result;
	}



}
