/*
 * Copyright 2015-2017 OpenCB
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

package org.opencb.opencga.catalog.models.acls;

/**
 * Created by pfurio on 29/03/17.
 */
public class AclParams {

    protected String permissions;
    protected Action action;

    public enum Action {
        SET,
        ADD,
        REMOVE,
        RESET
    }
    public AclParams() {
    }

    public AclParams(String permissions, Action action) {
        this.permissions = permissions;
        this.action = action;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AclParams{");
        sb.append("permissions='").append(permissions).append('\'');
        sb.append(", action=").append(action);
        sb.append('}');
        return sb.toString();
    }


    public String getPermissions() {
        return permissions;
    }

    public AclParams setPermissions(String permissions) {
        this.permissions = permissions;
        return this;
    }

    public Action getAction() {
        return action;
    }

    public AclParams setAction(Action action) {
        this.action = action;
        return this;
    }
}
