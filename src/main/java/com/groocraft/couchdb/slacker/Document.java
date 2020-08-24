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


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Ancestor of all CouchDB document pojo classes. There is not need to use this class as an ancestor for every pojo class, but it is more easier than define
 * {@code _id} and {@code _rev} to every pojo class.
 *
 * @author Majlanky
 */
public class Document {

    @JsonProperty("_id")
    @JsonInclude(Include.NON_NULL)
    private String id;

    @JsonProperty("_rev")
    @JsonInclude(Include.NON_NULL)
    private String revision;

    public Document() {
    }

    public Document(String id, String revision) {
        this.id = id;
        this.revision = revision;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRevision() {
        return revision;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return Objects.equals(id, document.id) &&
                Objects.equals(revision, document.revision);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, revision);
    }
}
