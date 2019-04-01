/*
 * Copyright (c) 2012-2018 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License v2.0 is available at
 * http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package io.moquette.integration;

import io.moquette.broker.MoquetteServer;
import org.fusesource.mqtt.client.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ServerIntegrationFuseTest {

    private static final Logger LOG = LoggerFactory.getLogger(ServerIntegrationPahoTest.class);

    MoquetteServer server;
    MQTT mqtt;
    BlockingConnection subscriber;
    BlockingConnection publisher;

    protected void startServer() throws IOException {
        this.server = MoquetteServer.builder()
            .withConfiguration(IntegrationUtils.prepareTestProperties())
            .build();
        this.server.start();
    }

    @Before
    public void setUp() throws Exception {
        startServer();

        mqtt = new MQTT();
        mqtt.setHost("localhost", 1883);
    }

    @After
    public void tearDown() throws Exception {
        if (subscriber != null) {
            subscriber.disconnect();
        }

        if (publisher != null) {
            publisher.disconnect();
        }

        server.stop();
        IntegrationUtils.clearTestStorage();
    }

    @Test
    public void checkWillTestamentIsPublishedOnConnectionKill_noRetain() throws Exception {
        LOG.info("checkWillTestamentIsPublishedOnConnectionKill_noRetain");

        String willTestamentTopic = "/will/test";
        String willTestamentMsg = "Bye bye";

        MQTT mqtt = new MQTT();
        mqtt.setHost("localhost", 1883);
        mqtt.setClientId("WillTestamentPublisher");
        mqtt.setWillRetain(false);
        mqtt.setWillMessage(willTestamentMsg);
        mqtt.setWillTopic(willTestamentTopic);
        publisher = mqtt.blockingConnection();
        publisher.connect();

        this.mqtt.setHost("localhost", 1883);
        this.mqtt.setCleanSession(false);
        this.mqtt.setClientId("Subscriber");
        subscriber = this.mqtt.blockingConnection();
        subscriber.connect();
        Topic[] topics = new Topic[]{new Topic(willTestamentTopic, QoS.AT_MOST_ONCE)};
        subscriber.subscribe(topics);

        // Exercise, kill the publisher connection
        publisher.kill();

        // Verify, that the testament is fired
        Message msg = subscriber.receive(1, TimeUnit.SECONDS); // wait the flush interval (1 sec)
        assertNotNull("We should get notified with 'Will' message", msg);
        msg.ack();
        assertEquals(willTestamentMsg, new String(msg.getPayload(), UTF_8));
    }
}
