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

package com.graphaware.neo4j.lifecycle.config;


import static com.graphaware.test.unit.GraphUnit.assertSameGraph;
import static com.graphaware.test.util.TestUtils.waitFor;
import static org.junit.Assert.assertTrue;

import com.graphaware.test.integration.GraphAwareIntegrationTest;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;

public class AddRemoveLabelsExpiryRevivalTest extends GraphAwareIntegrationTest {

  private static final long SECOND = 1_000;

  @Override
  protected String configFile() {
    return "neo4j-expire-revive-set-labels.conf";
  }

  @Test
  public void shouldAddInactiveLabelWhenExpiryDateReached() {
    getDatabase().execute("CREATE (w:Warmup)");
    getDatabase().execute("MATCH (n) DETACH DELETE n");

    long now = System.currentTimeMillis();
    long fiveSecondsAgo = now - 5 * SECOND;

    getDatabase().execute("CREATE (c1:CandidateProfile {name:'Anne', lastActive:" + fiveSecondsAgo + "})-[:LIKES]->(a1:Artist {name:'Leonard Cohen', lastActive:" + fiveSecondsAgo + "})");

//    assertSameGraph(getDatabase(), "CREATE (c1:CandidateProfile {name:'Anne', lastActive:" + nineSecondsAgo + "})-[:LIKES]->(a1:Artist {name:'Leonard Cohen', lastActive:" + nineSecondsAgo + "})");

    waitFor(200);

    //It immediately qualifies for revival
    assertSameGraph(getDatabase(), "CREATE (c1:CandidateProfile:ActiveProfile:Bar {name:'Anne', lastActive:" +
            fiveSecondsAgo + "})-[:LIKES]->(a1:Artist {name:'Leonard Cohen', lastActive:" + fiveSecondsAgo + "})");

    waitFor(5100 - (System.currentTimeMillis() - now));

    //Labels applied
    assertSameGraph(getDatabase(), "CREATE (c1:CandidateProfile:InactiveProfile:Foo {name:'Anne', lastActive:" +
            fiveSecondsAgo + "})-[:LIKES]->(a1:Artist {name:'Leonard Cohen', lastActive:" + fiveSecondsAgo + "})");

    //label not applied because of inclusion policies
    assertSameGraph(getDatabase(), "CREATE (c1:CandidateProfile:InactiveProfile:Foo {name:'Anne', lastActive:" +
            fiveSecondsAgo + "})-[:LIKES]->(a1:Artist {name:'Leonard Cohen', lastActive:" + fiveSecondsAgo + "})");

    now = System.currentTimeMillis();
    getDatabase().execute("MATCH (c1:CandidateProfile {name:'Anne'}) set c1.lastActive = " + now);

    waitFor(500);

    //It revives again, based on last active date
    assertSameGraph(getDatabase(), "CREATE (c1:CandidateProfile:ActiveProfile:Bar {name:'Anne', lastActive:" +
            now + "})-[:LIKES]->(a1:Artist {name:'Leonard Cohen', lastActive:" + fiveSecondsAgo + "})");

  }
}
