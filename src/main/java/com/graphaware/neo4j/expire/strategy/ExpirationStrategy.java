package com.graphaware.neo4j.expire.strategy;

import org.neo4j.graphdb.PropertyContainer;

/**
 * A strategy for expiring {@link PropertyContainer}s.
 *
 * @param <P> type of container this strategy is for.
 */
public interface ExpirationStrategy<P extends PropertyContainer> {

    /**
     * Expire a container.
     *
     * @param pc to expire.
     */
    void expire(P pc);
}
