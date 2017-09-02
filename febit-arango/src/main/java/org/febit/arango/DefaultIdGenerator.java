// Copyright (c) 2013-2015, Webit Team. All Rights Reserved.
package org.febit.arango;

/**
 *
 * @author zqq90
 */
public class DefaultIdGenerator implements IdGenerator {

    private static final DefaultIdGenerator INSTANCE = new DefaultIdGenerator();

    public static DefaultIdGenerator getInstance() {
        return INSTANCE;
    }

    @Override
    public String next(ArangoDao dao, Entity entity) {
        return ArangoUtil.randomKey();
    }
}
