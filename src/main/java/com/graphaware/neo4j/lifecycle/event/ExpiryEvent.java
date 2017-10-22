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

package com.graphaware.neo4j.lifecycle.event;

import com.graphaware.common.log.LoggerFactory;
import com.graphaware.neo4j.lifecycle.strategy.LifecycleStrategy;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import org.neo4j.graphdb.Entity;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.logging.Log;

public class ExpiryEvent implements LifecycleEvent {

	private static final Log LOG = LoggerFactory.getLogger(ExpiryEvent.class);

	private String nodeExpirationProperty;
	private String nodeTtlProperty;
	private String relationshipExpirationProperty;
	private String relationshipTtlProperty;
	private Long expiryOffset;
	private String nodeIndex;
	private String relationshipIndex;
	private LifecycleStrategy<Node> nodeStrategy;
	private LifecycleStrategy<Relationship> relationshipStrategy;

	public ExpiryEvent(String nodeExpirationProperty,
	                   String nodeTtlProperty,
	                   String relationshipExpirationProperty,
	                   String relationshipTtlProperty,
	                   Long expiryOffset,
	                   String nodeIndex,
	                   String relationshipIndex,
	                   LifecycleStrategy<Node> nodeStrategy,
	                   LifecycleStrategy<Relationship> relationshipStrategy) {

		this.nodeExpirationProperty = nodeExpirationProperty;
		this.nodeTtlProperty = nodeTtlProperty;
		this.relationshipExpirationProperty = relationshipExpirationProperty;
		this.relationshipTtlProperty = relationshipTtlProperty;
		this.expiryOffset = expiryOffset;
		this.nodeIndex = nodeIndex;
		this.relationshipIndex = relationshipIndex;
		this.nodeStrategy = nodeStrategy;
		this.relationshipStrategy = relationshipStrategy;
	}

	@Override
	public Long effectiveDate(Node node) {
		return getExpirationDate(node, this.nodeExpirationProperty, this.nodeTtlProperty);
	}

	@Override
	public Long effectiveDate(Relationship relationship) {
		return getExpirationDate(relationship, this.relationshipExpirationProperty, this.relationshipTtlProperty);
	}

	@Override
	public String nodeIndex() {
		return nodeIndex;
	}

	@Override
	public String relationshipIndex() {
		return relationshipIndex;
	}

	@Override
	public LifecycleStrategy<Node> nodeStrategy() {
		return nodeStrategy;
	}

	@Override
	public LifecycleStrategy<Relationship> relationshipStrategy() {
		return relationshipStrategy;
	}

	@Override
	public boolean shouldIndex(Node node, ImprovedTransactionData td) {
		//TODO: Why index deleted prop?
		return (td.hasPropertyBeenCreated(node, this.nodeExpirationProperty)
				|| td.hasPropertyBeenCreated(node, this.nodeTtlProperty)
				|| td.hasPropertyBeenChanged(node, this.nodeExpirationProperty)
				|| td.hasPropertyBeenChanged(node, this.nodeTtlProperty)
				|| td.hasPropertyBeenDeleted(node, this.nodeExpirationProperty)
				|| td.hasPropertyBeenDeleted(node, this.nodeTtlProperty));
	}

	@Override
	public boolean shouldIndex(Relationship current, ImprovedTransactionData td) {
		//TODO: Why index deleted prop?
		return (td.hasPropertyBeenCreated(current, this.relationshipExpirationProperty)
				|| td.hasPropertyBeenCreated(current, this.relationshipTtlProperty)
				|| td.hasPropertyBeenChanged(current, this.relationshipExpirationProperty)
				|| td.hasPropertyBeenChanged(current, this.relationshipTtlProperty)
				|| td.hasPropertyBeenDeleted(current, this.relationshipExpirationProperty)
				|| td.hasPropertyBeenDeleted(current, this.relationshipTtlProperty));
	}

	private Long getExpirationDate(Entity entity, String expirationProperty, String ttlProperty) {
		if (!(entity.hasProperty(expirationProperty) || entity.hasProperty(ttlProperty))) {
			return null;
		}

		Long result = null;

		if (entity.hasProperty(expirationProperty)) {
			try {
				long expiryOffset = this.expiryOffset;
				result = Long.parseLong(entity.getProperty(expirationProperty).toString()) + expiryOffset;
			} catch (NumberFormatException e) {
				LOG.warn("%s expiration property is non-numeric: %s", entity.getId(), entity.getProperty(expirationProperty));
			}
		}

		if (entity.hasProperty(ttlProperty)) {
			try {
				long newResult = System.currentTimeMillis() + Long.parseLong(entity.getProperty(ttlProperty).toString());

				if (result != null) {
					LOG.warn("%s has both expiry date and a ttl.", entity.getId());

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
				LOG.warn("%s ttl property is non-numeric: %s", entity.getId(), entity.getProperty(ttlProperty));
			}
		}

		return result;
	}
}
