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

import com.graphaware.neo4j.lifecycle.strategy.LifecycleStrategy;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import org.apache.commons.lang.StringUtils;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.springframework.util.Assert;

public interface LifecycleEvent {

	/**
	 * Evaluates the date, if any, on which this lifecycle event will be executed for a given node.
	 *
	 * @param node to evaluate.
	 * @return execution date.
	 */
	Long effectiveDate(Node node);

	/**
	 * Evaluates the date, if any, on which this lifecycle event will be executed for a given relationship.
	 *
	 * @param relationship to evaluate.
	 * @return execution date.
	 */
	Long effectiveDate(Relationship relationship);

	/**
	 * The node index name for this lifecycle event. If the event does not apply to nodes, it should return null.
	 */
	String nodeIndex();

	/**
	 * The relationship index name for this lifecycle event. If the event does not apply to relationships, it should
	 * return null.
	 */
	String relationshipIndex();

	/**
	 * The strategy to apply on nodes, if any, when this lifecycle event fires. This may be a CompositeStrategy, if
	 * multiple actions are required, or null if the event does not apply to nodes.
	 *
	 * @see com.graphaware.neo4j.lifecycle.strategy.CompositeStrategy
	 */
	LifecycleStrategy<Node> nodeStrategy();

	/**
	 * The strategy to apply on nodes, if any, when this lifecycle event fires. This may be a CompositeStrategy, if
	 * multiple actions are required, or null if the event does not apply to relationships.
	 *
	 * @see com.graphaware.neo4j.lifecycle.strategy.CompositeStrategy
	 */
	LifecycleStrategy<Relationship> relationshipStrategy();

	/**
	 * Given transaction data for a changed node, evaluate if the index should be updated.
	 */
	boolean shouldIndexChanged(Node node, ImprovedTransactionData td);

	/**
	 * Given transaction data for a changed relationship, evaluate if the index should be updated.
	 */
	boolean shouldIndexChanged(Relationship relationship, ImprovedTransactionData td);

	/**
	 * The event name. The returned value must be unique among all lifecycle events that are registered with the
	 * module.
	 */
	default String name() {
		return this.getClass().getSimpleName();
	}

	/**
	 * Called on startup to validate that the lifecycle event is in a correctly configured state for usage.
	 *
	 * @throws Exception
	 */
	default void validate() throws RuntimeException {
		if (StringUtils.isBlank(nodeIndex()) && StringUtils.isBlank(relationshipIndex())) {
			String msg = String.format("%s : neither node nor relationship index is configured.", name());
			throw new IllegalStateException(msg);
		}

		if (nodeIndex() != null && nodeStrategy() == null) {
			String msg = String.format("%s : node index is configured without a strategy.", name());
			throw new IllegalStateException(msg);
		}

		if (relationshipIndex() != null && relationshipStrategy() == null) {
			String msg = String.format("%s : relationship index is configured without a strategy.", name());
			throw new IllegalStateException(msg);
		}
	}
}

