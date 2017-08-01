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

package org.opencb.opencga.catalog.models.summaries;

import java.util.Collections;
import java.util.List;

/**
 * Created by pfurio on 12/08/16.
 */
public class VariableSetSummary {
    private long id;
    private String name;
    private List<VariableSummary> samples;
    private List<VariableSummary> individuals;
    private List<VariableSummary> cohorts;
    private List<VariableSummary> families;

    public VariableSetSummary() {
        this(0L, "", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    public VariableSetSummary(long id, String name) {
        this(id, name, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    public VariableSetSummary(long id, String name, List<VariableSummary> samples, List<VariableSummary> individuals,
                              List<VariableSummary> cohorts) {
        this.id = id;
        this.name = name;
        this.samples = samples;
        this.individuals = individuals;
        this.cohorts = cohorts;
    }

    public long getId() {
        return id;
    }

    public VariableSetSummary setId(long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public VariableSetSummary setName(String name) {
        this.name = name;
        return this;
    }

    public List<VariableSummary> getSamples() {
        return samples;
    }

    public VariableSetSummary setSamples(List<VariableSummary> samples) {
        this.samples = samples;
        return this;
    }

    public List<VariableSummary> getIndividuals() {
        return individuals;
    }

    public VariableSetSummary setIndividuals(List<VariableSummary> individuals) {
        this.individuals = individuals;
        return this;
    }

    public List<VariableSummary> getCohorts() {
        return cohorts;
    }

    public VariableSetSummary setCohorts(List<VariableSummary> cohorts) {
        this.cohorts = cohorts;
        return this;
    }

    public List<VariableSummary> getFamilies() {
        return families;
    }

    public VariableSetSummary setFamilies(List<VariableSummary> families) {
        this.families = families;
        return this;
    }
}
