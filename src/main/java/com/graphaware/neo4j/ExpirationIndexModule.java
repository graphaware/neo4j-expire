package com.graphaware.neo4j;

import com.graphaware.common.util.Change;
import com.graphaware.neo4j.config.ExpirationConfiguration;
import com.graphaware.neo4j.indexer.ExpirationIndexer;
import com.graphaware.neo4j.strategies.ExpirationStrategy;
import com.graphaware.runtime.config.BaseTxAndTimerDrivenModuleConfiguration;
import com.graphaware.runtime.metadata.EmptyContext;
import com.graphaware.runtime.metadata.TimerDrivenModuleContext;
import com.graphaware.runtime.metadata.TxDrivenModuleMetadata;
import com.graphaware.runtime.module.*;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.util.Date;

public class ExpirationIndexModule implements TxDrivenModule, TimerDrivenModule {

    private final ExpirationIndexer indexer;
    private final ExpirationConfiguration config;
    private final NodeDeleter deleter;
    private final String moduleId;

    private final ExpirationStrategy expirationStrategy;

    protected ExpirationIndexModule(String moduleId, ExpirationIndexer indexer, ExpirationConfiguration config, NodeDeleter deleter, ExpirationStrategy expirationStrategy) {
        this.moduleId = moduleId;
        this.indexer = indexer;
        this.config = config;
        this.deleter = deleter;
        this.expirationStrategy = expirationStrategy;
    }

    private boolean hasUpdatedExpireProperty(Node previousNode, Node newNode) {
        String expirationIndex = config.getExpirationProperty();

        return previousNode.hasProperty(expirationIndex) != newNode.hasProperty(expirationIndex)
                || !previousNode.getProperty(expirationIndex).equals(newNode.getProperty(config.getExpirationProperty()));
    }

    @Override
    public void start(GraphDatabaseService graphDatabaseService) {

    }

    @Override
    public void initialize(GraphDatabaseService graphDatabaseService) {

    }

    @Override
    public void reinitialize(GraphDatabaseService graphDatabaseService, TxDrivenModuleMetadata txDrivenModuleMetadata) {

    }

    @Override
    public Void beforeCommit(ImprovedTransactionData improvedTransactionData) throws DeliberateTransactionRollbackException {
        for (Node node : improvedTransactionData.getAllCreatedNodes()) {
            indexer.indexNode(node);
        }

        for (Node node : improvedTransactionData.getAllDeletedNodes()) {
            indexer.deleteNode(node);
        }

        for (Change<Node> change : improvedTransactionData.getAllChangedNodes()) {
            if (hasUpdatedExpireProperty(change.getPrevious(), change.getCurrent())) {
                indexer.deleteNode(change.getPrevious());
                indexer.indexNode(change.getCurrent());
            }
        }

        return null;
    }

    @Override
    public void afterCommit(Object o) {
    }

    @Override
    public void afterRollback(Object o) {
    }

    @Override
    public BaseTxAndTimerDrivenModuleConfiguration getConfiguration() {
        return ExpirationConfiguration.defaultConfiguration();
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

    @Override
    public void shutdown() {

    }
}
