// Copyright (c) 2013-2015, Webit Team. All Rights Reserved.
package org.febit.arango;

/**
 *
 * @author zqq90
 */
public class IncreateIdGenerator implements IdGenerator {

    private static final IncreateIdGenerator INSTANCE = new IncreateIdGenerator();

    public static IncreateIdGenerator getInstance() {
        return INSTANCE;
    }

    @Override
    public String next(ArangoDao dao, Entity entity) {
        return Long.toString(ArangoUtil.nextSeq(dao.db(), dao.getTableName()));
    }
}
