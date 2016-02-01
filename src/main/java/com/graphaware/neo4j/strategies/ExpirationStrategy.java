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

/**
 * Abstract Strategy to determine how nodes are expired in an automated manner, used by {@link com.graphaware.neo4j.ExpirationIndexModule}
 */
public abstract class ExpirationStrategy {
    protected final GraphDatabaseService database;
    protected final NodeDeleter deleter;

    protected ExpirationStrategy(GraphDatabaseService databaseService, NodeDeleter deleter) {
        this.database = databaseService;
        this.deleter = deleter;
    }

    /**
     * Method called by {@link com.graphaware.neo4j.ExpirationIndexModule} at regular intervals
     * Intended to work to expire any nodes that have expired.
     */
    public abstract void expireNodes();
}
