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

package com.graphaware.neo4j.expire;

import com.graphaware.common.log.LoggerFactory;
import com.graphaware.common.util.Change;
import com.graphaware.neo4j.expire.config.ExpirationConfiguration;
import com.graphaware.neo4j.expire.indexer.ExpirationIndexer;
import com.graphaware.neo4j.expire.indexer.LegacyExpirationIndexer;
import com.graphaware.runtime.config.BaseTxAndTimerDrivenModuleConfiguration;
import com.graphaware.runtime.metadata.EmptyContext;
import com.graphaware.runtime.metadata.TimerDrivenModuleContext;
import com.graphaware.runtime.module.BaseTxDrivenModule;
import com.graphaware.runtime.module.DeliberateTransactionRollbackException;
import com.graphaware.runtime.module.TimerDrivenModule;
import com.graphaware.runtime.module.TxDrivenModule;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import com.graphaware.tx.executor.batch.IterableInputBatchTransactionExecutor;
import com.graphaware.tx.executor.batch.UnitOfWork;
import com.graphaware.tx.executor.input.AllNodes;
import com.graphaware.tx.executor.input.AllRelationships;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.logging.Log;

/**
 * A {@link TxDrivenModule} and a {@link TimerDrivenModule} that allows for setting an expiry date or ttl on nodes
 * and relationships and deletes them when they have reached that date.
 */
public class ExpirationModule extends BaseTxDrivenModule<Void> implements TimerDrivenModule {

    private static final Log LOG = LoggerFactory.getLogger(ExpirationModule.class);

    private final ExpirationIndexer indexer;
    private final ExpirationConfiguration config;

    public ExpirationModule(String moduleId, GraphDatabaseService database, ExpirationConfiguration config) {
        super(moduleId);

        config.validate();

        this.indexer = new LegacyExpirationIndexer(database, config);
        this.config = config;
    }

    @Override
    public Void beforeCommit(ImprovedTransactionData td) throws DeliberateTransactionRollbackException {
        for (Node node : td.getAllCreatedNodes()) {
            indexer.indexNode(node);
        }

        for (Relationship relationship : td.getAllCreatedRelationships()) {
            indexer.indexRelationship(relationship);
        }

        for (Change<Node> change : td.getAllChangedNodes()) {
            Node current = change.getCurrent();
            String expProp = config.getNodeExpirationProperty();
            String ttlProp = config.getNodeTtlProperty();

            if (td.hasPropertyBeenCreated(current, expProp)
                    || td.hasPropertyBeenCreated(current, ttlProp)
                    || td.hasPropertyBeenChanged(current, expProp)
                    || td.hasPropertyBeenChanged(current, ttlProp)
                    || td.hasPropertyBeenDeleted(current, expProp)
                    || td.hasPropertyBeenDeleted(current, ttlProp)) {

                indexer.removeNode(change.getPrevious());
                indexer.indexNode(current);
            }
        }

        for (Change<Relationship> change : td.getAllChangedRelationships()) {
            Relationship current = change.getCurrent();
            String expProp = config.getRelationshipExpirationProperty();
            String ttlProp = config.getRelationshipTtlProperty();

            if (td.hasPropertyBeenCreated(current, expProp)
                    || td.hasPropertyBeenCreated(current, ttlProp)
                    || td.hasPropertyBeenChanged(current, expProp)
                    || td.hasPropertyBeenChanged(current, ttlProp)
                    || td.hasPropertyBeenDeleted(current, expProp)
                    || td.hasPropertyBeenDeleted(current, ttlProp)) {

                indexer.removeRelationship(change.getPrevious());
                indexer.indexRelationship(current);
            }
        }

        return null;
    }

    @Override
    public void initialize(GraphDatabaseService database) {
        int batchSize = 1000;

        if (config.getRelationshipExpirationIndex() != null) {
            LOG.info("Looking at all relationships to see if they have an expiry date or TTL...");

            new IterableInputBatchTransactionExecutor<>(database, batchSize, new AllRelationships(database, batchSize), new UnitOfWork<Relationship>() {
                @Override
                public void execute(GraphDatabaseService database, Relationship r, int batchNumber, int stepNumber) {
                    indexer.indexRelationship(r);
                }
            }).execute();
        }

        if (config.getNodeExpirationIndex() != null) {
            LOG.info("Looking at all nodes to see if they have an expiry date or TTL...");

            new IterableInputBatchTransactionExecutor<>(database, batchSize, new AllNodes(database, batchSize), new UnitOfWork<Node>() {
                @Override
                public void execute(GraphDatabaseService database, Node n, int batchNumber, int stepNumber) {
                    indexer.indexNode(n);
                }
            }).execute();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BaseTxAndTimerDrivenModuleConfiguration getConfiguration() {
        return config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimerDrivenModuleContext createInitialContext(GraphDatabaseService graphDatabaseService) {
        return new EmptyContext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimerDrivenModuleContext doSomeWork(TimerDrivenModuleContext timerDrivenModuleContext, GraphDatabaseService graphDatabaseService) {
        long now = System.currentTimeMillis();
        int expired = 0;

        IndexHits<Relationship> relationshipsToExpire = indexer.relationshipsExpiringBefore(now);
        if (relationshipsToExpire != null) {
            for (Relationship relationship : relationshipsToExpire) {
                if (expired < config.getMaxNoExpirations()) {
                    config.getRelationshipExpirationStrategy().expire(relationship);
                    expired++;
                } else {
                    break;
                }
            }
        }

        IndexHits<Node> nodesToExpire = indexer.nodesExpiringBefore(now);
        if (nodesToExpire != null) {
            for (Node node : nodesToExpire) {
                if (expired < config.getMaxNoExpirations()) {
                    config.getNodeExpirationStrategy().expire(node);
                    expired++;
                } else {
                    break;
                }
            }
        }

        return new EmptyContext();
    }
}
