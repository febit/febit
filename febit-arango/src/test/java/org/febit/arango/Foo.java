// Copyright (c) 2013-2015, Webit Team. All Rights Reserved.
package org.febit.arango;

/**
 *
 * @author zqq90
 */
public class Foo extends Entity {

    String string;
    String[] strings;
    Bar bar;
    Bar[] bars;
    Foo foo;
    String nullable;
    int i;
    int[] ints;

    @Override
    public String toString() {
        return "Foo{id=" + _id + ", " + "string=" + string + ", strings=" + strings + ", bar=" + bar + ", bars=" + bars + ", foo=" + foo + ", nullable=" + nullable + ", i=" + i + ", ints=" + ints + '}';
    }
}
