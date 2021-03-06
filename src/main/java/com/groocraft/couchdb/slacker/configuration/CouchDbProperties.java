/*
 * Copyright 2020 the original author or authors.
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

package com.groocraft.couchdb.slacker.configuration;

import com.groocraft.couchdb.slacker.QueryStrategy;
import com.groocraft.couchdb.slacker.SchemaOperation;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

/**
 * Properties pojo class for Couch Slacker configuration. It is used in {@link CouchSlackerConfiguration} class
 *
 * @author Majlanky
 * @see CouchSlackerConfiguration
 */
@Validated
@ConfigurationProperties(prefix = "couchdb.client")
public class CouchDbProperties {

    public static final String COUCH_ID_NAME = "_id";
    public static final String COUCH_REVISION_NAME = "_rev";

    /**
     * Must be valid URL.
     * Port is mandatory.
     * Scheme can be http or https.
     * Scheme is not mandatory, http is used if not present.
     */
    @URL
    private String url;

    /**
     * Name of user used for authentication to CouchDB.
     * Must not be empty.
     */
    @NotEmpty
    private String username;

    /**
     * Password for name used for authentication to CouchDB.
     * Must not be empty.
     */
    @NotEmpty
    private String password;

    /**
     * If bulk operation (findAll, deleteAll, etc.) is executed, this is the limit of document number processed in one batch.
     * Operation requesting operation with more documents than limit is processed in more batches.
     * Minimum is 10, maximum is 100000.
     * Default value is 10000.
     */
    @Min(10)
    @Max(100000)
    private int bulkMaxSize = 10000;

    /**
     * Flag which can turn on/off execution stats in the _find query result.
     * The stats are logged if turned on.
     * Default value is false.
     */
    private boolean findExecutionStats = false;

    /**
     * Schema operation is done before a usage of a database. Couch Slacker read all defined document mapping which gives list of used databases. Depending on
     * the configured operation validation that databases in the list exists(validate),
     * creates databases from the list(create),
     * delete and create databases from the list(drop)
     * or no operation is done (none).
     * With what parameters a database is created depends on Database annotation values or the following three values default-shards, default-replicas,
     * default-partitioned.
     * Default value is validate.
     */
    private SchemaOperation schemaOperation = SchemaOperation.VALIDATE;

    /**
     * If database is created by Couch Slacker, this value says number of shards.
     * Default value is 8
     */
    private int defaultShards = 8;

    /**
     * If database is created by Couch Slacker, this value says number of replicas.
     * Default value is 3
     */
    private int defaultReplicas = 3;

    /**
     * If database is created by Couch Slacker, this value says if it should be partitioned
     * Default value is false
     */
    private boolean defaultPartitioned = false;

    /**
     * Query strategy defines how query methods are executed. If mango is used, query methods are parsed to mango query and process standard CouchDB way. If
     * "view" is used, Couch Slacker will define view with matching rules for every query to speed up query time.
     */
    private QueryStrategy queryStrategy = QueryStrategy.MANGO;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getBulkMaxSize() {
        return bulkMaxSize;
    }

    public void setBulkMaxSize(int bulkMaxSize) {
        this.bulkMaxSize = bulkMaxSize;
    }

    public boolean isFindExecutionStats() {
        return findExecutionStats;
    }

    public void setFindExecutionStats(boolean findExecutionStats) {
        this.findExecutionStats = findExecutionStats;
    }

    public SchemaOperation getSchemaOperation() {
        return schemaOperation;
    }

    public void setSchemaOperation(SchemaOperation schemaOperation) {
        this.schemaOperation = schemaOperation;
    }

    public int getDefaultShards() {
        return defaultShards;
    }

    public void setDefaultShards(int defaultShards) {
        this.defaultShards = defaultShards;
    }

    public int getDefaultReplicas() {
        return defaultReplicas;
    }

    public void setDefaultReplicas(int defaultReplicas) {
        this.defaultReplicas = defaultReplicas;
    }

    public boolean isDefaultPartitioned() {
        return defaultPartitioned;
    }

    public void setDefaultPartitioned(boolean defaultPartitioned) {
        this.defaultPartitioned = defaultPartitioned;
    }

    public QueryStrategy getQueryStrategy() {
        return queryStrategy;
    }

    public void setQueryStrategy(QueryStrategy queryStrategy) {
        this.queryStrategy = queryStrategy;
    }

    public void copy(CouchDbProperties properties) {
        setPassword(properties.getPassword());
        setUsername(properties.getUsername());
        setUrl(properties.url);
        setBulkMaxSize(properties.getBulkMaxSize());
        setFindExecutionStats(properties.isFindExecutionStats());
        setSchemaOperation(properties.getSchemaOperation());
        setDefaultShards(properties.getDefaultShards());
        setDefaultReplicas(properties.getDefaultReplicas());
        setDefaultPartitioned(properties.isDefaultPartitioned());
        setQueryStrategy(properties.getQueryStrategy());
    }

}
