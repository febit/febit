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
