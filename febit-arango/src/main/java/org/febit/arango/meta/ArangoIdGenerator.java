// Copyright (c) 2013-2015, Webit Team. All Rights Reserved.
package org.febit.arango.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.febit.arango.DefaultIdGenerator;
import org.febit.arango.IdGenerator;

/**
 *
 * @author zqq90
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ArangoIdGenerator {

    Class<? extends IdGenerator> value() default DefaultIdGenerator.class;
}
