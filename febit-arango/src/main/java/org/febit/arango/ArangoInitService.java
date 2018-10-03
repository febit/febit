/**
 * Copyright 2013-present febit.org (support@febit.org)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.arango;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.CollectionEntity;
import org.febit.Listener;
import org.febit.arango.meta.Arango;
import org.febit.arango.meta.ArangoIdGenerator;
import org.febit.service.Service;
import org.febit.util.ClassUtil;
import org.febit.util.StringUtil;
import org.febit.util.agent.LazyAgent;
import org.febit.util.agent.LazyMap;

/**
 *
 * @author zqq90
 */
public abstract class ArangoInitService implements Service, Listener {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ArangoInitService.class);
    protected final LazyAgent<ArangoDB> LAZY_CLIENT = LazyAgent.create(this::createClient);

    protected final LazyAgent<ArangoDatabase> LAZY_DATABASE = LazyAgent.create(() -> {
        try {
            return createDatabase();
        } catch (Exception ex) {
            throw new RuntimeException("Failed create ArangoDatabase: " + ex.getMessage(), ex);
        }
    });

    protected final LazyMap<String, ArangoCollection> LAZY_COLLECTIONS = LazyMap.create(ArangoInitService.this::createCollection);

    protected abstract ArangoDB.Builder createClientBuilder() throws Exception;

    protected abstract ArangoDatabase createDatabase() throws Exception;

    protected ArangoDB createClient() {
        ArangoDB.Builder builder;
        try {
            builder = createClientBuilder();
        } catch (Exception ex) {
            throw new RuntimeException("Failed create ArangoDB: " + ex.getMessage(), ex);
        }
        builder.registerModule(new VPackFebitModule());
        ArangoDB db = builder.build();
        return db;
    }

    protected synchronized ArangoCollection createCollection(String name) {
        createPhysicalCollectionIfAbsent(name);
        ArangoCollection collection = database().collection(name);
        return collection;
    }

    protected void createPhysicalCollectionIfAbsent(String name) {
        for (CollectionEntity collection : database().getCollections()) {
            if (collection.getName().equals(name)) {
                LOG.debug("Collection exist: {}", name);
                return;
            }
        }
        try {
            LOG.info("Try to create collection: {}", name);
            database().createCollection(name);
        } catch (ArangoDBException e) {
            LOG.warn("Failed to create collection: " + name, e);
        }
    }

    public ArangoDB client() {
        return LAZY_CLIENT.get();
    }

    public ArangoDatabase database() {
        return LAZY_DATABASE.get();
    }

    public ArangoCollection collection(String name) {
        return LAZY_COLLECTIONS.get(name);
    }

    public <E extends Entity> ArangoDao<E> createDao(Class<E> entityType) {
        return createDao(entityType, null);
    }

    public <E extends Entity> ArangoDao<E> createDao(Class<E> entityType, String tableName) {
        if (StringUtil.isEmpty(tableName)) {
            tableName = resolveTableName(entityType);
        }
        ArangoCollection collection = collection(tableName);
        return new ArangoDao<>(entityType, collection, resolveIdCenerator(entityType));
    }

    protected <E extends Entity> IdGenerator resolveIdCenerator(Class<E> entityType) {
        ArangoIdGenerator generatorAnno = entityType.getAnnotation(ArangoIdGenerator.class);
        if (generatorAnno == null) {
            return null;
        }
        Class<? extends IdGenerator> generatorType = generatorAnno.value();
        if (generatorType.equals(DefaultIdGenerator.class)) {
            return DefaultIdGenerator.getInstance();
        }
        if (generatorType.equals(IncreateIdGenerator.class)) {
            return IncreateIdGenerator.getInstance();
        }
        return ClassUtil.newInstance(generatorAnno.value());
    }

    protected String resolveTableName(Class<?> entityType) {
        String tableName = null;
        Arango anno = entityType.getAnnotation(Arango.class);
        if (anno != null) {
            tableName = anno.value();
        }
        if (StringUtil.isEmpty(tableName)) {
            tableName = entityType.getSimpleName();
            LOG.warn("ArangoDao for '{}' used class name '{}' as collection name.", entityType.getName(), tableName);
        }
        return tableName;
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
        client().shutdown();
        LAZY_CLIENT.reset();
        LAZY_DATABASE.reset();
        LAZY_COLLECTIONS.clear();
    }
}
