/*
 * Copyright (c) 2013-2020 GraphAware
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

package com.graphaware.neo4j.expire.strategy;

import com.graphaware.common.serialize.Serializer;
import com.graphaware.common.serialize.SingletonSerializer;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * {@link ExpirationStrategy} that deletes the expired {@link Node} iff it has no {@link Relationship}s.
 */
public final class DeleteOrphanedNodeOnly implements ExpirationStrategy<Node> {

    static {
        Serializer.register(DeleteOrphanedNodeOnly.class, new SingletonSerializer());
    }

    private static final DeleteOrphanedNodeOnly INSTANCE = new DeleteOrphanedNodeOnly();

    public static DeleteOrphanedNodeOnly getInstance() {
        return INSTANCE;
    }

    private DeleteOrphanedNodeOnly() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void expire(Node node) {
        if (!node.hasRelationship()) {
            node.delete();
        }
    }
}
