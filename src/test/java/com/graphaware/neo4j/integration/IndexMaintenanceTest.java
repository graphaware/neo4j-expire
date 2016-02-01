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

import com.graphaware.neo4j.integration.helpers.ExpirationIntegrationTest;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class IndexMaintenanceTest extends ExpirationIntegrationTest {


    @Test
    public void nodesWithNumericExpirationPropertiesGetIndexed() throws InterruptedException {
        getDatabase().execute("CREATE (p:Person {name: 'Dave', _expire: 123})");

        try (Transaction tx = getDatabase().beginTx()) {
            assertThat(countNodesExpiringBefore1000(), equalTo(1L));
        }
    }

    @Test
    public void nodesWithNonNumericExpirationPropertiesGetIndexed() throws InterruptedException {
        getDatabase().execute("CREATE (p:Person {name: 'Dave', _expire: 'asd'})");

        try (Transaction tx = getDatabase().beginTx()) {
            assertThat(countNodesExpiringBefore1000(), equalTo(0L));
        }
    }

    @Test
    public void nodesWithNoExpirationPropertiesDoNotGetIndexed() throws InterruptedException {
        getDatabase().execute("CREATE (p:Person {name: 'Dave'})");

        try (Transaction tx = getDatabase().beginTx()) {
            assertThat(countNodesExpiringBefore1000(), equalTo(0L));
        }
    }

    @Test
    public void nodesAreRemovedFromIndexIfTheyLoseTheirExpirationProperty() {
        getDatabase().execute("CREATE (p:Person {name: 'Dave', _expire: 123})");
        getDatabase().execute("MATCH (p:Person {name: 'Dave', _expire: 123}) SET p._expire = NULL RETURN p");

        try (Transaction tx = getDatabase().beginTx()) {
            assertThat(countNodesExpiringBefore1000(), equalTo(0L));
        }
    }

    @Test
    public void nodesAreUpdatedFromIndexIfTheyChangeTheirExpirationProperty() {
        getDatabase().execute("CREATE (p:Person {name: 'Dave', _expire: 123})");
        getDatabase().execute("CREATE (p:Person {name: 'Bill', _expire: 234})");

        getDatabase().execute("MATCH (p:Person {name: 'Dave', _expire: 123}) SET p._expire = 100000 RETURN p");

        try (Transaction tx = getDatabase().beginTx()) {
            assertThat(countNodesExpiringBefore1000(), equalTo(1L));
        }
    }

}
