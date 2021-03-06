/*
 * Copyright 2015-2016 OpenCB
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

package org.opencb.opencga.app.cli.admin;

import org.opencb.opencga.app.cli.CommandExecutor;

/**
 * Created on 03/05/16
 *
 * @author Jacobo Coll &lt;jacobo167@gmail.com&gt;
 */
public abstract class AdminCommandExecutor extends CommandExecutor {

    protected String adminPassword;


    public AdminCommandExecutor(AdminCliOptionsParser.AdminCommonCommandOptions options) {
        super(options);
        this.adminPassword = options.adminPassword;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public CommandExecutor setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
        return this;
    }
}
