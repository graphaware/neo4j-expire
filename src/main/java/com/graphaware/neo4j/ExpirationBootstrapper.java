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

        if(config.containsKey("expirationProperty")) {
            configuration.setExpirationProperty(config.get("expirationProperty"));
        }
        if(config.containsKey("expirationIndex")) {
            configuration.setExpirationIndex(config.get("expirationIndex"));
        }

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
