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

import com.arangodb.velocypack.VPackAnnotationFieldFilter;
import com.arangodb.velocypack.VPackAnnotationFieldNaming;
import com.arangodb.velocypack.VPackModule;
import com.arangodb.velocypack.VPackSetupContext;
import org.febit.arango.meta.ArangoId;
import org.febit.arango.meta.ArangoIgnore;

/**
 *
 * @author zqq90
 */
public class VPackFebitModule implements VPackModule {

    @Override
    public <C extends VPackSetupContext<C>> void setup(C context) {
        context.annotationFieldFilter(ArangoIgnore.class, new IgnoreVPackAnnotationFieldFilter());
        context.annotationFieldNaming(ArangoId.class, new IdVPackAnnotationFieldNaming());
        context.fieldNamingStrategy(null);
    }

    protected static class IdVPackAnnotationFieldNaming implements VPackAnnotationFieldNaming<ArangoId> {

        @Override
        public String name(ArangoId annotation) {
            return "_key";
        }
    }

    protected static class IgnoreVPackAnnotationFieldFilter implements VPackAnnotationFieldFilter<ArangoIgnore> {

        @Override
        public boolean serialize(ArangoIgnore annotation) {
            return false;
        }

        @Override
        public boolean deserialize(ArangoIgnore annotation) {
            return true;
        }
    }

}
