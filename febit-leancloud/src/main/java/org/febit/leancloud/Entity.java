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
package org.febit.leancloud;

import java.io.Serializable;

/**
 *
 * @author zqq90
 */
public abstract class Entity implements Serializable {

    public class ACL {
        //FIXME:
    }

    protected String objectId;
    protected ACL ACL;
    protected String createdAt;
    protected String updatedAt;

    public String id() {
        return objectId;
    }

    public void id(String id) {
        this.objectId = id;
    }

    public boolean _isPersistent() {
        return this.objectId != null;
    }

    @Override
    public int hashCode() {
        return objectId != null ? objectId.hashCode() : 0;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        final String eid = ((Entity) obj).id();
        final String myId = id();

        if ((myId == null) || (eid == null)) {
            return false;
        }
        return myId.equals(eid);
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public ACL getACL() {
        return ACL;
    }

    public void setACL(ACL ACL) {
        this.ACL = ACL;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[ id=" + id() + " ]";
    }
}
