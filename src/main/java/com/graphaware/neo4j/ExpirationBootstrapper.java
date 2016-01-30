package com.graphaware.neo4j;

import com.graphaware.neo4j.config.ExpirationConfiguration;
import com.graphaware.neo4j.indexer.LegacyIndexer;
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
        return new ExpirationModule(moduleId, new LegacyIndexer(database, configuration), configuration);
    }
}
