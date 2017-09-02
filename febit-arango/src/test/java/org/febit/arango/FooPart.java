// Copyright (c) 2013-2015, Webit Team. All Rights Reserved.
package org.febit.arango;

import org.febit.arango.meta.ArangoId;

/**
 *
 * @author zqq90
 */
public class FooPart {

    @ArangoId
    String id;
    String string;
    int i;

    @Override
    public String toString() {
        return "FooPart{" + "string=" + string + ", i=" + i + ", id=" + id + '}';
    }

}
