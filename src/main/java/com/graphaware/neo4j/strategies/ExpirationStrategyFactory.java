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

public class ExpirationStrategyFactory {

    private final GraphDatabaseService database;
    private final NodeDeleter deleter;

    public ExpirationStrategyFactory(GraphDatabaseService database, NodeDeleter deleter) {
        this.database = database;
        this.deleter = deleter;
    }

    /**
     * Simple factory method to return the requested strategy from the string config
     *
     * @param strategyConfiguration Configuration read from neo4j.properties and passed in by {@link com.graphaware.neo4j.ExpirationBootstrapper}
     * @return the requested expiration strategy
     * @throws InvalidExpirationStrategyException when the properties do not match any known strategy
     */
    public ExpirationStrategy build(String strategyConfiguration) throws InvalidExpirationStrategyException {
        switch (strategyConfiguration) {
            case "manual":
                return new ManualExpirationStrategy(database, deleter);
            case "deleteOrphans":
                return new DeleteOrphanedRelationsStrategy(database, deleter);
            default:
                throw new InvalidExpirationStrategyException("The expiration strategy is not a recognised strategy.");
        }
    }

}
