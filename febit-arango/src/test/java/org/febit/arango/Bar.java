// Copyright (c) 2013-2015, Webit Team. All Rights Reserved.
package org.febit.arango;

import java.util.Objects;

/**
 *
 * @author zqq90
 */
public class Bar {

    public String name;
    String detail;

    public Bar() {
    }

    public Bar(String name, String detail) {
        this.name = name;
        this.detail = detail;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.name);
        hash = 97 * hash + Objects.hashCode(this.detail);
        return hash;
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
        final Bar other = (Bar) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.detail, other.detail)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Bar{" + "name=" + name + ", detail=" + detail + '}';
    }

}
