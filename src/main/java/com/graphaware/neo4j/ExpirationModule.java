package com.graphaware.neo4j;

import com.graphaware.neo4j.indexer.ExpirationIndexer;
import com.graphaware.runtime.module.BaseTxDrivenModule;
import com.graphaware.runtime.module.DeliberateTransactionRollbackException;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import org.neo4j.graphdb.Node;

public class ExpirationModule extends BaseTxDrivenModule {

    private final ExpirationIndexer indexer;

    protected ExpirationModule(String moduleId, ExpirationIndexer indexer) {
        super(moduleId);
        this.indexer = indexer;
    }

    @Override
    public Void beforeCommit(ImprovedTransactionData improvedTransactionData) throws DeliberateTransactionRollbackException {
        for(Node node : improvedTransactionData.getAllCreatedNodes()) {
            indexer.indexNode(node);
        }

        return null;
    }
}
