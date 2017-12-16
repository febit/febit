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
package org.febit.leancloud.client;

import java.util.HashMap;
import org.febit.leancloud.LcQuery;
import org.febit.util.Petite;
import org.febit.util.Stopwatch;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author zqq90
 */
@SuppressWarnings("unchecked")
@Test(groups = {"ignore"})
public class LcClientTest {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LcClientTest.class);

    protected String appId;
    protected String appKey;
    protected String masterKey;

    LcApiClient _client;

    @Petite.Init
    public void init() {
        _client = LcApiClient.builder()
                .appId(appId)
                .masterKey(masterKey)
                .appKey(appKey)
                .build();
    }

    @Test
    public void testQuery() {
        LcQueryResponse<Todo> response;

        Stopwatch stopwatch = Stopwatch.startNew();
        stopwatch.restart();
        LcQuery query;

        query = new LcQuery().keys("objectId,content");
        query.where().contains("content", "123");
        response = _client.query(query, Todo.class);
        LOG.info("isOk: {}, statusCode: {}, time: {}", response.isOk(), response.getStatusCode(), stopwatch.nowInMillis());

        assertNotNull(response);
        assertTrue(response.isOk());
    }

    @Test
    public void testWatch() {

        Stopwatch stopwatch = Stopwatch.startNew();
        for (int i = 0; i < 10; i++) {
            stopwatch.restart();
            LcFindResponse<Todo> findResponse = _client.find("59439446fe88c2006a65031f", Todo.class);
            LOG.info("isOk: {}, statusCode: {}, time: {}", findResponse.isOk(), findResponse.getStatusCode(), stopwatch.nowInMillis());
        }

    }

    @Test
    public void test() {
        if (appId == null) {
            return;
        }

        Todo todo = new Todo();
        todo.content = "中文,123,abc";
        todo.setUpdatedAt("2015-06-09T15:37:56.123Z");
        todo.setCreatedAt("2016-06-01T15:37:56.456Z");
        todo.map = new HashMap<>();
        todo.map.put("number", 123);
        todo.map.put("string", "abc");

        LcCreateResponse createResponse = _client.save(todo);

        assertNotNull(createResponse);
        assertTrue(createResponse.isOk());
        String objectId = createResponse.getObjectId();
        String createdAt = createResponse.getCreatedAt();
        assertNotNull(objectId);
        assertNotNull(createdAt);

        LcFindResponse<Todo> findResponse = _client.find(objectId, Todo.class);

        assertNotNull(findResponse);
        assertTrue(findResponse.isOk());
        Todo found = findResponse.getResult();

        assertNotNull(found);
        assertEquals(found.getCreatedAt(), createdAt);
        assertEquals(found.content, todo.content);
        assertEquals(found.getUpdatedAt(), found.getCreatedAt());
        assertEquals(found.map, todo.map);

        found.map.put("objectId", "xxxxxx");
        found.map.remove("number");

        LcUpdateResponse updateResponse = _client.update(found);

        assertNotNull(updateResponse);
        assertTrue(updateResponse.isOk());
    }

}
