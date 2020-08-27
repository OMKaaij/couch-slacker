/*
 * Copyright 2014-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.groocraft.couchdb.slacker;

import com.groocraft.couchdb.slacker.configuration.CouchDbProperties;
import com.groocraft.couchdb.slacker.http.ThrowingInterceptor;
import com.groocraft.couchdb.slacker.http.TrustAllStrategy;
import org.apache.http.Consts;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Builder for {@link CouchDbClient}. The client can be builder manually thru {@link #url(String)}, {@link #username(String)} and {@link #password(String)}
 * (String)} methods, or from {@link #properties(CouchDbProperties)}.
 *
 * @author Majlanky
 */
public class CouchDbClientBuilder {

    private CouchDbProperties properties;
    private Supplier<String> uidGenerator = () -> UUID.randomUUID().toString();

    CouchDbClientBuilder() {
        properties = new CouchDbProperties();
    }

    /**
     * Method to set all needed properties from {@link CouchDbProperties} instance
     *
     * @param properties initialized instance of {@link CouchDbProperties}. Must not be {@literal null}
     * @return {@link CouchDbClientBuilder}
     */
    public CouchDbClientBuilder properties(CouchDbProperties properties) {
        this.properties.copy(properties);
        return this;
    }

    /**
     * Method to set url of database
     *
     * @param url must be valid by URI rules. Must not be {@literal null}
     * @return {@link CouchDbClientBuilder}
     */
    public CouchDbClientBuilder url(String url) {
        this.properties.setUrl(url);
        return this;
    }

    /**
     * Method to set username used to authenticate to database
     *
     * @param username Must not be {@literal null}
     * @return {@link CouchDbClientBuilder}
     */
    public CouchDbClientBuilder username(String username) {
        this.properties.setUsername(username);
        return this;
    }

    /**
     * Method to set password used to authenticate to database
     *
     * @param password Must not be {@literal null}
     * @return {@link CouchDbClientBuilder}
     */
    public CouchDbClientBuilder password(String password) {
        this.properties.setUsername(password);
        return this;
    }

    /**
     * Method to set generator of UID for documents in DB.
     *
     * @param uidGenerator Must not be {@literal null}
     * @return {@link CouchDbClientBuilder}
     */
    public CouchDbClientBuilder uidGenerator(Supplier<String> uidGenerator) {
        this.uidGenerator = uidGenerator;
        return this;
    }

    /**
     * Method to build new instance of {@link CouchDbClient} by the given setting.
     *
     * @return {@link CouchDbClient}
     */
    public CouchDbClient build() {
        try {
            URI uri = new URI(properties.getUrl());
            HttpHost host = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
            HttpContext context = getHttpContext(host);
            HttpClient client = getHttpClient();
            return new CouchDbClient(client, host, context, uri, uidGenerator);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid url", e);
        }
    }

    /**
     * Method to create basic context with given {@link HttpContext} with basic authentication and cache. Authentication using
     * {@link CouchDbProperties#getUsername()} nad {@link CouchDbProperties#getPassword()} as credentials.
     *
     * @param host Must not be {@literal null}
     * @return {@link HttpContext}
     */
    private HttpContext getHttpContext(HttpHost host) {
        AuthCache authCache = new BasicAuthCache();
        authCache.put(host, new BasicScheme());
        HttpContext context = new BasicHttpContext();
        context.setAttribute(HttpClientContext.AUTH_CACHE, authCache);
        context.setAttribute(HttpClientContext.CREDS_PROVIDER, getCredentialProvider(properties.getUsername(), properties.getPassword()));
        return context;
    }

    /**
     * Method to create {@link HttpClient} with ability to connect to http and https, default UTF8, 0 retry when fault and interceptor which throws exception
     * when not OK response
     *
     * @return {@link HttpClient}
     * @see ThrowingInterceptor
     */
    public HttpClient getHttpClient() {
        try {
            PoolingHttpClientConnectionManager ccm = new PoolingHttpClientConnectionManager(getRegistry());
            HttpClientBuilder clientBuilder = HttpClients.custom()
                    .setConnectionManager(ccm)
                    .setDefaultConnectionConfig(ConnectionConfig.custom()
                            .setCharset(Consts.UTF_8).build())
                    .setDefaultRequestConfig(RequestConfig.DEFAULT)
                    .addInterceptorFirst(new ThrowingInterceptor())
                    .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
            return clientBuilder.build();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create HTTP client", e);
        }
    }

    /**
     * Method to configure and get {@link Registry} of {@link ConnectionSocketFactory} for http and https. In case of https all certificated are trusted.
     *
     * @return {@link Registry}
     * @throws KeyStoreException        if unable to load {@link TrustAllStrategy}
     * @throws NoSuchAlgorithmException if unable to load {@link TrustAllStrategy}
     * @throws KeyManagementException   if unable to build {@link TrustAllStrategy}
     */
    private Registry<ConnectionSocketFactory> getRegistry() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        // @formatter:off
        return RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", new SSLConnectionSocketFactory(SSLContexts.custom().loadTrustMaterial(new TrustAllStrategy()).build(),
                        new NoopHostnameVerifier()))
                .register("http", PlainConnectionSocketFactory.INSTANCE).build();
        // @formatter:on
    }

    /**
     * Method to get configured {@link CredentialsProvider} which is using {@code username} and {@code password} read from {@link CouchDbProperties}
     *
     * @param username of a valid CouchDB user which is used to authenticate during every request. Must not be {@literal null}
     * @param password of a valid CouchDB user which is used to authenticate during every request
     * @return prepared {@link CredentialsProvider}
     */
    private CredentialsProvider getCredentialProvider(String username, String password) {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                new UsernamePasswordCredentials(username, password));
        return credentialsProvider;
    }


}