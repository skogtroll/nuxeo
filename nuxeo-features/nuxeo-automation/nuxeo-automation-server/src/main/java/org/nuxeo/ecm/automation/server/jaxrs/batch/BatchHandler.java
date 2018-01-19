package org.nuxeo.ecm.automation.server.jaxrs.batch;

import java.util.Map;

public interface BatchHandler {
    String getName();
    Batch newBatch();
    Batch newBatch(String providedId);
    Batch getBatch(String batchId);
    void init(Map<String, String> configProperties);
}
