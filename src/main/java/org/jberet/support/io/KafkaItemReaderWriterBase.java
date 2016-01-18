/*
 * Copyright (c) 2016 Red Hat, Inc. and/or its affiliates.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Cheng Fang - Initial API and implementation
 */

package org.jberet.support.io;

import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;
import javax.batch.api.BatchProperty;
import javax.inject.Inject;

/**
 * Base class for {@link KafkaItemReader} and {@link KafkaItemWriter}.
 *
 * @since 1.3.0
 */
public abstract class KafkaItemReaderWriterBase {
    final static char topicPartitionDelimiter = ':';

    /**
     * The file path or URL to the Kafka configuration properties file. See Kafka docs for valid keys and values.
     *
     * @see "org.apache.kafka.clients.consumer.ConsumerConfig"
     * @see "org.apache.kafka.clients.producer.ProducerConfig"
     */
    @Inject
    @BatchProperty
    protected String configFile;

    public Serializable checkpointInfo() throws Exception {
        return null;
    }

    protected Properties createConfigProperties() throws IOException {
        final Properties configProps = new Properties();
        if (configFile != null) {
            configProps.load(ItemReaderWriterBase.getInputStream(configFile, false));
        }
        return configProps;
    }
}
