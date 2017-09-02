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
