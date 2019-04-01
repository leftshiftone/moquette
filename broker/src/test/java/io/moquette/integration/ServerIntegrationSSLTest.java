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
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertFalse;

/**
 * Check that Moquette could also handle SSL.
 */
public class ServerIntegrationSSLTest {

    private static final Logger LOG = LoggerFactory.getLogger(ServerIntegrationSSLTest.class);

    MoquetteServer server;
    static MqttClientPersistence s_dataStore;

    IMqttClient m_client;
    MessageCollector m_callback;

    static String backup;

    @BeforeClass
    public static void beforeTests() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        s_dataStore = new MqttDefaultFilePersistence(tmpDir);
        backup = System.getProperty("moquette.path");
    }

    @AfterClass
    public static void afterTests() {
        if (backup == null)
            System.clearProperty("moquette.path");
        else
            System.setProperty("moquette.path", backup);
    }

    protected void startServer() throws IOException {
        String file = getClass().getResource("/").getPath();
        System.setProperty("moquette.path", file);


        Properties sslProps = new Properties();
        sslProps.put(BrokerConstants.SSL_PORT_PROPERTY_NAME, "8883");
        sslProps.put(BrokerConstants.JKS_PATH_PROPERTY_NAME, "serverkeystore.jks");
        sslProps.put(BrokerConstants.KEY_STORE_PASSWORD_PROPERTY_NAME, "passw0rdsrv");
        sslProps.put(BrokerConstants.KEY_MANAGER_PASSWORD_PROPERTY_NAME, "passw0rdsrv");
        sslProps.put(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME, IntegrationUtils.localH2MvStoreDBPath());

        server = MoquetteServer.builder()
            .withConfiguration(sslProps)
            .build();
        server.start();
    }

    @Before
    public void setUp() throws Exception {
        String dbPath = IntegrationUtils.localH2MvStoreDBPath();
        File dbFile = new File(dbPath);
        assertFalse(String.format("The DB storagefile %s already exists", dbPath), dbFile.exists());

        startServer();

        m_client = new MqttClient("ssl://localhost:8883", "TestClient", s_dataStore);
        // m_client = new MqttClient("ssl://test.mosquitto.org:8883", "TestClient", s_dataStore);

        m_callback = new MessageCollector();
        m_client.setCallback(m_callback);
    }

    @After
    public void tearDown() throws Exception {
        if (m_client != null && m_client.isConnected()) {
            m_client.disconnect();
        }

        if (server != null) {
            server.stop();
        }
        IntegrationUtils.clearTestStorage();
    }

    @Test
    public void checkSupportSSL() throws Exception {
        LOG.info("*** checkSupportSSL ***");
        SSLSocketFactory ssf = configureSSLSocketFactory();

        MqttConnectOptions options = new MqttConnectOptions();
        options.setSocketFactory(ssf);
        m_client.connect(options);
        m_client.subscribe("/topic", 0);
        m_client.disconnect();
    }

    @Test
    public void checkSupportSSLForMultipleClient() throws Exception {
        LOG.info("*** checkSupportSSLForMultipleClient ***");
        SSLSocketFactory ssf = configureSSLSocketFactory();

        MqttConnectOptions options = new MqttConnectOptions();
        options.setSocketFactory(ssf);
        m_client.connect(options);
        m_client.subscribe("/topic", 0);

        MqttClient secondClient = new MqttClient("ssl://localhost:8883", "secondTestClient", new MemoryPersistence());
        MqttConnectOptions secondClientOptions = new MqttConnectOptions();
        secondClientOptions.setSocketFactory(ssf);
        secondClient.connect(secondClientOptions);
        secondClient.publish("/topic", new MqttMessage("message".getBytes(UTF_8)));
        secondClient.disconnect();

        m_client.disconnect();
    }

    /**
     * keystore generated into test/resources with command:
     *
     * keytool -keystore clientkeystore.jks -alias testclient -genkey -keyalg RSA -> mandatory to
     * put the name surname -> password is passw0rd -> type yes at the end
     *
     * to generate the crt file from the keystore -- keytool -certreq -alias testclient -keystore
     * clientkeystore.jks -file testclient.csr
     *
     * keytool -export -alias testclient -keystore clientkeystore.jks -file testclient.crt
     *
     * to import an existing certificate: keytool -keystore clientkeystore.jks -import -alias
     * testclient -file testclient.crt -trustcacerts
     */
    private SSLSocketFactory configureSSLSocketFactory() throws KeyManagementException, NoSuchAlgorithmException,
            UnrecoverableKeyException, IOException, CertificateException, KeyStoreException {
        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream jksInputStream = getClass().getClassLoader().getResourceAsStream("clientkeystore.jks");
        ks.load(jksInputStream, "passw0rd".toCharArray());

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, "passw0rd".toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);

        SSLContext sc = SSLContext.getInstance("TLS");
        TrustManager[] trustManagers = tmf.getTrustManagers();
        sc.init(kmf.getKeyManagers(), trustManagers, null);

        SSLSocketFactory ssf = sc.getSocketFactory();
        return ssf;
    }
}
