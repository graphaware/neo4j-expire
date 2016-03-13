package com.graphaware.neo4j.expire.strategy;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * {@link ExpirationStrategy} that deletes the expired {@link Node} iff it has no {@link Relationship}s.
 */
public final class DeleteOrphanedNodeOnly implements ExpirationStrategy<Node> {

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
