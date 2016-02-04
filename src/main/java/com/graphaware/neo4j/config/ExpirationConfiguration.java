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

package com.graphaware.neo4j.config;

import com.graphaware.common.policy.InclusionPolicies;
import com.graphaware.runtime.config.BaseTxAndTimerDrivenModuleConfiguration;
import com.graphaware.runtime.policy.InclusionPoliciesFactory;


/**
 * {@link BaseTxAndTimerDrivenModuleConfiguration} for {@link com.graphaware.neo4j.ExpirationIndexModule}.
 */
public class ExpirationConfiguration extends BaseTxAndTimerDrivenModuleConfiguration<ExpirationConfiguration> {

    private static final String DEFAULT_EXPIRATION_INDEX = "expirationIndex";
    private static final String DEFAULT_EXPIRATION_PROPERTY = "_expire";

    private String expirationIndex = DEFAULT_EXPIRATION_INDEX;
    private String expirationProperty = DEFAULT_EXPIRATION_PROPERTY;


    public ExpirationConfiguration(InclusionPolicies inclusionPolicies, long initializeUntil, InstanceRolePolicy instanceRolePolicy) {
        super(inclusionPolicies, initializeUntil, instanceRolePolicy);
    }

    /**
     * Create a default configuration with
     * inclusion policies = {@link InclusionPoliciesFactory#allBusiness()},
     * and initialize until = {@link #ALWAYS}.
     * and instance role police = {@link com.graphaware.runtime.config.TimerDrivenModuleConfiguration.InstanceRolePolicy#ANY}
     */
    public static ExpirationConfiguration defaultConfiguration() {
        return new ExpirationConfiguration(InclusionPolicies.all(), ALWAYS, InstanceRolePolicy.ANY);
    }

    public String getExpirationIndex() {
        return expirationIndex;
    }

    public void setExpirationIndex(String expirationIndex) {
        this.expirationIndex = expirationIndex;
    }

    public String getExpirationProperty() {
        return expirationProperty;
    }

    public void setExpirationProperty(String expirationProperty) {
        this.expirationProperty = expirationProperty;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ExpirationConfiguration newInstance(InclusionPolicies inclusionPolicies, long initializeUntil, InstanceRolePolicy instanceRolePolicy) {
        return new ExpirationConfiguration(inclusionPolicies, initializeUntil, instanceRolePolicy);
    }


}
