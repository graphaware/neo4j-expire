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

package com.graphaware.neo4j.expire.config;

import com.graphaware.common.policy.InclusionPolicies;
import com.graphaware.neo4j.expire.ExpirationModule;
import com.graphaware.neo4j.expire.strategy.DeleteOrphanedNodeOnly;
import com.graphaware.neo4j.expire.strategy.DeleteRelationship;
import com.graphaware.neo4j.expire.strategy.ExpirationStrategy;
import com.graphaware.runtime.config.BaseTxAndTimerDrivenModuleConfiguration;
import com.graphaware.runtime.policy.InclusionPoliciesFactory;
import org.apache.commons.lang.StringUtils;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.springframework.util.Assert;


/**
 * {@link BaseTxAndTimerDrivenModuleConfiguration} for {@link ExpirationModule}.
 */
public class ExpirationConfiguration extends BaseTxAndTimerDrivenModuleConfiguration<ExpirationConfiguration> {

    private static final String DEFAULT_NODE_EXPIRATION_INDEX = "nodeExpirationIndex";
    private static final String DEFAULT_RELATIONSHIP_EXPIRATION_INDEX = "relationshipExpirationIndex";
    private static final String DEFAULT_NODE_EXPIRATION_PROPERTY = null;
    private static final String DEFAULT_RELATIONSHIP_EXPIRATION_PROPERTY = null;
    private static final String DEFAULT_NODE_TTL_PROPERTY = null;
    private static final String DEFAULT_RELATIONSHIP_TTL_PROPERTY = null;
    private static final ExpirationStrategy<Node> DEFAULT_NODE_EXPIRATION_STRATEGY = DeleteOrphanedNodeOnly.getInstance();
    private static final ExpirationStrategy<Relationship> DEFAULT_RELATIONSHIP_EXPIRATION_STRATEGY = DeleteRelationship.getInstance();

    private String nodeExpirationIndex;
    private String relationshipExpirationIndex;
    private String nodeExpirationProperty;
    private String relationshipExpirationProperty;
    private String nodeTtlProperty;
    private String relationshipTtlProperty;
    private ExpirationStrategy<Node> nodeExpirationStrategy;
    private ExpirationStrategy<Relationship> relationshipExpirationStrategy;

    /**
     * Construct a new configuration.
     *
     * @param inclusionPolicies              policies for inclusion of nodes, relationships, and properties for processing by the module. Must not be <code>null</code>.
     * @param initializeUntil                until what time in ms since epoch it is ok to re(initialize) the entire module in case the configuration
     *                                       has changed since the last time the module was started, or if it is the first time the module was registered.
     *                                       {@link #NEVER} for never, {@link #ALWAYS} for always.
     * @param instanceRolePolicy             specifies which role a machine must have in order to run the module with this configuration. Must not be <code>null</code>.
     * @param nodeExpirationIndex            name of the legacy index where node expiry dates are stored. Can be <code>null</code>.
     * @param relationshipExpirationIndex    name of the legacy index where relationship expiry dates are stored. Can be <code>null</code>.
     * @param nodeExpirationProperty         name of the node property that specifies the expiration date in ms since epoch. Can be <code>null</code>.
     * @param relationshipExpirationProperty name of the relationship property that specifies the expiration date in ms since epoch. Can be <code>null</code>.
     * @param nodeTtlProperty                name of the node property that specifies the TTL in ms. Can be <code>null</code>.
     * @param relationshipTtlProperty        name of the relationship property that specifies the TTL in ms. Can be <code>null</code>.
     * @param nodeExpirationStrategy         expiration strategy for nodes. Must not be <code>null</code>.
     * @param relationshipExpirationStrategy expiration strategy for relationships. Must not be <code>null</code>.
     */
    private ExpirationConfiguration(InclusionPolicies inclusionPolicies, long initializeUntil, InstanceRolePolicy instanceRolePolicy, String nodeExpirationIndex, String relationshipExpirationIndex, String nodeExpirationProperty, String relationshipExpirationProperty, String nodeTtlProperty, String relationshipTtlProperty, ExpirationStrategy<Node> nodeExpirationStrategy, ExpirationStrategy<Relationship> relationshipExpirationStrategy) {
        super(inclusionPolicies, initializeUntil, instanceRolePolicy);
        this.nodeExpirationIndex = nodeExpirationIndex;
        this.relationshipExpirationIndex = relationshipExpirationIndex;
        this.nodeExpirationProperty = nodeExpirationProperty;
        this.relationshipExpirationProperty = relationshipExpirationProperty;
        this.nodeTtlProperty = nodeTtlProperty;
        this.relationshipTtlProperty = relationshipTtlProperty;
        this.nodeExpirationStrategy = nodeExpirationStrategy;
        this.relationshipExpirationStrategy = relationshipExpirationStrategy;
    }

    public void validate() {
        cleanup();

        if (StringUtils.isBlank(nodeExpirationIndex) && StringUtils.isBlank(relationshipExpirationIndex)) {
            throw new IllegalStateException("Neither node nor relationship expiry is configured. What's the point of having the module?");
        }

        Assert.notNull(nodeExpirationStrategy);
        Assert.notNull(relationshipExpirationStrategy);

        if (nodeExpirationIndex != null && StringUtils.equals(nodeTtlProperty, nodeExpirationProperty)) {
            throw new IllegalStateException("Node TTL and expiration property are not allowed to be the same!");
        }

        if (relationshipExpirationIndex != null && StringUtils.equals(relationshipTtlProperty, relationshipExpirationProperty)) {
            throw new IllegalStateException("Relationship TTL and expiration property are not allowed to be the same!");
        }
    }

    private void cleanup() {
        if (StringUtils.isBlank(nodeExpirationIndex)) {
            nodeExpirationProperty = null;
            nodeTtlProperty = null;
        }

        if (StringUtils.isBlank(relationshipExpirationIndex)) {
            relationshipExpirationProperty = null;
            relationshipTtlProperty = null;
        }

        if (StringUtils.isBlank(nodeExpirationProperty) && StringUtils.isBlank(nodeTtlProperty)) {
            nodeExpirationIndex = null;
        }

        if (StringUtils.isBlank(relationshipExpirationProperty) && StringUtils.isBlank(relationshipTtlProperty)) {
            relationshipExpirationIndex = null;
        }
    }

    /**
     * Create a default configuration with inclusion policies = {@link InclusionPoliciesFactory#allBusiness()},
     * initialize until = {@link #ALWAYS}, instance role policy = {@link com.graphaware.runtime.config.TimerDrivenModuleConfiguration.InstanceRolePolicy#ANY},
     * and {@link #DEFAULT_NODE_EXPIRATION_INDEX}, {@link #DEFAULT_RELATIONSHIP_EXPIRATION_INDEX},{@link #DEFAULT_NODE_EXPIRATION_PROPERTY},
     * {@link #DEFAULT_RELATIONSHIP_EXPIRATION_PROPERTY},{@link #DEFAULT_NODE_TTL_PROPERTY}, {@link #DEFAULT_RELATIONSHIP_TTL_PROPERTY},
     * {@link #DEFAULT_NODE_EXPIRATION_STRATEGY}, and {@link #DEFAULT_RELATIONSHIP_EXPIRATION_STRATEGY}.
     */
    public static ExpirationConfiguration defaultConfiguration() {
        return new ExpirationConfiguration(InclusionPolicies.all(), ALWAYS, InstanceRolePolicy.ANY, DEFAULT_NODE_EXPIRATION_INDEX, DEFAULT_RELATIONSHIP_EXPIRATION_INDEX, DEFAULT_NODE_EXPIRATION_PROPERTY, DEFAULT_RELATIONSHIP_EXPIRATION_PROPERTY, DEFAULT_NODE_TTL_PROPERTY, DEFAULT_RELATIONSHIP_TTL_PROPERTY, DEFAULT_NODE_EXPIRATION_STRATEGY, DEFAULT_RELATIONSHIP_EXPIRATION_STRATEGY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ExpirationConfiguration newInstance(InclusionPolicies inclusionPolicies, long initializeUntil, InstanceRolePolicy instanceRolePolicy) {
        return new ExpirationConfiguration(inclusionPolicies, initializeUntil, instanceRolePolicy, getNodeExpirationIndex(), getRelationshipExpirationIndex(), getNodeExpirationProperty(), getRelationshipExpirationProperty(), getNodeTtlProperty(), getRelationshipTtlProperty(), getNodeExpirationStrategy(), getRelationshipExpirationStrategy());
    }

    public ExpirationConfiguration withNodeExpirationIndex(String nodeExpirationIndex) {
        return new ExpirationConfiguration(getInclusionPolicies(), initializeUntil(), getInstanceRolePolicy(), nodeExpirationIndex, getRelationshipExpirationIndex(), getNodeExpirationProperty(), getRelationshipExpirationProperty(), getNodeTtlProperty(), getRelationshipTtlProperty(), getNodeExpirationStrategy(), getRelationshipExpirationStrategy());
    }

    public ExpirationConfiguration withRelationshipExpirationIndex(String relationshipExpirationIndex) {
        return new ExpirationConfiguration(getInclusionPolicies(), initializeUntil(), getInstanceRolePolicy(), getNodeExpirationIndex(), relationshipExpirationIndex, getNodeExpirationProperty(), getRelationshipExpirationProperty(), getNodeTtlProperty(), getRelationshipTtlProperty(), getNodeExpirationStrategy(), getRelationshipExpirationStrategy());
    }

    public ExpirationConfiguration withNodeExpirationProperty(String nodeExpirationProperty) {
        return new ExpirationConfiguration(getInclusionPolicies(), initializeUntil(), getInstanceRolePolicy(), getNodeExpirationIndex(), getRelationshipExpirationIndex(), nodeExpirationProperty, getRelationshipExpirationProperty(), getNodeTtlProperty(), getRelationshipTtlProperty(), getNodeExpirationStrategy(), getRelationshipExpirationStrategy());
    }

    public ExpirationConfiguration withRelationshipExpirationProperty(String relationshipExpirationProperty) {
        return new ExpirationConfiguration(getInclusionPolicies(), initializeUntil(), getInstanceRolePolicy(), getNodeExpirationIndex(), getRelationshipExpirationIndex(), getNodeExpirationProperty(), relationshipExpirationProperty, getNodeTtlProperty(), getRelationshipTtlProperty(), getNodeExpirationStrategy(), getRelationshipExpirationStrategy());
    }

    public ExpirationConfiguration withNodeTtlProperty(String nodeTtlProperty) {
        return new ExpirationConfiguration(getInclusionPolicies(), initializeUntil(), getInstanceRolePolicy(), getNodeExpirationIndex(), getRelationshipExpirationIndex(), getNodeExpirationProperty(), getRelationshipExpirationProperty(), nodeTtlProperty, getRelationshipTtlProperty(), getNodeExpirationStrategy(), getRelationshipExpirationStrategy());
    }

    public ExpirationConfiguration withRelationshipTtlProperty(String relationshipTtlProperty) {
        return new ExpirationConfiguration(getInclusionPolicies(), initializeUntil(), getInstanceRolePolicy(), getNodeExpirationIndex(), getRelationshipExpirationIndex(), getNodeExpirationProperty(), getRelationshipExpirationProperty(), getNodeTtlProperty(), relationshipTtlProperty, getNodeExpirationStrategy(), getRelationshipExpirationStrategy());
    }

    public ExpirationConfiguration withNodeExpirationStrategy(ExpirationStrategy<Node> nodeExpirationStrategy) {
        return new ExpirationConfiguration(getInclusionPolicies(), initializeUntil(), getInstanceRolePolicy(), getNodeExpirationIndex(), getRelationshipExpirationIndex(), getNodeExpirationProperty(), getRelationshipExpirationProperty(), getNodeTtlProperty(), getRelationshipTtlProperty(), nodeExpirationStrategy, getRelationshipExpirationStrategy());
    }

    public ExpirationConfiguration withRelationshipExpirationStrategy(ExpirationStrategy<Relationship> relationshipExpirationStrategy) {
        return new ExpirationConfiguration(getInclusionPolicies(), initializeUntil(), getInstanceRolePolicy(), getNodeExpirationIndex(), getRelationshipExpirationIndex(), getNodeExpirationProperty(), getRelationshipExpirationProperty(), getNodeTtlProperty(), getRelationshipTtlProperty(), getNodeExpirationStrategy(), relationshipExpirationStrategy);
    }

    public String getNodeExpirationIndex() {
        return nodeExpirationIndex;
    }

    public String getRelationshipExpirationIndex() {
        return relationshipExpirationIndex;
    }

    public String getNodeExpirationProperty() {
        return nodeExpirationProperty;
    }

    public String getRelationshipExpirationProperty() {
        return relationshipExpirationProperty;
    }

    public String getNodeTtlProperty() {
        return nodeTtlProperty;
    }

    public String getRelationshipTtlProperty() {
        return relationshipTtlProperty;
    }

    public ExpirationStrategy<Node> getNodeExpirationStrategy() {
        return nodeExpirationStrategy;
    }

    public ExpirationStrategy<Relationship> getRelationshipExpirationStrategy() {
        return relationshipExpirationStrategy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        ExpirationConfiguration that = (ExpirationConfiguration) o;

        if (nodeExpirationIndex != null ? !nodeExpirationIndex.equals(that.nodeExpirationIndex) : that.nodeExpirationIndex != null) {
            return false;
        }
        if (relationshipExpirationIndex != null ? !relationshipExpirationIndex.equals(that.relationshipExpirationIndex) : that.relationshipExpirationIndex != null) {
            return false;
        }
        if (nodeExpirationProperty != null ? !nodeExpirationProperty.equals(that.nodeExpirationProperty) : that.nodeExpirationProperty != null) {
            return false;
        }
        if (relationshipExpirationProperty != null ? !relationshipExpirationProperty.equals(that.relationshipExpirationProperty) : that.relationshipExpirationProperty != null) {
            return false;
        }
        if (nodeTtlProperty != null ? !nodeTtlProperty.equals(that.nodeTtlProperty) : that.nodeTtlProperty != null) {
            return false;
        }
        if (relationshipTtlProperty != null ? !relationshipTtlProperty.equals(that.relationshipTtlProperty) : that.relationshipTtlProperty != null) {
            return false;
        }
        if (nodeExpirationStrategy != null ? !nodeExpirationStrategy.equals(that.nodeExpirationStrategy) : that.nodeExpirationStrategy != null) {
            return false;
        }
        return !(relationshipExpirationStrategy != null ? !relationshipExpirationStrategy.equals(that.relationshipExpirationStrategy) : that.relationshipExpirationStrategy != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (nodeExpirationIndex != null ? nodeExpirationIndex.hashCode() : 0);
        result = 31 * result + (relationshipExpirationIndex != null ? relationshipExpirationIndex.hashCode() : 0);
        result = 31 * result + (nodeExpirationProperty != null ? nodeExpirationProperty.hashCode() : 0);
        result = 31 * result + (relationshipExpirationProperty != null ? relationshipExpirationProperty.hashCode() : 0);
        result = 31 * result + (nodeTtlProperty != null ? nodeTtlProperty.hashCode() : 0);
        result = 31 * result + (relationshipTtlProperty != null ? relationshipTtlProperty.hashCode() : 0);
        result = 31 * result + (nodeExpirationStrategy != null ? nodeExpirationStrategy.hashCode() : 0);
        result = 31 * result + (relationshipExpirationStrategy != null ? relationshipExpirationStrategy.hashCode() : 0);
        return result;
    }
}
