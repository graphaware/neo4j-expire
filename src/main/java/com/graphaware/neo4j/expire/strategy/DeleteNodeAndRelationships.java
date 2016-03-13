package com.graphaware.neo4j.expire.strategy;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * {@link ExpirationStrategy} that deletes the expired {@link Node} and a all its {@link Relationship}s.
 */
public final class DeleteNodeAndRelationships implements ExpirationStrategy<Node> {

    private static final DeleteNodeAndRelationships INSTANCE = new DeleteNodeAndRelationships();

    public static DeleteNodeAndRelationships getInstance() {
        return INSTANCE;
    }

    private DeleteNodeAndRelationships() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void expire(Node node) {
        for (Relationship r : node.getRelationships()) {
            r.delete();
        }

        node.delete();
    }
}
