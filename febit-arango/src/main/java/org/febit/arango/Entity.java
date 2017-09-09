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

import java.io.Serializable;
import java.util.Objects;
import org.febit.arango.meta.ArangoId;

/**
 *
 * @author zqq90
 */
public abstract class Entity implements Serializable {

    @ArangoId
    protected String _id;

    public String id() {
        return _id;
    }

    public String id(String id) {
        setId(id);
        return id;
    }

    public String getId() {
        return _id;
    }

    public void setId(String id) {
        this._id = id;
    }

    public boolean _isPersistent() {
        return this._id != null;
    }

    @Override
    public int hashCode() {
        if (_id == null) {
            return 0;
        }
        return _id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Entity other = (Entity) obj;
        if (!Objects.equals(this._id, other._id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[ _id=" + id() + " ]";
    }
}
