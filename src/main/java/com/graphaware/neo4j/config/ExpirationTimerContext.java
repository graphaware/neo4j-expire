package com.graphaware.neo4j.config;

import com.graphaware.runtime.metadata.PositionNotFoundException;
import com.graphaware.runtime.metadata.TimerDrivenModuleContext;
import org.neo4j.graphdb.GraphDatabaseService;

public class ExpirationTimerContext implements TimerDrivenModuleContext {
    @Override
    public long earliestNextCall() {
        return ASAP;
    }

    @Override
    public Object find(GraphDatabaseService graphDatabaseService) throws PositionNotFoundException {
        return null;
    }
}
