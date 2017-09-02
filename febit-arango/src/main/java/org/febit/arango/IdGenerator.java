// Copyright (c) 2013-2015, Webit Team. All Rights Reserved.
package org.febit.arango;

/**
 *
 * @author zqq90
 */
public interface IdGenerator {

    String next(ArangoDao dao, Entity entity);
}
