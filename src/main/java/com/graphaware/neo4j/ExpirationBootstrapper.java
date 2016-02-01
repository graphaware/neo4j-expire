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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


/**
 * Bootstraps the {@link ExpirationIndexModule} in server mode
 */
public class ExpirationBootstrapper extends BaseRuntimeModuleBootstrapper<ExpirationConfiguration> implements RuntimeModuleBootstrapper {
    private static final Logger LOG = LoggerFactory.getLogger(ExpirationBootstrapper.class);

    /**
     * {@inheritDoc}
     */
    @Override
    protected ExpirationConfiguration defaultConfiguration() {
        return ExpirationConfiguration.defaultConfiguration();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RuntimeModule doBootstrapModule(String moduleId, Map<String, String> config, GraphDatabaseService database, ExpirationConfiguration configuration) {
        ExpirationIndexer indexer = new LegacyIndexer(database, configuration);
        NodeDeleter deleter = new NodeDeleter(database, indexer);
        try {
            return new ExpirationIndexModule(moduleId,
                    indexer,
                    configuration,
                    new ExpirationStrategyFactory(database, deleter).build(config.get("expirationStrategy")));
        } catch (InvalidExpirationStrategyException e) {

            LOG.warn("No valid expiration strategy found, falling back to manual strategy");

            return new ExpirationIndexModule(moduleId,
                    new LegacyIndexer(database, configuration),
                    configuration,
                    new ManualExpirationStrategy(database, deleter));
        }
    }
}
