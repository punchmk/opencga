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

package org.opencb.opencga.catalog.models;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by jacobo on 11/09/14.
 */
public class Variable {

    private String name;
    private String title;
    private String category;

    /**
     * Type accepted values: text, numeric.
     */
    private VariableType type;
    private Object defaultValue;
    private boolean required;
    private boolean multiValue;

    /**
     * Example for numeric range: -3:5.
     * Example for categorical values: T,F
     */
    private List<String> allowedValues;
    private long rank;
    private String dependsOn;
    private String description;
    /**
     * Variables for validate internal fields. Only valid if type is OBJECT.
     **/
    private Set<Variable> variableSet;
    private Map<String, Object> attributes;

    public Variable() {
    }

    public Variable(String name, String title, String category, VariableType type, Object defaultValue, boolean required,
                    boolean multiValue, List<String> allowedValues, long rank, String dependsOn, String description,
                    Set<Variable> variableSet, Map<String, Object> attributes) {
        this.title = title;
        this.name = name;
        this.category = category;
        this.type = type;
        this.defaultValue = defaultValue;
        this.required = required;
        this.multiValue = multiValue;
        this.allowedValues = allowedValues;
        this.rank = rank;
        this.dependsOn = dependsOn;
        this.description = description;
        this.variableSet = variableSet;
        this.attributes = attributes;
    }

    @Deprecated
    public Variable(String name, String category, VariableType type, Object defaultValue, boolean required, boolean multiValue,
                    List<String> allowedValues, long rank, String dependsOn, String description, Set<Variable> variableSet,
                    Map<String, Object> attributes) {
        this(name, "", category, type, defaultValue, required, multiValue, allowedValues, rank, dependsOn, description, variableSet,
                attributes);
    }

    public enum VariableType {
        BOOLEAN,
        CATEGORICAL,
        INTEGER,
        DOUBLE,
        TEXT,
        OBJECT
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Variable{");
        sb.append("name='").append(name).append('\'');
        sb.append(", title='").append(title).append('\'');
        sb.append(", category='").append(category).append('\'');
        sb.append(", type=").append(type);
        sb.append(", defaultValue=").append(defaultValue);
        sb.append(", required=").append(required);
        sb.append(", multiValue=").append(multiValue);
        sb.append(", allowedValues=").append(allowedValues);
        sb.append(", rank=").append(rank);
        sb.append(", dependsOn='").append(dependsOn).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", variableSet=").append(variableSet);
        sb.append(", attributes=").append(attributes);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Variable)) {
            return false;
        }

        Variable variable = (Variable) o;

        if (!name.equals(variable.name)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public String getTitle() {
        return title;
    }

    public Variable setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public VariableType getType() {
        return type;
    }

    public void setType(VariableType type) {
        this.type = type;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isMultiValue() {
        return multiValue;
    }

    public void setMultiValue(boolean multiValue) {
        this.multiValue = multiValue;
    }

    public List<String> getAllowedValues() {
        return allowedValues;
    }

    public void setAllowedValues(List<String> allowedValues) {
        this.allowedValues = allowedValues;
    }

    public long getRank() {
        return rank;
    }

    public void setRank(long rank) {
        this.rank = rank;
    }

    public String getDependsOn() {
        return dependsOn;
    }

    public void setDependsOn(String dependsOn) {
        this.dependsOn = dependsOn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Variable> getVariableSet() {
        return variableSet;
    }

    public void setVariableSet(Set<Variable> variableSet) {
        this.variableSet = variableSet;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}
