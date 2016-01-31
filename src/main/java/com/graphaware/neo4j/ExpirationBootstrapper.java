package com.graphaware.neo4j;

import com.graphaware.neo4j.config.ExpirationConfiguration;
import com.graphaware.neo4j.indexer.ExpirationIndexer;
import com.graphaware.neo4j.indexer.LegacyIndexer;
import com.graphaware.neo4j.strategies.ExpirationStrategyFactory;
import com.graphaware.neo4j.strategies.InvalidExpirationStrategyException;
import com.graphaware.neo4j.strategies.ManualExpirationStrategy;
import com.graphaware.runtime.module.BaseRuntimeModuleBootstrapper;
import com.graphaware.runtime.module.RuntimeModule;
import com.graphaware.runtime.module.RuntimeModuleBootstrapper;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.Map;

public class ExpirationBootstrapper extends BaseRuntimeModuleBootstrapper<ExpirationConfiguration> implements RuntimeModuleBootstrapper {
    @Override
    protected ExpirationConfiguration defaultConfiguration() {
        return ExpirationConfiguration.defaultConfiguration();
    }

    @Override
    protected RuntimeModule doBootstrapModule(String moduleId, Map<String, String> config, GraphDatabaseService database, ExpirationConfiguration configuration) {
        ExpirationIndexer indexer = new LegacyIndexer(database, configuration);
        NodeDeleter deleter = new NodeDeleter(database, indexer);
        try {
            return new ExpirationIndexModule(moduleId,
                    indexer,
                    configuration,
                    deleter,
                    new ExpirationStrategyFactory(database, deleter).build("manual"));
        } catch (InvalidExpirationStrategyException e) {
            //TODO: Logging
            return new ExpirationIndexModule(moduleId,
                    new LegacyIndexer(database, configuration),
                    configuration,
                    new NodeDeleter(database, indexer),
                    new ManualExpirationStrategy(database, deleter));
        }
    }
}
