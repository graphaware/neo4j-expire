package com.graphaware.neo4j.config;

import com.graphaware.common.policy.InclusionPolicies;
import com.graphaware.runtime.config.BaseTxDrivenModuleConfiguration;

public class ExpirationConfiguration extends BaseTxDrivenModuleConfiguration<ExpirationConfiguration> {

    private static final String EXPIRATION_INDEX = "expirationIndex";
    private static final String EXPIRATION_PROPERTY = "_expire";

    protected ExpirationConfiguration(InclusionPolicies inclusionPolicies, long initializeUntil) {
        super(inclusionPolicies, initializeUntil);
    }


    public static ExpirationConfiguration defaultConfiguration() {
        return new ExpirationConfiguration(InclusionPolicies.all(), ALWAYS);
    }

    public String getExpirationIndex() {
        return EXPIRATION_INDEX;
    }

    public String getExpirationProperty() {
        return EXPIRATION_PROPERTY;
    }

    @Override
    protected ExpirationConfiguration newInstance(InclusionPolicies inclusionPolicies, long l) {
        return new ExpirationConfiguration(inclusionPolicies, l);
    }
}
