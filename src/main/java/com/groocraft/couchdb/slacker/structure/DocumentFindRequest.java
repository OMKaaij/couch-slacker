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

package com.groocraft.couchdb.slacker.structure;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.groocraft.couchdb.slacker.annotation.Index;
import com.groocraft.couchdb.slacker.utils.PartTreeWithParameters;
import com.groocraft.couchdb.slacker.utils.PartTreeWithParametersSerializer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Pojo class for creating json Mango find query.
 *
 * @author Majlanky
 */
@SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection"})
public class DocumentFindRequest {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("use_index")
    private String[] useIndex;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("limit")
    private Integer limit;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("skip")
    private Long skip;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("sort")
    private List<Map<String, String>> sort;

    @JsonProperty("selector")
    @JsonSerialize(using = PartTreeWithParametersSerializer.class)
    private final PartTreeWithParameters partTree;

    /**
     * @param partTree      created based on the generic query method name. Must not be {@literal null}
     * @param index         {@link Index} annotation if present on the generic query methods
     * @param pageable      if the generic query method has {@link Pageable} parameter, there is information about it, {@link Pageable#unpaged()} in all other
     *                      cases. Must not be {@literal null}
     * @param sortParameter if the generic query method has {@link Sort} parameter, there is an information about it, {@link Sort#unsorted()}
     *                      in all other cases. Must not be {@literal null}
     */
    public DocumentFindRequest(PartTreeWithParameters partTree, Index index, Pageable pageable, Sort sortParameter) {
        this.partTree = partTree;
        if (index != null) {
            useIndex = index.value();
        }
        Optional.ofNullable(partTree.getPartTree().getMaxResults()).ifPresent(i -> limit = i);
        if (pageable.isPaged()) {
            skip = pageable.getOffset();
            limit = pageable.getPageSize();
        }
        if (partTree.getPartTree().getSort().isSorted() || sortParameter.isSorted() || pageable.getSort().isSorted()) {
            sort = new LinkedList<>();
            sortParameter.and(partTree.getPartTree().getSort()).and(pageable.getSort()).forEach(o -> sort.add(Map.of(o.getProperty(),
                    o.getDirection().toString().toLowerCase())));
        }
    }

}
