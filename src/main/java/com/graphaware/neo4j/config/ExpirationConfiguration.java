package com.graphaware.neo4j.config;

import com.graphaware.common.policy.InclusionPolicies;
import com.graphaware.runtime.config.BaseTxAndTimerDrivenModuleConfiguration;
import com.graphaware.runtime.policy.InclusionPoliciesFactory;


/**
 * {@link BaseTxAndTimerDrivenModuleConfiguration} for {@link com.graphaware.neo4j.ExpirationIndexModule}.
 */
public class ExpirationConfiguration extends BaseTxAndTimerDrivenModuleConfiguration<ExpirationConfiguration> {

    private static final String EXPIRATION_INDEX = "expirationIndex";
    private static final String EXPIRATION_PROPERTY = "_expire";

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
        return EXPIRATION_INDEX;
    }

    public String getExpirationProperty() {
        return EXPIRATION_PROPERTY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ExpirationConfiguration newInstance(InclusionPolicies inclusionPolicies, long initializeUntil, InstanceRolePolicy instanceRolePolicy) {
        return new ExpirationConfiguration(inclusionPolicies, initializeUntil, instanceRolePolicy);
    }
}
