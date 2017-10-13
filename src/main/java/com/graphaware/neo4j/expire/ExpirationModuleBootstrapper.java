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

package com.graphaware.neo4j.expire;

import java.util.Map;

import com.graphaware.common.log.LoggerFactory;
import com.graphaware.neo4j.expire.config.ExpirationConfiguration;
import com.graphaware.neo4j.expire.strategy.AddRemoveLabels;
import com.graphaware.neo4j.expire.strategy.DeleteNodeAndRelationships;
import com.graphaware.neo4j.expire.strategy.DeleteOrphanedNodeOnly;
import com.graphaware.runtime.module.BaseRuntimeModuleBootstrapper;
import com.graphaware.runtime.module.RuntimeModule;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.logging.Log;

/**
 * Bootstraps the {@link ExpirationModule} in server mode.
 */
public class ExpirationModuleBootstrapper extends
		BaseRuntimeModuleBootstrapper<ExpirationConfiguration> {

	private static final Log LOG = LoggerFactory.getLogger(ExpirationModuleBootstrapper.class);

	private static final String NODE_EXPIRATION_INDEX = "nodeExpirationIndex";
	private static final String RELATIONSHIP_EXPIRATION_INDEX = "relationshipExpirationIndex";
	private static final String NODE_EXPIRATION_PROPERTY = "nodeExpirationProperty";
	private static final String RELATIONSHIP_EXPIRATION_PROPERTY = "relationshipExpirationProperty";
	private static final String NODE_TTL_PROPERTY = "nodeTtlProperty";
	private static final String RELATIONSHIP_TTL_PROPERTY = "relationshipTtlProperty";
	private static final String NODE_EXPIRATION_STRATEGY = "nodeExpirationStrategy";
	private static final String MAX_NO_EXPIRATIONS = "maxExpirations";

	//TODO: Avoid adding a new key for every new strategy
	private static final String FORCE_DELETE = "force";
	private static final String ORPHAN_DELETE = "orphan";
	private static final String ADD_REMOVE_LABELS = "labels";

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
	protected RuntimeModule doBootstrapModule(String moduleId, Map<String, String> config,
	                                          GraphDatabaseService database, ExpirationConfiguration configuration) {
		if (configExists(config, NODE_EXPIRATION_INDEX)) {
			String nodeExpirationIndex = config.get(NODE_EXPIRATION_INDEX);
			LOG.info("Node expiration index set to %s", nodeExpirationIndex);
			configuration = configuration.withNodeExpirationIndex(nodeExpirationIndex);
		}

		if (configExists(config, RELATIONSHIP_EXPIRATION_INDEX)) {
			String relationshipExpirationIndex = config.get(RELATIONSHIP_EXPIRATION_INDEX);
			LOG.info("Relationship expiration index set to %s", relationshipExpirationIndex);
			configuration = configuration.withRelationshipExpirationIndex(relationshipExpirationIndex);
		}

		if (configExists(config, NODE_EXPIRATION_PROPERTY)) {
			String nodeExpirationProperty = config.get(NODE_EXPIRATION_PROPERTY);
			LOG.info("Node expiration property set to %s", nodeExpirationProperty);
			configuration = configuration.withNodeExpirationProperty(nodeExpirationProperty);
		}

		if (configExists(config, RELATIONSHIP_EXPIRATION_PROPERTY)) {
			String relationshipExpirationProperty = config.get(RELATIONSHIP_EXPIRATION_PROPERTY);
			LOG.info("Relationship expiration property set to %s", relationshipExpirationProperty);
			configuration = configuration
					.withRelationshipExpirationProperty(relationshipExpirationProperty);
		}

		if (configExists(config, NODE_TTL_PROPERTY)) {
			String nodeTtlProperty = config.get(NODE_TTL_PROPERTY);
			LOG.info("Node ttl property set to %s", nodeTtlProperty);
			configuration = configuration.withNodeTtlProperty(nodeTtlProperty);
		}

		if (configExists(config, RELATIONSHIP_TTL_PROPERTY)) {
			String relationshipTtlProperty = config.get(RELATIONSHIP_TTL_PROPERTY);
			LOG.info("Relationship ttl property set to %s", relationshipTtlProperty);
			configuration = configuration.withRelationshipTtlProperty(relationshipTtlProperty);
		}

		if (configExists(config, NODE_EXPIRATION_STRATEGY)) {
			String nodeExpirationStrategy = config.get(NODE_EXPIRATION_STRATEGY);

			LOG.info("Node expiration strategy set to %s", nodeExpirationStrategy);
			if (FORCE_DELETE.equals(nodeExpirationStrategy)) {
				DeleteNodeAndRelationships strategy = DeleteNodeAndRelationships.getInstance();
				strategy.setConfig(config);
				configuration = configuration.withNodeExpirationStrategy(strategy);
			} else if (ORPHAN_DELETE.endsWith(nodeExpirationStrategy)) {
				DeleteOrphanedNodeOnly strategy = DeleteOrphanedNodeOnly.getInstance();
				strategy.setConfig(config);
				configuration = configuration.withNodeExpirationStrategy(strategy);
			} else if (ADD_REMOVE_LABELS.endsWith(nodeExpirationStrategy)) {
				AddRemoveLabels instance = AddRemoveLabels.getInstance();
				instance.setConfig(config);
				configuration = configuration.withNodeExpirationStrategy(instance);
			} else {
				LOG.error("Not a valid expiration strategy: %s", nodeExpirationStrategy);
				throw new IllegalArgumentException("Not a valid expiration strategy.");
			}
		}

		if (configExists(config, MAX_NO_EXPIRATIONS)) {
			String maxNoExpirations = config.get(MAX_NO_EXPIRATIONS);
			LOG.info("Max number of expirations set to %s", maxNoExpirations);
			configuration = configuration.withMaxNoExpirations(Integer.valueOf(maxNoExpirations));
		}

		return new ExpirationModule(moduleId, database, configuration);
	}
}
