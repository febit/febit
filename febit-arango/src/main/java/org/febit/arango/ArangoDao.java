/**
 * Copyright 2013-present febit.org (support@febit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.arango;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.DocumentCreateEntity;
import com.arangodb.entity.DocumentDeleteEntity;
import com.arangodb.entity.DocumentUpdateEntity;
import com.arangodb.model.AqlQueryOptions;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentDeleteOptions;
import com.arangodb.model.DocumentUpdateOptions;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.febit.arango.ArangoUtil.vars;
import org.febit.service.PageResult;

/**
 *
 * @author zqq90
 * @param <E>
 */
public class ArangoDao<E extends Entity> {

    protected final String _tableName;
    protected final IdGenerator _idGenerator;

    protected final Class<E> _entityType;
    protected final ArangoCollection _collection;
    protected final ArangoDatabase _db;

    public ArangoDao(Class<E> entityType, ArangoCollection collection) {
        this(entityType, collection, null);
    }

    /**
     * @param entityType
     * @param collection
     * @param idGenerator
     */
    public ArangoDao(Class<E> entityType, ArangoCollection collection, IdGenerator idGenerator) {
        if (entityType == null) {
            throw new IllegalArgumentException("entityType is required");
        }
        if (collection == null) {
            throw new IllegalArgumentException("collection is required");
        }
        this._collection = collection;
        this._db = collection.db();
        this._entityType = entityType;
        this._tableName = collection.name();
        if (idGenerator == null) {
            this._idGenerator = DefaultIdGenerator.getInstance();
        } else {
            this._idGenerator = idGenerator;
        }
    }

    public ArangoCollection collection() {
        return this._collection;
    }

    public ArangoCursor<E> cursor(Condition query) {
        return cursor(query.buildForQuery(_tableName), query.vars(), null, this._entityType);
    }

    public <T> ArangoCursor<T> cursor(Condition query, Class<T> type) {
        return cursor(query.buildForQuery(_tableName), query.vars(), null, type);
    }

    public <T> ArangoCursor<T> cursor(Condition query,
            final AqlQueryOptions options, Class<T> type) {
        return cursor(query.buildForQuery(_tableName), query.vars(), options, type);
    }

    public <T> ArangoCursor<T> cursor(final String query,
            final Map<String, Object> bindVars,
            final AqlQueryOptions options, Class<T> type) {
        return this._db.query(query, bindVars, options, type);
    }

    public <T> ArangoCursor<T> cursor(final String query,
            final Map<String, Object> bindVars, Class<T> type) {
        return this._db.query(query, bindVars, null, type);
    }

    public <T> List<T> queryField(Condition condition, String field, Class<T> type) {
        return query(condition.buildForQueryField(_tableName, field),
                condition.vars(), type);
    }

    public <T> List<T> queryField(Condition condition, String field, AqlQueryOptions options, Class<T> type) {
        return query(condition.buildForQueryField(_tableName, field),
                condition.vars(), options, type);
    }

    public List<E> query(Condition condition) {
        return query(condition.buildForQuery(_tableName),
                condition.vars(), this._entityType);
    }

    public <T> List<T> query(Condition condition, Class<T> type) {
        return query(condition.buildForQuery(_tableName),
                condition.vars(), type);
    }

    public <T> List<T> query(Condition condition, AqlQueryOptions options, Class<T> type) {
        return query(condition.buildForQuery(_tableName),
                condition.vars(), options, type);
    }

    public List<E> query(String query) {
        return query(query, null, null, this._entityType);
    }

    public <T> List<T> query(String query, Class<T> type) {
        return query(query, null, null, type);
    }

    public <T> List<T> query(String query,
            Map<String, Object> bindVars, Class<T> type) {
        return query(query, bindVars, null, type);
    }

    public <T> List<T> query(String query, Map<String, Object> bindVars,
            AqlQueryOptions options, Class<T> type) {
        return cursor(query, bindVars, options, type).asListRemaining();
    }

    public <T> T queryFieldFirst(Condition condition, String field, Class<T> type) {
        condition.limit(1);
        return queryFirst(condition.buildForQueryField(_tableName, field),
                condition.vars(), type);
    }

    public <T> T queryFieldFirst(Condition condition, String field, AqlQueryOptions options, Class<T> type) {
        condition.limit(1);
        return queryFirst(condition.buildForQueryField(_tableName, field),
                condition.vars(), options, type);
    }

    public E queryFirst(final String query) {
        return queryFirst(query, null, null, this._entityType);
    }

    public <T> T queryFirst(final String query, Class<T> type) {
        return queryFirst(query, null, null, type);
    }

    public <T> T queryFirst(final String query,
            Map<String, Object> bindVars, Class<T> type) {
        return queryFirst(query, bindVars, null, type);
    }

    public <T> T queryFirst(final String query, Map<String, Object> bindVars,
            AqlQueryOptions options, Class<T> type) {
        if (options == null) {
            options = new AqlQueryOptions();
        }
        options.batchSize(1);
        ArangoCursor<T> cursor = cursor(query, bindVars, options, type);
        if (!cursor.hasNext()) {
            return null;
        }
        return cursor.next();
    }

    public E queryFirst(Condition query) {
        return queryFirst(query.buildForQueryFirst(_tableName), query.vars(), this._entityType);
    }

    public <T> T queryFirst(Condition query, Class<T> type) {
        return queryFirst(query.buildForQueryFirst(_tableName), query.vars(), type);
    }

    public <T> T queryFirst(Condition query,
            AqlQueryOptions options,
            Class<T> type) {
        return queryFirst(query.buildForQueryFirst(_tableName), query.vars(), options, type);
    }

    public long count(Condition query) {
        return queryFirst(query.buildForCount(this._tableName), query.vars(), Long.class);
    }

    public boolean exist(Condition query) {
        return queryFirst(query.buildForExist(this._tableName), query.vars(), Boolean.class);
    }

    public void genarateId(Entity object) {
        if (object._isPersistent()) {
            return;
        }
        object.id(_idGenerator.next(this, object));
    }

    public void save(E object, DocumentCreateOptions options) {
        if (options == null) {
            options = new DocumentCreateOptions();
        }
        genarateId(object);
        DocumentCreateEntity<E> createEntity = collection().insertDocument(object, options);
    }

    public void save(E object) {
        save(object, null);
    }

    public void saveSub(Entity object) {
        saveSub(object, null);
    }

    public void saveSub(Entity object, DocumentCreateOptions options) {
        if (options == null) {
            options = new DocumentCreateOptions();
        }
        genarateId(object);
        DocumentCreateEntity<Entity> createEntity = collection().insertDocument(object, options);
    }

    public void save(Collection<E> objects, DocumentCreateOptions options) {
        for (E obj : objects) {
            genarateId(obj);
        }
        collection().insertDocuments(objects, options);
    }

    public void save(Collection<E> objects) {
        save(objects, null);
    }

    public void save(E[] objects, DocumentCreateOptions options) {
        save(Arrays.asList(objects), options);
    }

    public void save(E[] objects) {
        save(Arrays.asList(objects), null);
    }

    public int delete(String key) {
        if (key == null) {
            return 0;
        }
        DocumentDeleteEntity deleteEntity = collection()
                .deleteDocument(key);
        return 1;
    }

    public int delete(String key, DocumentDeleteOptions options) {
        DocumentDeleteEntity deleteEntity = collection()
                .deleteDocument(key, _entityType, options);
        return 1;
    }

    public int delete(String[] ids) {
        return delete(ids, null);
    }

    public int delete(String[] ids, DocumentDeleteOptions options) {
        throw new UnsupportedOperationException();
    }

    public int delete(Condition query, DocumentDeleteOptions options) {
        throw new UnsupportedOperationException();
    }

    public int delete(Condition query) {
        return delete(query, null);
    }

    public E find(Condition query) {
        return queryFirst(query);
    }

    public E find(String key) {
        if (key == null) {
            return null;
        }
        return find(key, _entityType);
    }

    public <T> T find(Condition query, Class<T> clazz) {
        return queryFirst(query, clazz);
    }

    public <T> T find(String key, Class<T> clazz) {
        if (key == null) {
            return null;
        }
        return find(Condition.c("_key", key), clazz);
    }

    public <T> T findXById(String key, String field, Class<T> clazz) {
        if (key == null) {
            return null;
        }
        String query = "FOR d in " + this._tableName + "\n"
                + " FILTER d._key == @_key\n"
                + " RETURN d." + field;
        return queryFirst(query, vars("_key", key), clazz);
    }

    public E[] find(String[] ids) {
        return find(ids, _entityType);
    }

    public <T> T[] find(String[] ids, Class<T> clazz) {
        String query = "FOR d in " + this._tableName + "\n"
                + " FILTER d._key IN @keys\n"
                + " RETURN d";
        ArangoCursor<T> cursor = cursor(query, vars("keys", ids), clazz);
        return ArangoUtil.readToArray(cursor, clazz);
    }

    public List<E> list(Condition query) {
        return query(query, _entityType);
    }

    public <T> List<T> list(Condition query, Class<T> clazz) {
        return query(query, clazz);
    }

    public void page(Condition query, PageResult pageResult) {
        page(query, pageResult, this._entityType);
    }

    public <T> void page(Condition query, PageResult pageResult, Class<T> type) {
        page(query, pageResult, null, type);
    }

    public <T> void page(Condition query, PageResult pageResult, AqlQueryOptions options, Class<T> type) {
        int limit = pageResult.getPageSize();
        int skip = (pageResult.getPage() - 1) * limit;
        query.limit(skip, limit);
        if (options == null) {
            options = new AqlQueryOptions();
        }
        options.fullCount(Boolean.TRUE);
        options.count(Boolean.TRUE);
        ArangoCursor cursor = cursor(query, options, type);
        pageResult.setTotalSize(getFullCount(cursor));
        List<E> rows = cursor.asListRemaining();
        pageResult.setResults(rows);
        pageResult.setLinks(ArangoUtil.resolveLinks(rows, type));
    }

    public <T> T findAndDelete(Condition query, Class<T> clazz) {
        throw new UnsupportedOperationException();
    }

    public <T> T findAndModify(Condition query, Class<T> clazz) {
        throw new UnsupportedOperationException();
    }

    public void update(Entity entity) {
        update(entity, null);
    }

    public void update(Entity entity, DocumentUpdateOptions options) {
        DocumentUpdateEntity updateEntity = collection()
                .updateDocument(entity.getId(), entity, options);
    }

    public boolean updateXById(String key, String name, Object val) {
        return updateXById(key, name, val, null);
    }

    public boolean updateXById(String key, String name, Object val, DocumentUpdateOptions options) {
        Map<String, Object> update = new HashMap<>();
        update.put(name, val);
        return update(key, update, options);
    }

    public boolean update(String key, Map<String, Object> update) {
        return update(key, update, null);
    }

    public boolean update(String key, Map<String, Object> update, DocumentUpdateOptions options) {
        DocumentUpdateEntity updateEntity = collection()
                .updateDocument(key, update, options);
        updateEntity.getKey();
        return true;
    }

    public int update(Condition query, Map<String, Object> update) {
        List<String> ids = query(query.buildForUpdate(_tableName, update), query.vars(), String.class
        );
        return ids.size();
    }

    public int upsert(Condition query, Map<String, Object> update) {
        List<String> ids = query(query.buildForUpdate(_tableName, update), query.vars(), String.class
        );
        return ids.size();
    }

    public ArangoDatabase db() {
        return _db;
    }

    public Class<E> getEntityType() {
        return _entityType;
    }

    public String getTableName() {
        return _tableName;
    }


    protected static long getFullCount(ArangoCursor cursor) {
        if (cursor == null) {
            return 0;
        }
        if (cursor.getStats() == null) {
            return 0;
        }
        Long count = cursor.getStats().getFullCount();
        return count != null ? count : 0;
    }

}
