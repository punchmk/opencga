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

package org.opencb.opencga.analysis.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.opencga.analysis.AnalysisExecutionException;
import org.opencb.opencga.catalog.monitor.executors.old.ExecutorManager;
import org.opencb.opencga.catalog.utils.FileMetadataReader;
import org.opencb.opencga.analysis.storage.AnalysisFileIndexer;
import org.opencb.opencga.catalog.managers.CatalogManager;
import org.opencb.opencga.catalog.db.api.FileDBAdaptor;
import org.opencb.opencga.catalog.exceptions.CatalogException;
import org.opencb.opencga.catalog.models.File;
import org.opencb.opencga.catalog.models.Job;
import org.opencb.opencga.catalog.managers.CatalogFileUtils;
import org.opencb.opencga.catalog.utils.CatalogSampleAnnotationsLoader;
import org.opencb.opencga.storage.core.exceptions.StorageEngineException;
import org.opencb.opencga.storage.core.variant.VariantStorageEngine;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by pfurio on 08/06/16.
 */
public class AnalysisDemo {

    public AnalysisDemo() {
    }

    public static void insertPedigreeFile(CatalogManager catalogManager, long studyId, Path inputFile, String sessionId)
            throws CatalogException, StorageEngineException {
        String path = "data/peds";
        URI sourceUri = inputFile.toUri();
        File file = catalogManager.createFile(studyId, File.Format.PED, File.Bioformat.PEDIGREE,
                Paths.get(path, inputFile.getFileName().toString()).toString(), "Description", true, -1, sessionId).first();
        new CatalogFileUtils(catalogManager).upload(sourceUri, file, null, sessionId, false, false, false, false);
        FileMetadataReader.get(catalogManager).setMetadataInformation(file, null, new QueryOptions(), sessionId, false);

        // Load samples using the pedigree file
        CatalogSampleAnnotationsLoader catalogSampleAnnotationsLoader = new CatalogSampleAnnotationsLoader(catalogManager);
        catalogSampleAnnotationsLoader.loadSampleAnnotations(file, null, sessionId);
    }

    public static void insertVariantFile(CatalogManager catalogManager, long studyId, Path inputFile, String sessionId)
            throws CatalogException, StorageEngineException, AnalysisExecutionException, JsonProcessingException {
        String path = "data/vcfs";
        URI sourceUri = inputFile.toUri();
        File file = catalogManager.createFile(studyId, File.Format.VCF, File.Bioformat.VARIANT,
                Paths.get(path, inputFile.getFileName().toString()).toString(), "Description", true, -1, sessionId).first();
        new CatalogFileUtils(catalogManager).upload(sourceUri, file, null, sessionId, false, false, false, false);
        FileMetadataReader.get(catalogManager).setMetadataInformation(file, null, new QueryOptions(), sessionId, false);


        long inputFileId = file.getId();

        QueryResult<File> outdirResult = catalogManager.searchFile(studyId,
                new Query(FileDBAdaptor.QueryParams.PATH.key(), "data/jobs/"), sessionId);
        long outDirId;
        if (outdirResult.getResult().isEmpty()) {
            outDirId = catalogManager.getFileManager().createFolder(Long.toString(studyId), Paths.get("data/jobs/").toString(), null,
                    true, null, QueryOptions.empty(), sessionId).first().getId();
        } else {
            outDirId = outdirResult.first().getId();
        }

        boolean doTransform = false;
        boolean doLoad = false;
        boolean annotate = false;
        boolean calculateStats = false;
        boolean queue = false;
        String logLevel = "info";

        AnalysisFileIndexer analysisFileIndexer = new AnalysisFileIndexer(catalogManager);
//
//        List<String> extraParams = cliOptions.commonOptions.params.entrySet()
//                .stream()
//                .map(entry -> "-D" + entry.getKey() + "=" + entry.getValue())
//                .collect(Collectors.toList());

        QueryOptions options = new QueryOptions()
                .append(ExecutorManager.EXECUTE, !queue)
                .append(AnalysisFileIndexer.TRANSFORM, doTransform)
                .append(AnalysisFileIndexer.LOAD, doLoad)
                .append(VariantStorageEngine.Options.CALCULATE_STATS.key(), calculateStats)
                .append(VariantStorageEngine.Options.ANNOTATE.key(), annotate)
                .append(AnalysisFileIndexer.LOG_LEVEL, logLevel);
//                .append(AnalysisFileIndexer.PARAMETERS, extraParams)
//                .append(VariantStorageEngine.Options.AGGREGATED_TYPE.key(), cliOptions.aggregated)
//                .append(VariantStorageEngine.Options.EXTRA_GENOTYPE_FIELDS.key(), cliOptions.extraFields)
//                .append(VariantStorageEngine.Options.EXCLUDE_GENOTYPES.key(), cliOptions.excludeGenotype)

        QueryResult<Job> result = analysisFileIndexer.index(inputFileId, outDirId, sessionId, options);
//        if (queue) {
//            System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(result));
//        }
    }

}
