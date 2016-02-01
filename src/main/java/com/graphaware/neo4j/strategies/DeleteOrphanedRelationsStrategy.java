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

package com.graphaware.neo4j.strategies;

import com.graphaware.neo4j.NodeDeleter;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.Date;

/**
 * {@link ExpirationStrategy} that will remove all nodes that have expired, and all relationships
 * that are attached to these nodes
 */
public class DeleteOrphanedRelationsStrategy extends ExpirationStrategy {

    public DeleteOrphanedRelationsStrategy(GraphDatabaseService databaseService, NodeDeleter nodeDeleter) {
        super(databaseService, nodeDeleter);
    }

    /**
     * {@inheritDoc}
     */
    public void expireNodes() {
        long millisecondsFromEpoch = new Date().getTime();

        deleter.deleteNodesIncludingAdjoiningEdgesExpiringBefore(millisecondsFromEpoch);
    }
}