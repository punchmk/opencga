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

package org.opencb.opencga.catalog.db.mongodb.converters;

import org.bson.Document;
import org.opencb.commons.datastore.mongodb.GenericDocumentComplexConverter;
import org.opencb.opencga.catalog.models.Cohort;
import org.opencb.opencga.catalog.models.Sample;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by pfurio on 3/22/16.
 */
public class CohortConverter extends GenericDocumentComplexConverter<Cohort> {

    public CohortConverter() {
        super(Cohort.class);
    }

    @Override
    public Document convertToStorageType(Cohort object) {
        Document document = super.convertToStorageType(object);
        document.put("id", document.getInteger("id").longValue());
        document.put("samples", convertSamplesToDocument(object.getSamples()));
        return document;
    }

    public List<Document> convertSamplesToDocument(List<Sample> sampleList) {
        if (sampleList == null || sampleList.size() == 0) {
            return Collections.emptyList();
        }
        List<Document> samples = new ArrayList(sampleList.size());
        for (Sample sample : sampleList) {
            long sampleId = sample != null ? (sample.getId() == 0 ? -1L : sample.getId()) : -1L;
            if (sampleId > 0) {
                samples.add(new Document("id", sampleId));
            }
        }
        return samples;
    }

}
