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
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class DeleteOrphanedNodesTest extends ExpirationIntegrationTest {

    private static final long ONE_SECOND = 1000L;

    @Override
    protected String propertiesFile() {
        try {
            return new ClassPathResource("neo4j-expire-delete-orphans.properties").getFile().getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void nodeGetsDeletedAfterExpirationTime() throws InterruptedException {
        Long inOneSecond = new Date().getTime() + ONE_SECOND;
        getDatabase().execute(
                String.format("CREATE (p:Person {name: 'Dave', _expire: %d})", inOneSecond));

        Thread.sleep(3 * ONE_SECOND);

        assertThat(getNodeCount(), equalTo(0L));
    }

    @Test
    public void nodeGetsDeletedWhenInRelationAfterExpirationTime() throws InterruptedException {
        Long inOneSecond = new Date().getTime() + ONE_SECOND;
        getDatabase().execute(
                String.format("CREATE (p:Person {name: 'Dave', _expire: %d})", inOneSecond));
        getDatabase().execute("CREATE (p:Person {name: 'Bill'})");
        getDatabase().execute("MATCH (p:Person),(q:Person)\n" +
                "WHERE p.name = 'Dave' AND q.name = 'Bill'\n" +
                "CREATE (q)-[r:Father]->(p)\n" +
                "RETURN r");

        Thread.sleep(5 * ONE_SECOND);

        assertThat(getNodeCount(), equalTo(1L));
    }

    @Test
    public void nodesWithoutExpirationPropertiesDoNotGetExpired() throws InterruptedException {
        getDatabase().execute("CREATE (p:Person {name: 'Bill'})");

        Thread.sleep(3 * ONE_SECOND);

        assertThat(getNodeCount(), equalTo(1L));
    }

    @Test
    public void nodesDoNotExpireBeforeTheirTime() throws InterruptedException {
        Long inTenSeconds = new Date().getTime() + (10 * ONE_SECOND);
        getDatabase().execute(
                String.format("CREATE (p:Person {name: 'Dave', _expire: %d})", inTenSeconds));

        Thread.sleep(3 * ONE_SECOND);

        assertThat(getNodeCount(), equalTo(1L));
    }
}
