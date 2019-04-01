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
import io.moquette.broker.security.DBAuthenticator;
import io.moquette.broker.security.DBAuthenticatorTest;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public class ServerIntegrationDBAuthenticatorTest {

    private static final Logger LOG = LoggerFactory.getLogger(ServerIntegrationDBAuthenticatorTest.class);

    static MqttClientPersistence dataStore;
    static MqttClientPersistence pubDataStore;
    static DBAuthenticatorTest dbAuthenticatorTest;

    MoquetteServer server;
    IMqttClient client;
    IMqttClient publisher;
    MessageCollector messageCollector;
    IConfig config;

    @BeforeClass
    public static void beforeTests() throws NoSuchAlgorithmException, SQLException, ClassNotFoundException {
        String tmpDir = System.getProperty("java.io.tmpdir");
        dataStore = new MqttDefaultFilePersistence(tmpDir);
        pubDataStore = new MqttDefaultFilePersistence(tmpDir + File.separator + "publisher");
        dbAuthenticatorTest = new DBAuthenticatorTest();
        dbAuthenticatorTest.setup();
    }

    protected void startServer() {
        final Properties configProps = addDBAuthenticatorConf(IntegrationUtils.prepareTestProperties());
        this.server = MoquetteServer.builder().withConfiguration(configProps).build();
        this.server.start();
    }

    private void stopServer() {
        server.stop();
    }

    private Properties addDBAuthenticatorConf(Properties properties) {
        properties.put(BrokerConstants.AUTHENTICATOR_CLASS_NAME, DBAuthenticator.class.getCanonicalName());
        properties.put(BrokerConstants.DB_AUTHENTICATOR_DRIVER, DBAuthenticatorTest.ORG_H2_DRIVER);
        properties.put(BrokerConstants.DB_AUTHENTICATOR_URL, DBAuthenticatorTest.JDBC_H2_MEM_TEST);
        properties.put(BrokerConstants.DB_AUTHENTICATOR_QUERY, "SELECT PASSWORD FROM ACCOUNT WHERE LOGIN=?");
        properties.put(BrokerConstants.DB_AUTHENTICATOR_DIGEST, DBAuthenticatorTest.SHA_256);
        return properties;
    }

    @Before
    public void setUp() throws Exception {
        startServer();

        client = new MqttClient("tcp://localhost:1883", "TestClient", dataStore);
        messageCollector = new MessageCollector();
        client.setCallback(messageCollector);

        publisher = new MqttClient("tcp://localhost:1883", "Publisher", pubDataStore);
    }

    @After
    public void tearDown() throws Exception {
        if (client != null && client.isConnected()) {
            client.disconnect();
        }

        if (publisher != null && publisher.isConnected()) {
            publisher.disconnect();
        }

        stopServer();

        IntegrationUtils.clearTestStorage();
    }

    @AfterClass
    public static void shutdown() {
        dbAuthenticatorTest.teardown();
    }

    @Test
    public void connectWithValidCredentials() throws Exception {
        LOG.info("*** connectWithCredentials ***");
        client = new MqttClient("tcp://localhost:1883", "Publisher", pubDataStore);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName("dbuser");
        options.setPassword("password".toCharArray());
        client.connect(options);
        assertTrue(true);
    }

    @Test
    public void connectWithWrongCredentials() {
        LOG.info("*** connectWithWrongCredentials ***");
        try {
            client = new MqttClient("tcp://localhost:1883", "Publisher", pubDataStore);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName("dbuser");
            options.setPassword("wrongPassword".toCharArray());
            client.connect(options);
        } catch (MqttException e) {
            if (e instanceof MqttSecurityException) {
                assertTrue(true);
                return;
            } else {
                assertTrue(e.getMessage(), false);
                return;
            }
        }
        assertTrue("must not be connected. cause : wrong password given to client", false);
    }

}
