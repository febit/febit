// Copyright (c) 2013-2015, Webit Team. All Rights Reserved.
package org.febit.arango;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import java.net.UnknownHostException;
import org.febit.util.Petite;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author zqq90
 */
@Test(enabled = false)
public class ArangoDaoTest {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ArangoDaoTest.class);

    protected String host = null;//"localhost";
    protected int port = 8529;
    protected String user = "arango";
    protected String password = "arango";
    protected String database = "test";

    ArangoDao<Foo> _fooDao;

    @Petite.Init
    public void init() {
        if (host == null) {
            return;
        }
        ArangoDB client = new ArangoDB.Builder()
                .host(host, port)
                .user(user)
                .password(password)
                .registerModule(new VPackFebitModule())
                .build();

        try {
            client.createDatabase(database);
        } catch (Exception e) {
            LOG.warn("", e);
        }

        ArangoDatabase db = client.db(database);
        try {
            db.createCollection("Foo");
            db.createCollection("app_sys_seq");
        } catch (Exception e) {
            LOG.warn("", e);
        }

        _fooDao = new ArangoDao<>(Foo.class, db.collection("Foo"), null);
    }

    @Test(groups = "ignore")
    public void fooTest() {
        ArangoDao<Foo> dao = _fooDao;

        Foo foo = new Foo();

        foo.string = "first";
        dao.save(foo);
        foo.string = "asdasdas";
        foo.i = 1;
        dao.update(foo);

        foo.string = "aaaa";
        foo.i = 2;
        foo.bar = new Bar("name2", "detail");
        dao.update(foo);
        System.out.println();
        assertEquals(_fooDao.findXById(foo.getId(), "bar", Bar.class), foo.bar);

        foo.string = "bbbb";
        foo.i = 3;
        foo.bar = new Bar("name3", "detail");
        dao.update(foo);

        assertEquals(_fooDao.findXById(foo.getId(), "string", String.class), "bbbb");

        System.out.println(dao.list(new Condition().in("i", 1, 3, 4, 5).contains("string", "bb"), FooPart.class));
    }

    @Test(groups = "ignore")
    public void seqTest() throws UnknownHostException {

        ArangoDatabase db = _fooDao.db();
        System.out.println("xxx: " + ArangoUtil.nextSeq(db, "xxx"));
        System.out.println("xxx: " + ArangoUtil.nextSeq(db, "xxx"));
        System.out.println("yyy: " + ArangoUtil.nextSeq(db, "yyy"));
        System.out.println("yyy: " + ArangoUtil.nextSeq(db, "yyy"));
    }

}
