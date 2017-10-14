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

package com.graphaware.neo4j.lifecycle.expire;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.graphaware.common.log.LoggerFactory;
import com.graphaware.neo4j.lifecycle.expire.config.ExpirationConfiguration;
import com.graphaware.neo4j.lifecycle.expire.strategy.*;
import com.graphaware.neo4j.lifecycle.utils.SingletonResolver;
import com.graphaware.runtime.module.BaseRuntimeModuleBootstrapper;
import com.graphaware.runtime.module.RuntimeModule;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.logging.Log;

/**
 * Bootstraps the {@link LifecyleModule} in server mode.
 */
public class LifecycleModuleBootstrapper extends BaseRuntimeModuleBootstrapper<ExpirationConfiguration> {

	private static final Log LOG = LoggerFactory.getLogger(LifecycleModuleBootstrapper.class);

	private static final String NODE_EXPIRATION_INDEX = "nodeExpirationIndex";
	private static final String RELATIONSHIP_EXPIRATION_INDEX = "relationshipExpirationIndex";
	private static final String NODE_EXPIRATION_PROPERTY = "nodeExpirationProperty";
	private static final String RELATIONSHIP_EXPIRATION_PROPERTY = "relationshipExpirationProperty";
	private static final String NODE_TTL_PROPERTY = "nodeTtlProperty";
	private static final String RELATIONSHIP_TTL_PROPERTY = "relationshipTtlProperty";
	private static final String NODE_EXPIRATION_STRATEGY = "nodeExpirationStrategy";
	private static final String RELATIONSHIP_EXPIRATION_STRATEGY = "relationshipExpirationStrategy";
	private static final String MAX_NO_EXPIRATIONS = "maxExpirations";

	private static final String FORCE_DELETE = "force";
	private static final String ORPHAN_DELETE = "orphan";
	private static final String DELETE_REL = "delete";
	private static final String COMPOSITE = "composite\\((.*?)\\)";

	private SingletonResolver<? extends ExpirationStrategy<Node>> nodeExpireLoader = new SingletonResolver<>();
	private SingletonResolver<? extends ExpirationStrategy<Relationship>> relExpireLoader = new SingletonResolver<>();

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
			Matcher matcher = Pattern.compile(COMPOSITE).matcher(nodeExpirationStrategy);
			if (FORCE_DELETE.equals(nodeExpirationStrategy)) {
				DeleteNodeAndRelationships strategy = DeleteNodeAndRelationships.getInstance();
				strategy.setConfig(config);
				configuration = configuration.withNodeExpirationStrategy(strategy);
			} else if (ORPHAN_DELETE.endsWith(nodeExpirationStrategy)) {
				DeleteOrphanedNodeOnly strategy = DeleteOrphanedNodeOnly.getInstance();
				strategy.setConfig(config);
				configuration = configuration.withNodeExpirationStrategy(strategy);
			} else if (matcher.find()) {
				List<? extends ExpirationStrategy<Node>> list = nodeExpireLoader.resolve(matcher.group(1));
				CompositeExpirationStrategy<Node> strategy = new CompositeExpirationStrategy<>(list);
				strategy.setConfig(config);
				configuration = configuration.withNodeExpirationStrategy(strategy);
			} else {
				LOG.error("Not a valid node expiration strategy: %s", nodeExpirationStrategy);
				throw new IllegalArgumentException(String.format("Not a valid expiration strategy: %s",
						nodeExpirationStrategy));
			}
		}

		if (configExists(config, RELATIONSHIP_EXPIRATION_STRATEGY)) {
			String relationshipExpirationStrategy = config.get(RELATIONSHIP_EXPIRATION_STRATEGY);
			LOG.info("Relationship expiration strategy set to %s", relationshipExpirationStrategy);
			Matcher composite = Pattern.compile(COMPOSITE).matcher(relationshipExpirationStrategy);
			if (DELETE_REL.endsWith(relationshipExpirationStrategy)) {
				DeleteRelationship strategy = DeleteRelationship.getInstance();
				strategy.setConfig(config);
				configuration = configuration.withRelationshipExpirationStrategy(strategy);
			} else if (composite.find()) {
				List<? extends ExpirationStrategy<Relationship>> list = relExpireLoader.resolve(composite.group(1));
				CompositeExpirationStrategy<Relationship> strategy = new CompositeExpirationStrategy<>(list);
				strategy.setConfig(config);
				configuration = configuration.withRelationshipExpirationStrategy(strategy);
			} else {
				LOG.error("Not a valid relationship expiration strategy: %s", relationshipExpirationStrategy);
				throw new IllegalArgumentException(String.format("Not a valid expiration strategy: %s",
						relationshipExpirationStrategy));
			}
		}

		if (configExists(config, MAX_NO_EXPIRATIONS)) {
			String maxNoExpirations = config.get(MAX_NO_EXPIRATIONS);
			LOG.info("Max number of expirations set to %s", maxNoExpirations);
			configuration = configuration.withMaxNoExpirations(Integer.valueOf(maxNoExpirations));
		}

		return new LifecyleModule(moduleId, database, configuration);
	}
}
