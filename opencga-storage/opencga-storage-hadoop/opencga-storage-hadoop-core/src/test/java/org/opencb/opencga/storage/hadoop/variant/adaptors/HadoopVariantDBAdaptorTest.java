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

package org.opencb.opencga.storage.hadoop.variant.adaptors;

import org.junit.*;
import org.junit.rules.ExternalResource;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opencb.biodata.models.feature.Genotype;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.tools.variant.merge.VariantMerger;
import org.opencb.commons.datastore.core.ObjectMap;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.opencga.storage.core.variant.VariantStorageEngine;
import org.opencb.opencga.storage.core.variant.adaptors.VariantDBAdaptorTest;
import org.opencb.opencga.storage.core.variant.adaptors.VariantDBIterator;
import org.opencb.opencga.storage.core.variant.adaptors.VariantQueryException;
import org.opencb.opencga.storage.core.variant.adaptors.VariantQueryParam;
import org.opencb.opencga.storage.hadoop.variant.HadoopVariantStorageEngine;
import org.opencb.opencga.storage.hadoop.variant.HadoopVariantStorageTest;
import org.opencb.opencga.storage.hadoop.variant.VariantHbaseTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.runners.Parameterized.Parameter;
import static org.junit.runners.Parameterized.Parameters;


/**
 * Created on 20/05/16
 *
 * @author Jacobo Coll &lt;jacobo167@gmail.com&gt;
 */
@RunWith(Parameterized.class)
public class HadoopVariantDBAdaptorTest extends VariantDBAdaptorTest implements HadoopVariantStorageTest {


    private static final boolean FILES = false;
    private static final boolean GROUP_BY = false;
    private static final boolean CT_GENES = false;
    protected static final boolean MISSING_ALLELE = false;

    @Parameter
    public ObjectMap indexParams;

    public static ObjectMap previousIndexParams = null;

    @Parameters
    public static List<Object[]> data() {
        List<Object[]> parameters = new ArrayList<>();
        parameters.add(new Object[]{
                new ObjectMap()
                        .append(VariantStorageEngine.Options.TRANSFORM_FORMAT.key(), "avro")
                        .append(HadoopVariantStorageEngine.HADOOP_LOAD_DIRECT, true)
                        .append(VariantStorageEngine.Options.MERGE_MODE.key(), VariantStorageEngine.MergeMode.BASIC)
                        .append(VariantStorageEngine.Options.EXTRA_GENOTYPE_FIELDS.key(), VariantMerger.GENOTYPE_FILTER_KEY + ",DS,GL")
                        .append(VariantStorageEngine.Options.CALCULATE_STATS.key(), true)
        });
        parameters.add(new Object[]{
                new ObjectMap()
                        .append(VariantStorageEngine.Options.TRANSFORM_FORMAT.key(), "proto")
                        .append(HadoopVariantStorageEngine.HADOOP_LOAD_DIRECT, true)
                        .append(VariantStorageEngine.Options.MERGE_MODE.key(), VariantStorageEngine.MergeMode.ADVANCED)
                        .append(VariantStorageEngine.Options.EXTRA_GENOTYPE_FIELDS.key(), VariantMerger.GENOTYPE_FILTER_KEY + ",DS,GL")
                        .append(VariantStorageEngine.Options.CALCULATE_STATS.key(), true)
        });
        return parameters;
    }

    @Before
    @Override
    public void before() throws Exception {
        boolean fileIndexed = VariantDBAdaptorTest.fileIndexed;
        try {
            VariantStorageEngine.MergeMode mergeMode = VariantStorageEngine.MergeMode.from(indexParams);
            if (!indexParams.equals(previousIndexParams)) {
                fileIndexed = false;
                VariantDBAdaptorTest.fileIndexed = false;
                clearDB(getVariantStorageEngine().getVariantTableName());
                clearDB(getVariantStorageEngine().getArchiveTableName(STUDY_ID));
            }
            previousIndexParams = indexParams;
            System.out.println("Loading with MergeMode : " + mergeMode);
            super.before();
        } finally {
            try {
                if (!fileIndexed) {
                    VariantHbaseTestUtils.printVariants(studyConfiguration, getVariantStorageEngine().getDBAdaptor(), newOutputUri());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

//    @Override
//    public Map<String, ?> getOtherStorageConfigurationOptions() {
//        return new ObjectMap(AbstractHadoopVariantStoragePipeline.SKIP_CREATE_PHOENIX_INDEXES, true);
//    }

    @ClassRule
    public static ExternalResource externalResource = new HadoopExternalResource();

//    @Override
//    protected String getHetGT() {
//        return Genotype.HET_REF;
//    }
//
    @Override
    protected String getHomRefGT() {
        return Genotype.HOM_REF;
    }
//
//    @Override
//    protected String getHomAltGT() {
//        return Genotype.HOM_VAR;
//    }

    @Override
    protected ObjectMap getOtherParams() {
        return indexParams;
    }


    @Override
    public void rank_gene() throws Exception {
        Assume.assumeTrue(GROUP_BY);
        super.rank_gene();
    }

    @Override
    public void testExcludeFiles() {
        Assume.assumeTrue(FILES);
        super.testExcludeFiles();
    }

    @Override
    public void testReturnNoneFiles() {
        Assume.assumeTrue(FILES);
        super.testReturnNoneFiles();
    }

    @Override
    public void testGetAllVariants_missingAllele() throws Exception {
        Assume.assumeTrue(MISSING_ALLELE);
        super.testGetAllVariants_missingAllele();
    }

    @Override
    public void testGetAllVariants_negatedGenotypesMixed() {
        thrown.expect(VariantQueryException.class);
        super.testGetAllVariants_negatedGenotypesMixed();
    }

    @Override
    @Ignore
    public void groupBy_gene_limit_0() throws Exception {
        Assume.assumeTrue(GROUP_BY);
        super.groupBy_gene_limit_0();
    }

    @Override
    public void groupBy_gene() throws Exception {
        Assume.assumeTrue(GROUP_BY);
        super.groupBy_gene();
    }

    @Override
    public void testGetAllVariants_files() {
        Assume.assumeTrue(FILES);
        super.testGetAllVariants_files();
    }

    @Override
    public void testGetAllVariants_filter() {
        Assume.assumeTrue(FILES);
        super.testGetAllVariants_filter();
    }

    @Override
    public void rank_ct() throws Exception {
        Assume.assumeTrue(GROUP_BY);
        super.rank_ct();
    }

    @Override
    public void limitSkip(Query query, QueryOptions options) {
        Assume.assumeTrue("Unable to paginate queries without sorting", options.getBoolean(QueryOptions.SORT, false));
        super.limitSkip(query, options);
    }

    @Override
    public void testGetAllVariants_ct_gene() {
        Assume.assumeTrue(CT_GENES);
        super.testGetAllVariants_ct_gene();
    }

    @Override
    public void testInclude() {
        Assume.assumeTrue(FILES);
        super.testInclude();
    }

    @Test
    public void testNativeQuery() {
        int count = 0;
        for (VariantDBIterator iterator = dbAdaptor.iterator(new Query(), new QueryOptions("native", true)); iterator.hasNext();) {
            Variant variant = iterator.next();
//            System.out.println(variant.toJson());
            count++;
        }
        Assert.assertEquals(dbAdaptor.count(new Query()).first().intValue(), count);
    }

    @Test
    public void testArchiveIterator() {
        int count = 0;
        Query query = new Query(VariantQueryParam.STUDIES.key(), studyConfiguration.getStudyId())
                .append(VariantQueryParam.FILES.key(), 6);

        for (VariantDBIterator iterator = dbAdaptor.iterator(query, new QueryOptions("archive", true)); iterator.hasNext(); ) {
            Variant variant = iterator.next();
//            System.out.println(variant.toJson());
            count++;
        }
        Assert.assertEquals(source.getStats().getNumRecords(), count);
    }

}
