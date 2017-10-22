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

import javax.management.relation.Relation;

import com.graphaware.neo4j.lifecycle.strategy.LifecycleStrategy;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public interface LifecycleEvent {

	Long effectiveDate(Node node);

	Long effectiveDate(Relationship relationship);

	String nodeIndex();

	String relationshipIndex();

	LifecycleStrategy<Node> nodeStrategy();

	LifecycleStrategy<Relationship> relationshipStrategy();

	boolean shouldIndex(Node node, ImprovedTransactionData td);

	boolean shouldIndex(Relationship relationship, ImprovedTransactionData td);

	default String name() {
		return this.getClass().getSimpleName();
	}

}

