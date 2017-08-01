package org.opencb.opencga.storage.core.manager.variant.operations;

import org.junit.Before;
import org.junit.Test;
import org.opencb.biodata.models.variant.StudyEntry;
import org.opencb.biodata.models.variant.VariantSource;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.opencga.catalog.db.api.CohortDBAdaptor;
import org.opencb.opencga.catalog.db.api.FileDBAdaptor;
import org.opencb.opencga.catalog.models.Cohort;
import org.opencb.opencga.catalog.models.File;
import org.opencb.opencga.catalog.models.FileIndex;
import org.opencb.opencga.catalog.models.Sample;
import org.opencb.opencga.storage.core.manager.variant.AbstractVariantStorageOperationTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created on 10/07/17.
 *
 * @author Jacobo Coll &lt;jacobo167@gmail.com&gt;
 */
public class RemoveVariantsTest extends AbstractVariantStorageOperationTest {

    @Override
    protected VariantSource.Aggregation getAggregation() {
        return VariantSource.Aggregation.NONE;
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testLoadAndRemoveOne() throws Exception {

        File file77 = create("platinum/1K.end.platinum-genomes-vcf-NA12877_S1.genome.vcf.gz");
        indexFile(file77, new QueryOptions(), outputId);

        removeFile(file77, new QueryOptions(), outputId);

        // File already transformed. Just load
        loadFile(file77, new QueryOptions(), outputId);

    }

    @Test
    public void testLoadAndRemoveOneWithOtherLoaded() throws Exception {
        File file78 = create("platinum/1K.end.platinum-genomes-vcf-NA12878_S1.genome.vcf.gz");
        indexFile(file78, new QueryOptions(), outputId);

        testLoadAndRemoveOne();
    }


    @Test
    public void testLoadAndRemoveMany() throws Exception {
        List<File> files = new ArrayList<>();
        for (int i = 77; i <= 93; i++) {
            files.add(create("platinum/1K.end.platinum-genomes-vcf-NA128" + i + "_S1.genome.vcf.gz"));
        }
        indexFiles(files, new QueryOptions(), outputId);

        removeFile(files.subList(0, files.size()/2), new QueryOptions(), outputId);

    }

    @Test
    public void testLoadAndRemoveDifferentChromosomes() throws Exception {
        List<File> files = new ArrayList<>();
        files.add(create("1k.chr1.phase3_shapeit2_mvncall_integrated_v5.20130502.genotypes.vcf.gz"));
        files.add(create("10k.chr22.phase3_shapeit2_mvncall_integrated_v5.20130502.genotypes.vcf.gz"));
        indexFiles(files, new QueryOptions(), outputId);

        removeFile(files.get(0), new QueryOptions(), outputId);
    }

    @Test
    public void testLoadAndRemoveStudy() throws Exception {
        File file77 = create("platinum/1K.end.platinum-genomes-vcf-NA12877_S1.genome.vcf.gz");
        File file78 = create("platinum/1K.end.platinum-genomes-vcf-NA12878_S1.genome.vcf.gz");

        indexFile(file77, new QueryOptions(), outputId);
        indexFile(file78, new QueryOptions(), outputId);

        removeStudy(studyId, new QueryOptions());
    }

    private void removeFile(File file, QueryOptions options, long outputId) throws Exception {
        removeFile(Collections.singletonList(file), options, outputId);
    }

    private void removeFile(List<File> files, QueryOptions options, long outputId) throws Exception {
        List<String> fileIds = files.stream().map(File::getId).map(String::valueOf).collect(Collectors.toList());

        long studyId = catalogManager.getStudyIdByFileId(files.get(0).getId());

        List<File> removedFiles = variantManager.removeFile(fileIds, String.valueOf(studyId), sessionId, new QueryOptions());
        assertEquals(files.size(), removedFiles.size());

        Cohort all = catalogManager.getCohortManager().get(studyId, new Query(CohortDBAdaptor.QueryParams.NAME.key(), StudyEntry.DEFAULT_COHORT), null, sessionId).first();
        Set<Long> allSampleIds = all.getSamples().stream().map(Sample::getId).collect(Collectors.toSet());

        assertThat(all.getStatus().getName(), anyOf(is(Cohort.CohortStatus.INVALID), is(Cohort.CohortStatus.NONE)));
        Set<Long> loadedSamples = catalogManager.getAllFiles(studyId, new Query(FileDBAdaptor.QueryParams.INDEX_STATUS_NAME.key(), FileIndex.IndexStatus.READY), null, sessionId)
                .getResult()
                .stream()
                .flatMap(f -> f.getSamples().stream())
                .map(Sample::getId)
                .collect(Collectors.toSet());
        assertEquals(loadedSamples, allSampleIds);

        for (File file : removedFiles) {
            assertEquals(FileIndex.IndexStatus.TRANSFORMED, file.getIndex().getStatus().getName());
        }

    }

    private void removeStudy(Object study, QueryOptions options) throws Exception {
        variantManager.removeStudy(study.toString(), sessionId, new QueryOptions());

        Query query = new Query(FileDBAdaptor.QueryParams.INDEX_STATUS_NAME.key(), FileIndex.IndexStatus.READY);
        assertEquals(0, catalogManager.getFileManager().count(query, sessionId).first().intValue());

        Cohort all = catalogManager.getCohortManager().get(studyId, new Query(CohortDBAdaptor.QueryParams.NAME.key(), StudyEntry.DEFAULT_COHORT), null, sessionId).first();
        assertTrue(all.getSamples().isEmpty());
    }


}
