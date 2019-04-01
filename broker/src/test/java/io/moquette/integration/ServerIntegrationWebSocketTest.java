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

import io.moquette.BrokerConstants;
import io.moquette.broker.MoquetteServer;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.MemoryConfig;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

/**
 * Integration test to check the function of Moquette with a WebSocket channel.
 */
public class ServerIntegrationWebSocketTest {

    private static final Logger LOG = LoggerFactory.getLogger(ServerIntegrationWebSocketTest.class);

    MoquetteServer m_server;
    WebSocketClient client;
    IConfig m_config;

    protected void startServer() throws IOException {
        final Properties configProps = IntegrationUtils.prepareTestProperties();
        configProps
            .put(BrokerConstants.WEB_SOCKET_PORT_PROPERTY_NAME, Integer.toString(BrokerConstants.WEBSOCKET_PORT));
        m_config = new MemoryConfig(configProps);
        m_server = MoquetteServer.builder()
            .withConfiguration(m_config)
            .build();
        m_server.start();
    }

    @Before
    public void setUp() throws Exception {
        startServer();
        client = new WebSocketClient();
    }

    @After
    public void tearDown() throws Exception {
        client.stop();
        m_server.stop();
        IntegrationUtils.clearTestStorage();
    }

    @SuppressWarnings("FutureReturnValueIgnored")
    @Test
    public void checkPlainConnect() throws Exception {
        LOG.info("*** checkPlainConnect ***");
        String destUri = "ws://localhost:" + BrokerConstants.WEBSOCKET_PORT + BrokerConstants.WEBSOCKET_PATH;

        MQTTWebSocket socket = new MQTTWebSocket();
        client.start();
        URI echoUri = new URI(destUri);
        ClientUpgradeRequest request = new ClientUpgradeRequest();
        client.connect(socket, echoUri, request);
        LOG.info("Connecting to : {}", echoUri);
        boolean connected = socket.awaitConnected(4, TimeUnit.SECONDS);
        LOG.info("Connected was : {}", connected);

        assertTrue(connected);
    }
}
