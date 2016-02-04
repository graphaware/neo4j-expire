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

package com.graphaware.neo4j;

import com.graphaware.common.util.Change;
import com.graphaware.neo4j.config.ExpirationConfiguration;
import com.graphaware.neo4j.indexer.ExpirationIndexer;
import com.graphaware.neo4j.strategies.ExpirationStrategy;
import com.graphaware.runtime.config.BaseTxAndTimerDrivenModuleConfiguration;
import com.graphaware.runtime.metadata.EmptyContext;
import com.graphaware.runtime.metadata.TimerDrivenModuleContext;
import com.graphaware.runtime.metadata.TxDrivenModuleMetadata;
import com.graphaware.runtime.module.DeliberateTransactionRollbackException;
import com.graphaware.runtime.module.TimerDrivenModule;
import com.graphaware.runtime.module.TxDrivenModule;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExpirationIndexModule implements TxDrivenModule, TimerDrivenModule {

    private static final Logger LOG = LoggerFactory.getLogger(NodeDeleter.class);

    private final ExpirationIndexer indexer;
    private final ExpirationConfiguration config;
    private final String moduleId;

    private final ExpirationStrategy expirationStrategy;

    protected ExpirationIndexModule(String moduleId, ExpirationIndexer indexer, ExpirationConfiguration config, ExpirationStrategy expirationStrategy) {
        this.moduleId = moduleId;
        this.indexer = indexer;
        this.config = config;
        this.expirationStrategy = expirationStrategy;
    }

    @Override
    public void start(GraphDatabaseService graphDatabaseService) {

    }


    @Override
    public Void beforeCommit(ImprovedTransactionData improvedTransactionData) throws DeliberateTransactionRollbackException {
        for (Node node : improvedTransactionData.getAllCreatedNodes()) {
            indexer.indexNode(node);
            LOG.debug("Node indexed: %s", node.toString());
        }

        for (Node node : improvedTransactionData.getAllDeletedNodes()) {
            indexer.removeNode(node);
            LOG.debug("Node removed from index: %s", node.toString());
        }

        for (Change<Node> change : improvedTransactionData.getAllChangedNodes()) {
            if (hasUpdatedExpireProperty(change.getPrevious(), change.getCurrent())) {
                indexer.removeNode(change.getPrevious());
                indexer.indexNode(change.getCurrent());
                LOG.debug("Node index updated: %s", change.getCurrent().toString());
            }
        }

        return null;
    }

    @Override
    public BaseTxAndTimerDrivenModuleConfiguration getConfiguration() {
        return config;
    }

    @Override
    public TimerDrivenModuleContext createInitialContext(GraphDatabaseService graphDatabaseService) {
        return new EmptyContext();
    }

    @Override
    public TimerDrivenModuleContext doSomeWork(TimerDrivenModuleContext timerDrivenModuleContext, GraphDatabaseService graphDatabaseService) {
        expirationStrategy.expireNodes();
        return new EmptyContext();
    }

    @Override
    public String getId() {
        return moduleId;
    }


    //Trivial Implementations

    @Override
    public void initialize(GraphDatabaseService graphDatabaseService) {
    }

    @Override
    public void reinitialize(GraphDatabaseService graphDatabaseService, TxDrivenModuleMetadata txDrivenModuleMetadata) {
    }

    @Override
    public void afterCommit(Object state) {

    }

    @Override
    public void afterRollback(Object state) {

    }

    @Override
    public void shutdown() {

    }


    private boolean hasUpdatedExpireProperty(Node previousNode, Node newNode) {
        String expirationIndex = config.getExpirationProperty();

        return previousNode.hasProperty(expirationIndex) != newNode.hasProperty(expirationIndex)
                || !previousNode.getProperty(expirationIndex).equals(newNode.getProperty(config.getExpirationProperty()));
    }
}
