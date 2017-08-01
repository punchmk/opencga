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

package org.opencb.opencga.server.rest;

import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.opencga.catalog.models.Variable;
import org.opencb.opencga.catalog.models.VariableSet;
import org.opencb.opencga.catalog.models.summaries.VariableSetSummary;
import org.opencb.opencga.core.exception.VersionException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.util.List;

/**
 * Created by jacobo on 16/12/14.
 */
@Path("/{version}/variableset")
@Produces("application/json")
@Api(value = "VariableSet", position = 8, description = "Methods for working with 'variableset' endpoint")
public class VariableSetWSServer extends OpenCGAWSServer {


    public VariableSetWSServer(@Context UriInfo uriInfo, @Context HttpServletRequest httpServletRequest, @Context HttpHeaders httpHeaders)
            throws IOException, VersionException {
        super(uriInfo, httpServletRequest, httpHeaders);
    }

    private static class VariableSetParameters {
        public Boolean unique;
        public Boolean confidential;
        public String name;
        public String description;
        public List<Variable> variables;
    }

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create variable set", position = 1, response = VariableSet.class)
    public Response createSet(
            @ApiParam(value = "DEPRECATED: studyId", hidden = true) @QueryParam("studyId") String studyIdStr,
            @ApiParam(value = "Study [[user@]project:]study where study and project can be either the id or alias") @QueryParam("study")
                    String studyStr,
            @ApiParam(value="JSON containing the variableSet information", required = true) VariableSetParameters params) {
        try {
            if (StringUtils.isNotEmpty(studyIdStr)) {
                studyStr = studyIdStr;
            }
            logger.info("variables: {}", params.variables);
            long studyId = catalogManager.getStudyId(studyStr, sessionId);
            QueryResult<VariableSet> queryResult = catalogManager.getStudyManager().createVariableSet(studyId, params.name,
                    params.unique, params.confidential, params.description, null, params.variables, sessionId);
            return createOkResponse(queryResult);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{variableset}/info")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get VariableSet info", position = 2, response = VariableSet.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "include", value = "Fields included in the response, whole JSON path must be provided",
                    example = "name,attributes", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "exclude", value = "Fields excluded in the response, whole JSON path must be provided",
                    example = "id,status", dataType = "string", paramType = "query"),
    })
    public Response variablesetInfo(
            @ApiParam(value = "Study [[user@]project:]study where study and project can be either the id or alias") @QueryParam("study")
                    String studyStr,
            @ApiParam(value = "Variable set id or name", required = true) @PathParam("variableset") String variableset) {
        try {
            QueryResult<VariableSet> queryResult =
                    catalogManager.getStudyManager().getVariableSet(studyStr, variableset, queryOptions, sessionId);
            return createOkResponse(queryResult);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{variableset}/summary")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get VariableSet summary", position = 2, response = VariableSetSummary.class)
    public Response variablesetSummary(
            @ApiParam(value = "Study [[user@]project:]study where study and project can be either the id or alias") @QueryParam("study")
                    String studyStr,
            @ApiParam(value = "Variable set id or name", required = true) @PathParam("variableset") String variablesetId) {
        try {
            QueryResult<VariableSetSummary> queryResult = catalogManager.getStudyManager().getVariableSetSummary(studyStr, variablesetId,
                    sessionId);
            return createOkResponse(queryResult);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/search")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get VariableSet info", position = 2, response = VariableSet[].class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "include", value = "Fields included in the response, whole JSON path must be provided",
                    example = "name,attributes", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "exclude", value = "Fields excluded in the response, whole JSON path must be provided",
                    example = "id,status", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "limit", value = "Number of results to be returned in the queries", dataType = "integer",
                    paramType = "query"),
            @ApiImplicitParam(name = "skip", value = "Number of results to skip in the queries", dataType = "integer", paramType = "query"),
            @ApiImplicitParam(name = "count", value = "Total number of results. [PENDING]", dataType = "boolean", paramType = "query")
    })
    public Response search(@ApiParam(value = "studyId", hidden = true) @QueryParam("studyId") String studyIdStr,
                           @ApiParam(value = "Study [[user@]project:]study where study and project can be either the id or alias") @QueryParam("study") String studyStr,
                           @ApiParam(value = "CSV list of variable set ids or names", required = false) @QueryParam("id") String id,
                           @ApiParam(value = "name", required = false) @QueryParam("name") String name,
                           @ApiParam(value = "description", required = false) @QueryParam("description") String description,
                           @ApiParam(value = "Release value") @QueryParam("release") String release,
                           @ApiParam(value = "attributes", required = false) @QueryParam("attributes") String attributes,
                           @ApiParam(value = "Skip count", defaultValue = "false") @QueryParam("skipCount") boolean skipCount) {
        try {
            queryOptions.put(QueryOptions.SKIP_COUNT, skipCount);

            if (StringUtils.isNotEmpty(studyIdStr)) {
                studyStr = studyIdStr;
            }
            if (StringUtils.isNotEmpty(query.getString("study"))) {
                query.remove("study");
            }

            QueryResult<VariableSet> queryResult = catalogManager.getStudyManager().searchVariableSets(studyStr, query, queryOptions,
                    sessionId);
            return createOkResponse(queryResult);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    private static class VariableSetUpdateParameters {
        public String name;
        public String description;
    }

    @POST
    @Path("/{variableset}/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update some variableset attributes using POST method [PENDING]", position = 3, response = VariableSet.class)
    public Response updateByPost(
            @ApiParam(value = "Variable set id or name", required = true) @PathParam("variableset") String variablesetId,
            @ApiParam(value="JSON containing the parameters to be updated", required = true) VariableSetUpdateParameters params) {
        return createErrorResponse("update - POST", "PENDING");
    }

    @GET
    @Path("/{variableset}/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Delete an unused variable Set", position = 4)
    public Response delete(
            @ApiParam(value = "Study [[user@]project:]study where study and project can be either the id or alias") @QueryParam("study") String studyStr,
            @ApiParam(value = "Variable set id or name", required = true) @PathParam("variableset") String variablesetId) {
        try {
            QueryResult<VariableSet> queryResult = catalogManager.getStudyManager().deleteVariableSet(studyStr, variablesetId, sessionId);
            return createOkResponse(queryResult);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @POST
    @Path("/{variableset}/field/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Add a new field in a variable set", position = 5)
    public Response addFieldToVariableSet(
            @ApiParam(value = "Study [[user@]project:]study where study and project can be either the id or alias") @QueryParam("study") String studyStr,
            @ApiParam(value = "Variable set id or name", required = true) @PathParam("variableset") String variablesetId,
            @ApiParam(value = "Variable to be added", required = true) Variable variable) {
        try {
            QueryResult<VariableSet> queryResult = catalogManager.getStudyManager().addFieldToVariableSet(studyStr, variablesetId,
                    variable, sessionId);
            return createOkResponse(queryResult);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{variableset}/field/delete")
    @ApiOperation(value = "Delete one field from a variable set", position = 6)
    public Response renameFieldInVariableSet(
            @ApiParam(value = "Study [[user@]project:]study where study and project can be either the id or alias") @QueryParam("study") String studyStr,
            @ApiParam(value = "Variable set id or name", required = true) @PathParam("variableset") String variablesetId,
            @ApiParam(value = "Variable name to delete", required = true) @QueryParam("name") String name) {
        try {
            QueryResult<VariableSet> queryResult = catalogManager.getStudyManager().removeFieldFromVariableSet(studyStr, variablesetId,
                    name, sessionId);
            return createOkResponse(queryResult);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{variableset}/field/rename")
    @ApiOperation(value = "Rename the field id of a field in a variable set", position = 7)
    public Response renameFieldInVariableSet(
            @ApiParam(value = "Study [[user@]project:]study where study and project can be either the id or alias") @QueryParam("study") String studyStr,
            @ApiParam(value = "Variable set id or name", required = true) @PathParam("variableset") String variablesetId,
            @ApiParam(value = "Variable name to rename", required = true) @QueryParam("oldName") String oldName,
            @ApiParam(value = "New name for the variable", required = true) @QueryParam("newName") String newName) {
        try {
            QueryResult<VariableSet> queryResult = catalogManager.getStudyManager().renameFieldFromVariableSet(studyStr, variablesetId,
                    oldName, newName, sessionId);
            return createOkResponse(queryResult);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }
}
