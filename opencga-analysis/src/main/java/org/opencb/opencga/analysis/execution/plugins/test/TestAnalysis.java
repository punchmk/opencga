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

package org.opencb.opencga.analysis.execution.plugins.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opencb.opencga.catalog.models.tool.Manifest;
import org.opencb.opencga.catalog.models.tool.Execution;
import org.opencb.opencga.catalog.models.tool.Option;
import org.opencb.opencga.analysis.execution.plugins.OpenCGAAnalysis;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created on 26/11/15
 *
 * @author Jacobo Coll &lt;jacobo167@gmail.com&gt;
 */
public class TestAnalysis extends OpenCGAAnalysis {

    public static final String OUTDIR = "outdir";
    public static final String PARAM_1 = "param1";
    public static final String ERROR = "error";
    public static final String PLUGIN_ID = "test_plugin";
    private final Manifest manifest;

    public TestAnalysis() {
        List<Option> validParams = Arrays.asList(
                new Option(OUTDIR, "", true),
                new Option(PARAM_1, "", false),
                new Option(ERROR, "", false)
        );
        List<Execution> executions = Collections.singletonList(
                new Execution("default", "default", "", Collections.emptyList(), Collections.emptyList(), OUTDIR, validParams, Collections.emptyList(), null, null)
        );
        manifest = new Manifest(null, "0.1.0", PLUGIN_ID, "Test plugin", "", "", "", null, Collections.emptyList(), executions, null, null);
        try {
            System.out.println(new ObjectMapper().writer().writeValueAsString(manifest));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Manifest getManifest() {
        return manifest;
    }

    @Override
    public String getIdentifier() {
        return PLUGIN_ID;
    }

    @Override
    public int run() throws Exception {
        if (getConfiguration().containsKey(PARAM_1)) {
            getLogger().info(getConfiguration().getString(PARAM_1));
        }
        return getConfiguration().getBoolean(ERROR) ? 1 : 0;
    }

}
