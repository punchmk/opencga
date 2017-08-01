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

import org.opencb.opencga.catalog.models.*;

import java.util.List;
import java.util.Map;

/**
 * Created by pfurio on 29/04/16.
 */
public class StudySummary {

    private String name;
    private String alias;
    private Study.Type type;
    private String creatorId;
    private String creationDate;
    private String description;
    private Status status;
    private long size;
    private String cipher;

    private List<Group> groups;
    private List<Role> roles;

    private List<Experiment> experiments;

    private long files;
    private long jobs;
    private long individuals;
    private long samples;

    private long datasets;
    private long cohorts;

    private List<VariableSet> variableSets;

    private Map<String, Object> attributes;
    private Map<String, Object> stats;

    public StudySummary() {
    }

    public String getName() {
        return name;
    }

    public StudySummary setName(String name) {
        this.name = name;
        return this;
    }

    public String getAlias() {
        return alias;
    }

    public StudySummary setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public Study.Type getType() {
        return type;
    }

    public StudySummary setType(Study.Type type) {
        this.type = type;
        return this;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public StudySummary setCreatorId(String creatorId) {
        this.creatorId = creatorId;
        return this;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public StudySummary setCreationDate(String creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public StudySummary setDescription(String description) {
        this.description = description;
        return this;
    }

    public Status getStatus() {
        return status;
    }

    public StudySummary setStatus(Status status) {
        this.status = status;
        return this;
    }

    public long getDiskUsage() {
        return size;
    }

    public StudySummary setDiskUsage(long size) {
        this.size = size;
        return this;
    }

    public String getCipher() {
        return cipher;
    }

    public StudySummary setCipher(String cipher) {
        this.cipher = cipher;
        return this;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public StudySummary setGroups(List<Group> groups) {
        this.groups = groups;
        return this;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public StudySummary setRoles(List<Role> roles) {
        this.roles = roles;
        return this;
    }

    public List<Experiment> getExperiments() {
        return experiments;
    }

    public StudySummary setExperiments(List<Experiment> experiments) {
        this.experiments = experiments;
        return this;
    }

    public long getFiles() {
        return files;
    }

    public StudySummary setFiles(long files) {
        this.files = files;
        return this;
    }

    public long getJobs() {
        return jobs;
    }

    public StudySummary setJobs(long jobs) {
        this.jobs = jobs;
        return this;
    }

    public long getIndividuals() {
        return individuals;
    }

    public StudySummary setIndividuals(long individuals) {
        this.individuals = individuals;
        return this;
    }

    public long getSamples() {
        return samples;
    }

    public StudySummary setSamples(long samples) {
        this.samples = samples;
        return this;
    }

    public long getDatasets() {
        return datasets;
    }

    public StudySummary setDatasets(long datasets) {
        this.datasets = datasets;
        return this;
    }

    public long getCohorts() {
        return cohorts;
    }

    public StudySummary setCohorts(long cohorts) {
        this.cohorts = cohorts;
        return this;
    }

    public List<VariableSet> getVariableSets() {
        return variableSets;
    }

    public StudySummary setVariableSets(List<VariableSet> variableSets) {
        this.variableSets = variableSets;
        return this;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public StudySummary setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
        return this;
    }

    public Map<String, Object> getStats() {
        return stats;
    }

    public StudySummary setStats(Map<String, Object> stats) {
        this.stats = stats;
        return this;
    }
}
