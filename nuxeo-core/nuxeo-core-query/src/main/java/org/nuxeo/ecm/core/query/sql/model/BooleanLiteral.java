/*
 * (C) Copyright 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
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
 *
 * Contributors:
 *     Florent Guillaume
 */
package org.nuxeo.ecm.core.query.sql.model;

/**
 * Boolean literal.
 */
public class BooleanLiteral extends Literal {

    private static final long serialVersionUID = 1L;

    public final boolean value;

    public BooleanLiteral(boolean value) {
        this.value = value;
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitBooleanLiteral(this);
    }

    @Override
    public String asString() {
        return String.valueOf(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof BooleanLiteral) {
            return value == ((BooleanLiteral) obj).value;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Boolean.valueOf(value).hashCode();
    }

}
