package com.graphaware.neo4j;

import com.graphaware.common.util.Change;
import com.graphaware.neo4j.config.ExpirationConfiguration;
import com.graphaware.neo4j.indexer.ExpirationIndexer;
import com.graphaware.runtime.module.BaseTxDrivenModule;
import com.graphaware.runtime.module.DeliberateTransactionRollbackException;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import org.neo4j.graphdb.Node;

public class ExpirationModule extends BaseTxDrivenModule {

    private final ExpirationIndexer indexer;
    private final ExpirationConfiguration config;

    protected ExpirationModule(String moduleId, ExpirationIndexer indexer, ExpirationConfiguration config) {
        super(moduleId);
        this.indexer = indexer;
        this.config = config;
    }

    @Override
    public Void beforeCommit(ImprovedTransactionData improvedTransactionData) throws DeliberateTransactionRollbackException {
        for(Node node : improvedTransactionData.getAllCreatedNodes()) {
            indexer.indexNode(node);
        }

        for(Node node : improvedTransactionData.getAllDeletedNodes()) {
            indexer.deleteNode(node);
        }

        for(Change<Node> change : improvedTransactionData.getAllChangedNodes()) {
            if (hasUpdatedExpireProperty(change.getPrevious(), change.getCurrent())) {
                indexer.deleteNode(change.getPrevious());
                indexer.indexNode(change.getCurrent());
            }
        }

        return null;
    }

    private boolean hasUpdatedExpireProperty(Node previousNode, Node newNode) {
        String expirationIndex = config.getExpirationProperty();

        return previousNode.hasProperty(expirationIndex) != newNode.hasProperty(expirationIndex)
                || !previousNode.getProperty(expirationIndex).equals(newNode.getProperty(config.getExpirationProperty()));
    }
}
