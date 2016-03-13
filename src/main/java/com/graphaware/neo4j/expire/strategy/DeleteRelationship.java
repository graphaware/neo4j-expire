package com.graphaware.neo4j.expire.strategy;

import org.neo4j.graphdb.Relationship;

/**
 * Default {@link Relationship} {@link ExpirationStrategy} that simply deletes the expired {@link Relationship}.
 */
public final class DeleteRelationship implements ExpirationStrategy<Relationship> {

    private static final DeleteRelationship INSTANCE = new DeleteRelationship();

    public static DeleteRelationship getInstance() {
        return INSTANCE;
    }

    private DeleteRelationship() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void expire(Relationship relationship) {
        relationship.delete();
    }
}
