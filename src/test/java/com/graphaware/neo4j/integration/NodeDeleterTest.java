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

package com.graphaware.neo4j.integration;

import com.graphaware.neo4j.NodeDeleter;
import com.graphaware.neo4j.config.ExpirationConfiguration;
import com.graphaware.neo4j.indexer.LegacyIndexer;
import com.graphaware.neo4j.integration.helpers.ExpirationIntegrationTest;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class NodeDeleterTest extends ExpirationIntegrationTest {

    @Test
    public void nodesShouldBeDeletedIfTheyAreAfterTimestamp() {
        getDatabase().execute("CREATE (p:Person {name: 'Dave', _expire: 123})");
        getDatabase().execute("CREATE (p:Person {name: 'Dave', _expire: 234})");

        NodeDeleter deleter = new NodeDeleter(getDatabase(), new LegacyIndexer(getDatabase(), ExpirationConfiguration.defaultConfiguration()));

        deleter.deleteNodesIncludingAdjoiningEdgesExpiringBefore(200);

        assertThat(getNodeCount(), equalTo(1L));
    }

    @Test
    public void noNodesShouldBeDeletedIfTimestampIsEarlierThanAllNodes() {
        getDatabase().execute("CREATE (p:Person {name: 'Dave', _expire: 123})");
        getDatabase().execute("CREATE (p:Person {name: 'Dave', _expire: 234})");

        NodeDeleter deleter = new NodeDeleter(getDatabase(), new LegacyIndexer(getDatabase(), ExpirationConfiguration.defaultConfiguration()));

        deleter.deleteNodesIncludingAdjoiningEdgesExpiringBefore(50);

        assertThat(getNodeCount(), equalTo(2L));
    }


}
