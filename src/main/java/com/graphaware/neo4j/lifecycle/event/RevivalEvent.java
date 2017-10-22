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

public class RevivalEvent implements LifecycleEvent {

	private static final Log LOG = LoggerFactory.getLogger(RevivalEvent.class);

	private String nodeRevivalProperty;
	private String relationshipRevivalProperty;
	private Long revivalOffset;
	private String nodeIndex;
	private String relationshipIndex;
	private LifecycleStrategy<Node> nodeStrategy;
	private LifecycleStrategy<Relationship> relationshipStrategy;

	public RevivalEvent(String nodeRevivalProperty,
	                    String relationshipRevivalProperty,
	                    Long revivalOffset,
	                    String nodeIndex,
	                    String relationshipIndex,
	                    LifecycleStrategy<Node> nodeStrategy,
	                    LifecycleStrategy<Relationship> relationshipStrategy) {

		this.nodeRevivalProperty = nodeRevivalProperty;
		this.relationshipRevivalProperty = relationshipRevivalProperty;
		this.revivalOffset = revivalOffset;
		this.nodeIndex = nodeIndex;
		this.relationshipIndex = relationshipIndex;
		this.nodeStrategy = nodeStrategy;
		this.relationshipStrategy = relationshipStrategy;
	}

	@Override
	public Long effectiveDate(Node node) {
		return getRevivalDate(node, this.nodeRevivalProperty);
	}

	@Override
	public Long effectiveDate(Relationship relationship) {
		return getRevivalDate(relationship, this.relationshipRevivalProperty);
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
		return (td.hasPropertyBeenCreated(node, nodeRevivalProperty)
				|| td.hasPropertyBeenChanged(node, nodeRevivalProperty)
				|| td.hasPropertyBeenDeleted(node, nodeRevivalProperty));
	}

	@Override
	public boolean shouldIndex(Relationship rel, ImprovedTransactionData td) {
		//TODO: Why index deleted prop?
		return (td.hasPropertyBeenCreated(rel, relationshipRevivalProperty)
				|| td.hasPropertyBeenChanged(rel, relationshipRevivalProperty)
				|| td.hasPropertyBeenDeleted(rel, relationshipRevivalProperty));
	}

	private Long getRevivalDate(Entity entity, String revivalProperty) {

		Long result = null;
		if (entity.hasProperty(revivalProperty)) {
			try {
				result = Long.parseLong(entity.getProperty(revivalProperty).toString()) + revivalOffset;
			} catch (NumberFormatException e) {
				LOG.warn("%s revival property is non-numeric: %s", entity.getId(), entity.getProperty(revivalProperty));
			}
		}

		return result;
	}
}
