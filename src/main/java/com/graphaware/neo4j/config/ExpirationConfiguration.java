package com.graphaware.neo4j.config;

import com.graphaware.common.policy.InclusionPolicies;
import com.graphaware.neo4j.strategies.ExpirationStrategy;
import com.graphaware.neo4j.strategies.ExpirationStrategyFactory;
import com.graphaware.neo4j.strategies.InvalidExpirationStrategyException;
import com.graphaware.neo4j.strategies.ManualExpirationStrategy;
import com.graphaware.runtime.config.BaseTxAndTimerDrivenModuleConfiguration;
import com.graphaware.runtime.config.BaseTxDrivenModuleConfiguration;
import com.graphaware.runtime.config.TimerDrivenModuleConfiguration;

public class ExpirationConfiguration extends BaseTxAndTimerDrivenModuleConfiguration<ExpirationConfiguration> {

    private static final String EXPIRATION_INDEX = "expirationIndex";
    private static final String EXPIRATION_PROPERTY = "_expire";

    private static final String EXPIRATION_STRATEGY_NAME = "manual";


    public ExpirationConfiguration(InclusionPolicies inclusionPolicies, long initializeUntil, InstanceRolePolicy instanceRolePolicy) {
        super(inclusionPolicies, initializeUntil, instanceRolePolicy);
    }

    public static ExpirationConfiguration defaultConfiguration() {
        return new ExpirationConfiguration(InclusionPolicies.all(), ALWAYS, InstanceRolePolicy.ANY);
    }

    public String getExpirationIndex() {
        return EXPIRATION_INDEX;
    }

    public String getExpirationProperty() {
        return EXPIRATION_PROPERTY;
    }

    @Override
    protected ExpirationConfiguration newInstance(InclusionPolicies inclusionPolicies, long initializeUntil, InstanceRolePolicy instanceRolePolicy) {
        return new ExpirationConfiguration(inclusionPolicies, initializeUntil, instanceRolePolicy);
    }
}
