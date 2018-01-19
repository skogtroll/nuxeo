package org.nuxeo.ecm.automation.server.jaxrs.batch.handler;

import org.nuxeo.ecm.automation.server.jaxrs.batch.Batch;
import org.nuxeo.ecm.automation.server.jaxrs.batch.BatchHandler;

import java.util.Map;

public abstract class AbstractBatchHandler implements BatchHandler {

    private String name;

    public AbstractBatchHandler(String name) {
        this.name = name;
    }

    @Override public String getName() {
        return name;
    }

    @Override public abstract Batch newBatch();

    @Override public abstract Batch getBatch(String batchId);

    @Override public abstract Batch newBatch(String providedId);

    @Override public void init(Map<String, String> configProperties) {

    }
}
