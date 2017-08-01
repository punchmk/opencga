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

package org.opencb.opencga.catalog.managers;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opencb.commons.datastore.core.ObjectMap;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.test.GenericTest;
import org.opencb.opencga.catalog.CatalogManagerExternalResource;
import org.opencb.opencga.catalog.db.api.ProjectDBAdaptor;
import org.opencb.opencga.catalog.exceptions.CatalogAuthorizationException;
import org.opencb.opencga.catalog.exceptions.CatalogException;
import org.opencb.opencga.catalog.models.Project;
import org.opencb.opencga.catalog.models.Study;
import org.opencb.opencga.catalog.models.acls.permissions.StudyAclEntry;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by pfurio on 28/11/16.
 */
public class ProjectManagerTest extends GenericTest {

    public final static String PASSWORD = "asdf";
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public CatalogManagerExternalResource catalogManagerResource = new CatalogManagerExternalResource();

    protected CatalogManager catalogManager;
    protected String sessionIdUser;
    protected String sessionIdUser2;
    protected String sessionIdUser3;
    private long project1;
    private long project2;
    private long project3;
    private long studyId;
    private long studyId2;
    private long studyId3;

    @Before
    public void setUp() throws IOException, CatalogException {
        catalogManager = catalogManagerResource.getCatalogManager();
        setUpCatalogManager(catalogManager);
    }

    public void setUpCatalogManager(CatalogManager catalogManager) throws IOException, CatalogException {
        catalogManager.createUser("user", "User Name", "mail@ebi.ac.uk", PASSWORD, "", null, null);
        catalogManager.createUser("user2", "User2 Name", "mail2@ebi.ac.uk", PASSWORD, "", null, null);
        catalogManager.createUser("user3", "User3 Name", "user.2@e.mail", PASSWORD, "ACME", null, null);

        sessionIdUser = catalogManager.login("user", PASSWORD, "127.0.0.1").first().getId();
        sessionIdUser2 = catalogManager.login("user2", PASSWORD, "127.0.0.1").first().getId();
        sessionIdUser3 = catalogManager.login("user3", PASSWORD, "127.0.0.1").first().getId();

        project1 = catalogManager.getProjectManager().create("Project about some genomes", "1000G", "", "ACME", "Homo sapiens",
                null, null, "GRCh38", new QueryOptions(), sessionIdUser).first().getId();
        project2 = catalogManager.getProjectManager().create("Project Management Project", "pmp", "life art intelligent system",
                "myorg", "Homo sapiens", null, null, "GRCh38", new QueryOptions(),
                sessionIdUser2).first().getId();
        project3 = catalogManager.getProjectManager().create("project 1", "p1", "", "", "Homo sapiens",
                null, null, "GRCh38", new QueryOptions(), sessionIdUser3).first().getId();

        studyId = catalogManager.createStudy(project1, "Phase 1", "phase1", Study.Type.TRIO, "Done", sessionIdUser).first().getId();
        studyId2 = catalogManager.createStudy(project1, "Phase 3", "phase3", Study.Type.CASE_CONTROL, "d", sessionIdUser).first().getId();
        studyId3 = catalogManager.createStudy(project2, "Study 1", "s1", Study.Type.CONTROL_SET, "", sessionIdUser2).first().getId();
    }

    @Test
    public void getOwnProjectNoStudies() throws CatalogException {
        QueryResult<Project> projectQueryResult = catalogManager.getProjectManager().get(project3, null, sessionIdUser3);
        assertEquals(1, projectQueryResult.getNumResults());
    }

    @Test
    public void getSharedProjects() throws CatalogException {
        try {
            catalogManager.getProjectManager().getSharedProjects("user", null, sessionIdUser);
            fail("User should not have permissions oveer this project yet");
        } catch (CatalogAuthorizationException e) {
            // Correct
        }

        // Create a new study in project2 with some dummy permissions for user
        long s2 = catalogManager.createStudy(project2, "Study 2", "s2", Study.Type.CONTROL_SET, "", sessionIdUser2).first().getId();
        catalogManager.createStudyAcls(Long.toString(s2), "user", StudyAclEntry.StudyPermissions.VIEW_STUDY.toString(), null,
                sessionIdUser2);

        QueryResult<Project> queryResult = catalogManager.getProjectManager().getSharedProjects("user", null, sessionIdUser);
        assertEquals(1, queryResult.getNumResults());
        assertEquals(1, queryResult.first().getStudies().size());
        assertEquals("s2", queryResult.first().getStudies().get(0).getAlias());

        // Add permissions to a group were user belongs
        catalogManager.getStudyManager().createGroup(Long.toString(studyId3), "@member", "user", sessionIdUser2);
        catalogManager.createStudyAcls(Long.toString(studyId3), "@member", StudyAclEntry.StudyPermissions.VIEW_STUDY.toString(), null,
                sessionIdUser2);

        queryResult = catalogManager.getProjectManager().getSharedProjects("user", null, sessionIdUser);
        assertEquals(1, queryResult.getNumResults());
        assertEquals(2, queryResult.first().getStudies().size());
        assertEquals("user2@pmp", queryResult.first().getAlias());

        // Add permissions to user in a study of user3
        long s3 = catalogManager.createStudy(project3, "StudyProject3", "s3", Study.Type.CONTROL_SET, "", sessionIdUser3).first().getId();
        catalogManager.createStudyAcls(Long.toString(s3), "user", StudyAclEntry.StudyPermissions.VIEW_STUDY.toString(), null,
                sessionIdUser3);

        queryResult = catalogManager.getProjectManager().getSharedProjects("user", null, sessionIdUser);
        assertEquals(2, queryResult.getNumResults());
        for (Project project : queryResult.getResult()) {
            if (project.getId() == project2) {
                assertEquals(2, project.getStudies().size());
            } else {
                assertEquals(1, project.getStudies().size());
            }
        }
    }

    @Test
    public void updateOrganismInProject() throws CatalogException {
        Project pr = catalogManager.getProjectManager().create("Project about some genomes", "project2", "", "ACME", "Homo sapiens",
                null, null, "GRCh38", null, sessionIdUser).first();

        long myProject = pr.getId();

        assertEquals("Homo sapiens", pr.getOrganism().getScientificName());
        assertEquals("", pr.getOrganism().getCommonName());
        assertEquals("GRCh38", pr.getOrganism().getAssembly());
        assertEquals(-1, pr.getOrganism().getTaxonomyCode());

        ObjectMap objectMap = new ObjectMap();
        objectMap.put(ProjectDBAdaptor.QueryParams.ORGANISM_TAXONOMY_CODE.key(), 55);
        QueryResult<Project> update = catalogManager.getProjectManager().update(myProject, objectMap, null, sessionIdUser);

        assertEquals(1, update.getNumResults());
        assertEquals("Homo sapiens", update.first().getOrganism().getScientificName());
        assertEquals("", update.first().getOrganism().getCommonName());
        assertEquals("GRCh38", update.first().getOrganism().getAssembly());
        assertEquals(55, update.first().getOrganism().getTaxonomyCode());

        objectMap = new ObjectMap();
        objectMap.put(ProjectDBAdaptor.QueryParams.ORGANISM_COMMON_NAME.key(), "common");

        update = catalogManager.getProjectManager().update(myProject, objectMap, null, sessionIdUser);

        assertEquals(1, update.getNumResults());
        assertEquals("Homo sapiens", update.first().getOrganism().getScientificName());
        assertEquals("common", update.first().getOrganism().getCommonName());
        assertEquals("GRCh38", update.first().getOrganism().getAssembly());
        assertEquals(55, update.first().getOrganism().getTaxonomyCode());

        objectMap = new ObjectMap();
        objectMap.put(ProjectDBAdaptor.QueryParams.ORGANISM_ASSEMBLY.key(), "assembly");

        thrown.expect(CatalogException.class);
        thrown.expectMessage("Cannot update organism");
        catalogManager.getProjectManager().update(myProject, objectMap, null, sessionIdUser);
    }
}
