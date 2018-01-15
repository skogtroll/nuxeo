/*
 * (C) Copyright 2017 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     bdelbosc
 */
package org.nuxeo.lib.stream.log.internals;

import java.io.Externalizable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.nuxeo.lib.stream.codec.Codec;
import org.nuxeo.lib.stream.log.LogAppender;
import org.nuxeo.lib.stream.log.LogManager;
import org.nuxeo.lib.stream.log.LogPartition;
import org.nuxeo.lib.stream.log.LogTailer;
import org.nuxeo.lib.stream.log.RebalanceListener;

public abstract class AbstractLogManager implements LogManager {
    protected final Map<String, CloseableLogAppender> appenders = new ConcurrentHashMap<>();

    protected final Map<LogPartitionGroup, LogTailer> tailersAssignments = new ConcurrentHashMap<>();

    // this define a concurrent set of tailers
    protected final Set<LogTailer> tailers = Collections.newSetFromMap(new ConcurrentHashMap<LogTailer, Boolean>());

    protected abstract void create(String name, int size);

    protected abstract <M extends Externalizable> CloseableLogAppender<M> createAppender(String name, Codec<M> codec);

    protected abstract <M extends Externalizable> LogTailer<M> doCreateTailer(Collection<LogPartition> partitions,
            String group, Codec<M> codec);

    protected abstract <M extends Externalizable> LogTailer<M> doSubscribe(String group, Collection<String> names,
            RebalanceListener listener, Codec<M> codec);

    @Override
    public synchronized boolean createIfNotExists(String name, int size) {
        if (!exists(name)) {
            create(name, size);
            return true;
        }
        return false;
    }

    @Override
    public boolean delete(String name) {
        return false;
    }

    @Override
    public <M extends Externalizable> LogTailer<M> createTailer(String group, Collection<LogPartition> partitions,
            Codec<M> codec) {
        partitions.forEach(partition -> checkInvalidAssignment(group, partition));
        partitions.forEach(partition -> checkInvalidCodec(partition, codec));
        LogTailer<M> ret = doCreateTailer(partitions, group, codec);
        partitions.forEach(partition -> tailersAssignments.put(new LogPartitionGroup(group, partition), ret));
        tailers.add(ret);
        return ret;
    }

    @Override
    public boolean supportSubscribe() {
        return false;
    }

    @Override
    public <M extends Externalizable> LogTailer<M> subscribe(String group, Collection<String> names,
            RebalanceListener listener, Codec<M> codec) {
        LogTailer<M> ret = doSubscribe(group, names, listener, codec);
        tailers.add(ret);
        return ret;
    }

    protected void checkInvalidAssignment(String group, LogPartition partition) {
        LogPartitionGroup key = new LogPartitionGroup(group, partition);
        LogTailer ret = tailersAssignments.get(key);
        if (ret != null && !ret.closed()) {
            throw new IllegalArgumentException(
                    "Tailer for this partition already created: " + partition + ", group: " + group);
        }
        if (!exists(partition.name())) {
            throw new IllegalArgumentException("Tailer with unknown Log name: " + partition.name());
        }
    }

    protected void checkInvalidCodec(LogPartition partition, Codec codec) {
        String codecClass = codec == null ? null : codec.getClass().getName();
        if (appenders.containsKey(partition.name())) {
            Codec appCodec = getAppender(partition.name()).getCodec();
            String appCodecClass = appCodec == null ? null : appCodec.getClass().getName();
            if (!Objects.equals(codecClass, appCodecClass)) {
                throw new IllegalArgumentException(
                        String.format("Try to tail on Log %s with different codec: %s instead of: %s", partition.name(),
                                appCodecClass, codecClass));
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized <M extends Externalizable> LogAppender<M> getAppender(String name, Codec<M> codec) {
        LogAppender<M> ret = (LogAppender<M>) appenders.computeIfAbsent(name, n -> {
            if (exists(n)) {
                return createAppender(n, codec);
            }
            throw new IllegalArgumentException("Unknown Log name: " + n);
        });
        if (codec == null) {
            // the requested codec is not known so no check
            return ret;
        }
        if (ret.getCodec() == null) {
            throw new IllegalArgumentException(String.format(
                    "The appender for Log %s exists with a default codec, cannot use a different codec: %s", name,
                    codec.getClass()));
        }
        if (!ret.getCodec().getClass().getName().equals(codec.getClass().getName())) {
            throw new IllegalArgumentException(
                    String.format("The appender for Log %s exists with a %s codec, cannot use a different codec: %s",
                            name, codec.getClass(), ret.getCodec().getClass()));
        }
        return ret;
    }

    @Override
    public void close() {
        appenders.values().stream().filter(Objects::nonNull).forEach(CloseableLogAppender::close);
        appenders.clear();
        tailers.stream().filter(Objects::nonNull).forEach(LogTailer::close);
        tailers.clear();
        tailersAssignments.clear();
    }

}
