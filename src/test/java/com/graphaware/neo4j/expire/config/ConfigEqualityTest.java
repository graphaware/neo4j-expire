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

import com.graphaware.common.policy.composite.CompositeNodeInclusionPolicy;
import com.graphaware.common.policy.spel.SpelNodeInclusionPolicy;
import com.graphaware.common.serialize.Serializer;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConfigEqualityTest {

    @Test
    public void equalConfigShouldBeEqual() {
        assertTrue(ExpirationConfiguration.defaultConfiguration().withNodeTtlProperty("ttl").equals(ExpirationConfiguration.defaultConfiguration().withNodeTtlProperty("ttl")));
        assertTrue(ExpirationConfiguration.defaultConfiguration().withNodeTtlProperty("ttl").withNodeExpirationIndex("bla").equals(ExpirationConfiguration.defaultConfiguration().withNodeTtlProperty("ttl").withNodeExpirationIndex("bla")));
        assertTrue(ExpirationConfiguration.defaultConfiguration().withNodeTtlProperty("ttl").with(CompositeNodeInclusionPolicy.of(new SpelNodeInclusionPolicy("hasLabel('Test')"))).equals(ExpirationConfiguration.defaultConfiguration().withNodeTtlProperty("ttl").with(CompositeNodeInclusionPolicy.of(new SpelNodeInclusionPolicy("hasLabel('Test')")))));
        assertTrue(ExpirationConfiguration.defaultConfiguration().withNodeTtlProperty("ttl").with(CompositeNodeInclusionPolicy.of(new SpelNodeInclusionPolicy("hasLabel('Test')"))).equals(Serializer.fromByteArray(Serializer.toByteArray(ExpirationConfiguration.defaultConfiguration().withNodeTtlProperty("ttl").with(CompositeNodeInclusionPolicy.of(new SpelNodeInclusionPolicy("hasLabel('Test')")))))));

        assertFalse(ExpirationConfiguration.defaultConfiguration().withNodeTtlProperty("ttl").equals(ExpirationConfiguration.defaultConfiguration().withNodeTtlProperty("different")));
        assertFalse(ExpirationConfiguration.defaultConfiguration().withNodeTtlProperty("ttl").equals(ExpirationConfiguration.defaultConfiguration().withNodeTtlProperty("ttl").withRelationshipExpirationProperty("ttl")));
        assertFalse(ExpirationConfiguration.defaultConfiguration().withNodeTtlProperty("ttl").withNodeExpirationIndex("bla").equals(ExpirationConfiguration.defaultConfiguration().withNodeTtlProperty("ttl").withRelationshipExpirationIndex("bla")));
    }
}
