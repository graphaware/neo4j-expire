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

package com.graphaware.neo4j.lifecycle;

import com.graphaware.common.log.LoggerFactory;
import com.graphaware.common.util.Change;
import com.graphaware.neo4j.lifecycle.config.LifecycleConfiguration;
import com.graphaware.neo4j.lifecycle.indexer.expire.ExpirationIndexer;
import com.graphaware.neo4j.lifecycle.indexer.expire.LegacyExpirationIndexer;
import com.graphaware.neo4j.lifecycle.indexer.revive.LegacyRevivalIndexer;
import com.graphaware.neo4j.lifecycle.indexer.revive.RevivalIndexer;
import com.graphaware.neo4j.lifecycle.strategy.LifecycleStrategy;
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
public class LifecyleModule extends BaseTxDrivenModule<Void> implements TimerDrivenModule {

	private static final Log LOG = LoggerFactory.getLogger(LifecyleModule.class);

	private final ExpirationIndexer expirationIndexer;
	private final RevivalIndexer revivalIndexer;
	private final LifecycleConfiguration config;

	public LifecyleModule(String moduleId, GraphDatabaseService database, LifecycleConfiguration config) {
		super(moduleId);

		config.validate();

		this.expirationIndexer = new LegacyExpirationIndexer(database, config);
		this.revivalIndexer = new LegacyRevivalIndexer(database, config);
		this.config = config;
	}

	@Override
	public Void beforeCommit(ImprovedTransactionData td) throws DeliberateTransactionRollbackException {
		indexNewNodes(td);
		indexNewRels(td);
		indexChangedNodes(td);
		indexChangedRels(td);
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
					expirationIndexer.indexRelationship(r);
					revivalIndexer.indexRelationship(r);
				}
			}).execute();
		}

		if (config.getNodeExpirationIndex() != null) {
			LOG.info("Looking at all nodes to see if they have an expiry date or TTL...");

			new IterableInputBatchTransactionExecutor<>(database, batchSize, new AllNodes(database, batchSize), new UnitOfWork<Node>() {
				@Override
				public void execute(GraphDatabaseService database, Node n, int batchNumber, int stepNumber) {
					expirationIndexer.indexNode(n);
					revivalIndexer.indexNode(n);
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

		reviveRelationships(now);
		reviveNodes(now);
		expireRelationships(now);
		expireNodes(now);

		return new EmptyContext();
	}

	private void expireRelationships(long now) {
		int expired = 0;
		IndexHits<Relationship> relationshipsToExpire = expirationIndexer.candidateRelsExpiringBefore(now);
		if (relationshipsToExpire != null) {
			for (Relationship relationship : relationshipsToExpire) {
				if (expired < config.getMaxNoExpirations()) {
					LifecycleStrategy<Relationship> strategy = config.getRelationshipExpirationStrategy();
					boolean didExpire = strategy.applyIfNeeded(relationship, LifecycleEvent.EXPIRY);
					if (didExpire && strategy.removesFromIndex()) {
						expirationIndexer.removeRelationship(relationship);
					}
					expired++;
				} else {
					break;
				}
			 }
		}
	}

	private void expireNodes(long now) {
		int expired = 0;
		IndexHits<Node> nodesToExpire = expirationIndexer.candidateNodesExpiringBefore(now);
		if (nodesToExpire != null) {
			for (Node node : nodesToExpire) {
				if (expired < config.getMaxNoExpirations()) {
					LifecycleStrategy<Node> strategy = config.getNodeExpirationStrategy();
					boolean didExpire = strategy.applyIfNeeded(node, LifecycleEvent.EXPIRY );
					if (didExpire && strategy.removesFromIndex()) {
						expirationIndexer.removeNode(node);
					}
					else {
						System.out.println("nope!");
					}
					expired++;
				} else {
					break;
				}
			}
		}
	}

	private void reviveRelationships(long now) {
		int revived = 0;
		IndexHits<Relationship> relsToRevive = revivalIndexer.candidateRelsRevivingBefore(now);
		if (relsToRevive != null) {
			for (Relationship relationship : relsToRevive) {
				if (revived < config.getMaxNoExpirations()) {
					LifecycleStrategy<Relationship> strategy = config.getRelationshipRevivalStrategy();
					boolean didRevive = strategy.applyIfNeeded(relationship, LifecycleEvent.REVIVAL);
					if (didRevive && strategy.removesFromIndex()) {
						revivalIndexer.removeRelationship(relationship);
					}
					revived++;
				} else {
					break;
				}
			}
		}
	}

	private void reviveNodes(long now) {
		int revived = 0;
		IndexHits<Node> nodesToRevive = revivalIndexer.candidateNodesRevivingBefore(now);
		if (nodesToRevive != null) {
			for (Node node : nodesToRevive) {
				if (revived < config.getMaxNoExpirations()) {
					LifecycleStrategy<Node> strategy = config.getNodeRevivalStrategy();
					boolean didRevive = strategy.applyIfNeeded(node, LifecycleEvent.REVIVAL );
					if (didRevive && strategy.removesFromIndex()) {
						revivalIndexer.removeNode(node);
					}
					revived++;
				} else {
					break;
				}
			}
		}
	}

	private void indexNewNodes(ImprovedTransactionData td) {
		for (Node node : td.getAllCreatedNodes()) {
			expirationIndexer.indexNode(node);
			revivalIndexer.indexNode(node);
		}
	}

	private void indexNewRels(ImprovedTransactionData td) {
		for (Relationship relationship : td.getAllCreatedRelationships()) {
			expirationIndexer.indexRelationship(relationship);
			revivalIndexer.indexRelationship(relationship);
		}
	}

	private void indexChangedNodes(ImprovedTransactionData td) {
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

				expirationIndexer.removeNode(change.getPrevious());
				expirationIndexer.indexNode(current);
			}

			//TODO: Why are we indexing deleted props?
			String revivalProp = config.getNodeRevivalProperty();
			if (td.hasPropertyBeenCreated(current, revivalProp)
					|| td.hasPropertyBeenChanged(current, revivalProp)
					|| td.hasPropertyBeenDeleted(current, expProp)) {

				revivalIndexer.removeNode(change.getPrevious());
				revivalIndexer.indexNode(current);
			}


		}
	}

	private void indexChangedRels(ImprovedTransactionData td) {
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

				expirationIndexer.removeRelationship(change.getPrevious());
				expirationIndexer.indexRelationship(current);
			}

			//TODO: Why index deleted prop?
			String revivalProp = config.getRelationshipRevivalProperty();
			if (td.hasPropertyBeenCreated(current, revivalProp)
					|| td.hasPropertyBeenChanged(current, revivalProp)
					|| td.hasPropertyBeenDeleted(current, revivalProp)) {

				revivalIndexer.removeRelationship(change.getPrevious());
				revivalIndexer.indexRelationship(current);
			}
		}
	}
}
