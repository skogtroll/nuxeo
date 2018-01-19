package org.nuxeo.ecm.automation.server.jaxrs.batch.handler.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.server.jaxrs.batch.Batch;
import org.nuxeo.ecm.automation.server.jaxrs.batch.handler.AbstractBatchHandler;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.transientstore.api.TransientStore;
import org.nuxeo.ecm.core.transientstore.api.TransientStoreService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.services.config.ConfigurationService;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DefaultBatchHandlerImpl extends AbstractBatchHandler {

    protected static final Log log = LogFactory.getLog(DefaultBatchHandlerImpl.class);

    protected static final String BATCH_HANDLER_NAME = "default";
    protected static final String CLIENT_BATCH_ID_FLAG = "allowClientGeneratedBatchId";


    private static class Config {
        public static final String TRANSIENT_STORE_NAME = "transientStore";
    }

    private TransientStore transientStore;
    private TransientStoreService transientStoreService;

    public DefaultBatchHandlerImpl() {
        super(BATCH_HANDLER_NAME);
    }

    @Override public Batch newBatch() {
        return initBatch();
    }

    protected Batch initBatch() {
        return initBatch(null);
    }

    protected Batch initBatch(String batchId) {
        if (StringUtils.isEmpty(batchId)) {
            batchId = "batchId-" + UUID.randomUUID().toString();
        } else if (!Framework.getService(ConfigurationService.class).isBooleanPropertyTrue(CLIENT_BATCH_ID_FLAG)) {
            throw new NuxeoException(String.format(
                    "Cannot initialize upload batch with a given id since configuration property %s is not set to true",
                    CLIENT_BATCH_ID_FLAG));
        }

        // That's the way of storing an empty entry
        log.debug("Initializing batch with id " + batchId);
        transientStore.setCompleted(batchId, false);
        transientStore.putParameter(batchId, "provider", getName());
        return new Batch(batchId);
    }

    @Override public Batch newBatch(String providedId) {
        return initBatch(providedId);
    }

    @Override public Batch getBatch(String batchId) {
        Map<String, Serializable> batchEntryParams = transientStore.getParameters(batchId);

        if (batchEntryParams == null) {
            if (!hasBatch(batchId)) {
                return null;
            }
            batchEntryParams = new HashMap<>();
        }

        if (getName().equalsIgnoreCase(batchEntryParams.getOrDefault("provider", getName()).toString())) {
            return new Batch(getName(), batchId, batchEntryParams, this);
        }

        return null;
    }

    private boolean hasBatch(String batchId) {
        return !StringUtils.isEmpty(batchId) && transientStore.exists(batchId);
    }

    @Override public void init(Map<String, String> configProperties) {
        if (!containsRequired(configProperties)) {
            throw new NuxeoException();
        }

        transientStoreService = Framework.getService(TransientStoreService.class);
        transientStore = transientStoreService.getStore(configProperties.get(Config.TRANSIENT_STORE_NAME));

        super.init(configProperties);
    }

    private boolean containsRequired(Map<String, String> configProperties) {
        if (configProperties.containsKey(Config.TRANSIENT_STORE_NAME)) {
            return false;
        }

        return true;
    }


}
