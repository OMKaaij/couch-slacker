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

package com.groocraft.couchdb.slacker.repository;

import com.groocraft.couchdb.slacker.CouchDbClient;
import com.groocraft.couchdb.slacker.EntityMetadata;
import com.groocraft.couchdb.slacker.SchemaOperation;
import com.groocraft.couchdb.slacker.TestDocument;
import com.groocraft.couchdb.slacker.exception.SchemaProcessingException;
import com.groocraft.couchdb.slacker.structure.DesignDocument;
import com.groocraft.couchdb.slacker.structure.View;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouchDBSchemaProcessorTest {

    @Mock
    CouchDbClient client;

    @Test
    void testNone() throws Exception {
        CouchDBSchemaProcessor schemaProcessor = new CouchDBSchemaProcessor(client, SchemaOperation.NONE);
        schemaProcessor.process(Collections.singletonList(TestDocument.class));
        verify(client, never().description("No test should be called if operation is NONE")).databaseExists(TestDocument.class);
        verify(client, never().description("No create should be called if operation is NONE")).createDatabase(TestDocument.class);
        verify(client, never().description("No delete should be called if operation is NONE")).deleteDatabase(TestDocument.class);
    }

    @Test
    void testValidate() throws IOException {
        when(client.getEntityMetadata(TestDocument.class)).thenReturn(new EntityMetadata(TestDocument.class));
        when(client.databaseExists(TestDocument.class)).thenReturn(true, false);
        when(client.readDesignSafely("all", "test")).thenReturn(Optional.of(
                new DesignDocument("all", Collections.singleton(new View("data", "function(doc){emit(null);}", "_count")))));
        CouchDBSchemaProcessor schemaProcessor = new CouchDBSchemaProcessor(client, SchemaOperation.VALIDATE);
        assertDoesNotThrow(() -> schemaProcessor.process(Collections.singletonList(TestDocument.class)));
        assertThrows(SchemaProcessingException.class, () -> schemaProcessor.process(Collections.singletonList(TestDocument.class)));
    }

    @Test
    void testCreate() throws Exception {
        when(client.getEntityMetadata(TestDocument.class)).thenReturn(new EntityMetadata(TestDocument.class));
        when(client.databaseExists(TestDocument.class)).thenReturn(true, true, false, true);
        when(client.readDesignSafely("all", "test")).thenReturn(Optional.of(
                new DesignDocument("all", Collections.singleton(new View("data", "function(doc){emit(null);}", "_count")))));
        CouchDBSchemaProcessor schemaProcessor = new CouchDBSchemaProcessor(client, SchemaOperation.CREATE);
        schemaProcessor.process(Collections.singletonList(TestDocument.class));
        verify(client, never().description("Create must no be called when database already exists")).createDatabase(TestDocument.class);
        schemaProcessor.process(Collections.singletonList(TestDocument.class));
        verify(client, times(1).description("Missing database must be created")).createDatabase(TestDocument.class);
    }

    @Test
    void testDrop() throws Exception {
        when(client.getEntityMetadata(TestDocument.class)).thenReturn(new EntityMetadata(TestDocument.class));
        when(client.databaseExists(TestDocument.class)).thenReturn(true, false, true);
        when(client.readDesignSafely("all", "test")).thenReturn(Optional.of(
                new DesignDocument("all", Collections.singleton(new View("data", "function(doc){emit(null);}", "_count")))));
        CouchDBSchemaProcessor schemaProcessor = new CouchDBSchemaProcessor(client, SchemaOperation.DROP);
        schemaProcessor.process(Collections.singletonList(TestDocument.class));
        verify(client, times(1).description("Database must be deleted when DROP")).deleteDatabase(TestDocument.class);
        verify(client, times(1).description("Database must be created when DROP")).createDatabase(TestDocument.class);
    }
}