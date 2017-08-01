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

package org.opencb.opencga.catalog.db.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.commons.datastore.core.ObjectMap;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.datastore.mongodb.GenericDocumentComplexConverter;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.datastore.mongodb.MongoDBQueryUtils;
import org.opencb.opencga.catalog.db.api.CohortDBAdaptor;
import org.opencb.opencga.catalog.db.api.DBIterator;
import org.opencb.opencga.catalog.db.api.StudyDBAdaptor;
import org.opencb.opencga.catalog.db.mongodb.converters.CohortConverter;
import org.opencb.opencga.catalog.db.mongodb.iterators.MongoDBIterator;
import org.opencb.opencga.catalog.exceptions.CatalogAuthorizationException;
import org.opencb.opencga.catalog.exceptions.CatalogDBException;
import org.opencb.opencga.catalog.models.*;
import org.opencb.opencga.catalog.models.acls.permissions.CohortAclEntry;
import org.opencb.opencga.catalog.models.acls.permissions.StudyAclEntry;
import org.opencb.opencga.core.common.TimeUtils;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.opencb.opencga.catalog.db.mongodb.AuthorizationMongoDBUtils.filterAnnotationSets;
import static org.opencb.opencga.catalog.db.mongodb.AuthorizationMongoDBUtils.getQueryForAuthorisedEntries;
import static org.opencb.opencga.catalog.db.mongodb.MongoDBUtils.*;

public class CohortMongoDBAdaptor extends AnnotationMongoDBAdaptor implements CohortDBAdaptor {

    private final MongoDBCollection cohortCollection;
    private CohortConverter cohortConverter;

    public CohortMongoDBAdaptor(MongoDBCollection cohortCollection, MongoDBAdaptorFactory dbAdaptorFactory) {
        super(LoggerFactory.getLogger(CohortMongoDBAdaptor.class));
        this.dbAdaptorFactory = dbAdaptorFactory;
        this.cohortCollection = cohortCollection;
        this.cohortConverter = new CohortConverter();
    }

    @Override
    protected GenericDocumentComplexConverter<? extends Annotable> getConverter() {
        return cohortConverter;
    }

    @Override
    protected MongoDBCollection getCollection() {
        return cohortCollection;
    }

    @Override
    public QueryResult<Cohort> insert(Cohort cohort, long studyId, QueryOptions options) throws CatalogDBException {
        long startTime = startQuery();

        dbAdaptorFactory.getCatalogStudyDBAdaptor().checkId(studyId);
        checkCohortNameExists(studyId, cohort.getName());

        long newId = dbAdaptorFactory.getCatalogMetaDBAdaptor().getNewAutoIncrementId();
        cohort.setId(newId);

        Document cohortObject = cohortConverter.convertToStorageType(cohort);
        cohortObject.append(PRIVATE_STUDY_ID, studyId);
        cohortObject.append(PRIVATE_ID, newId);

        try {
            cohortCollection.insert(cohortObject, null);
        } catch (MongoWriteException e) {
            throw ifDuplicateKeyException(() -> CatalogDBException.alreadyExists("Cohort", studyId, "name", cohort.getName(), e), e);
        }

        return endQuery("createCohort", startTime, get(newId, options));
    }

    @Override
    public QueryResult<Cohort> getAllInStudy(long studyId, QueryOptions options) throws CatalogDBException {
        return get(new Query(QueryParams.STUDY_ID.key(), studyId), options);
    }

    @Override
    public QueryResult<Cohort> update(long cohortId, ObjectMap parameters, QueryOptions options) throws CatalogDBException {
        long startTime = startQuery();
        update(new Query(QueryParams.ID.key(), cohortId), parameters);
        return endQuery("Modify cohort", startTime, get(cohortId, options));
    }

    @Override
    @Deprecated
    public QueryResult<AnnotationSet> annotate(long cohortId, AnnotationSet annotationSet, boolean overwrite)
            throws CatalogDBException {
        long startTime = startQuery();

        QueryResult<Long> count = cohortCollection.count(new Document("annotationSets.name", annotationSet.getName())
                .append(PRIVATE_ID, cohortId));
        if (overwrite) {
            if (count.getResult().get(0) == 0) {
                throw CatalogDBException.idNotFound("AnnotationSet", annotationSet.getName());
            }
        } else {
            if (count.getResult().get(0) > 0) {
                throw CatalogDBException.alreadyExists("AnnotationSet", "name", annotationSet.getName());
            }
        }

        Document object = getMongoDBDocument(annotationSet, "AnnotationSet");

        Bson query = new Document(PRIVATE_ID, cohortId);
        if (overwrite) {
            ((Document) query).put("annotationSets.name", annotationSet.getName());
        } else {
            ((Document) query).put("annotationSets.name", new Document("$ne", annotationSet.getName()));
        }

        Bson update;
        if (overwrite) {
            update = Updates.set("annotationSets.$", object);
        } else {
            update = Updates.push("annotationSets", object);
        }

        QueryResult<UpdateResult> queryResult = cohortCollection.update(query, update, null);

        if (queryResult.first().getModifiedCount() != 1) {
            throw CatalogDBException.alreadyExists("AnnotationSet", "name", annotationSet.getName());
        }

        return endQuery("", startTime, Collections.singletonList(annotationSet));
    }

    @Override
    @Deprecated
    public QueryResult<AnnotationSet> deleteAnnotation(long cohortId, String annotationId) throws CatalogDBException {
        long startTime = startQuery();

        Cohort cohort = get(cohortId, new QueryOptions("include", "projects.studies.cohorts.annotationSets")).first();
        AnnotationSet annotationSet = null;
        for (AnnotationSet as : cohort.getAnnotationSets()) {
            if (as.getName().equals(annotationId)) {
                annotationSet = as;
                break;
            }
        }

        if (annotationSet == null) {
            throw CatalogDBException.idNotFound("AnnotationSet", annotationId);
        }

        Bson query = new Document(PRIVATE_ID, cohortId);
        Bson update = Updates.pull("annotationSets", new Document("name", annotationId));
        QueryResult<UpdateResult> resultQueryResult = cohortCollection.update(query, update, null);
        if (resultQueryResult.first().getModifiedCount() < 1) {
            throw CatalogDBException.idNotFound("AnnotationSet", annotationId);
        }

        return endQuery("Delete annotation", startTime, Collections.singletonList(annotationSet));
    }

    @Override
    public long getStudyId(long cohortId) throws CatalogDBException {
        checkId(cohortId);
        QueryResult queryResult = nativeGet(new Query(QueryParams.ID.key(), cohortId),
                new QueryOptions(MongoDBCollection.INCLUDE, PRIVATE_STUDY_ID));
        if (queryResult.getResult().isEmpty()) {
            throw CatalogDBException.idNotFound("Cohort", cohortId);
        } else {
            return ((Document) queryResult.first()).getLong(PRIVATE_STUDY_ID);
        }
    }

    @Override
    public QueryResult<Long> count(Query query) throws CatalogDBException {
        long startTime = startQuery();
        return endQuery("Count cohort", startTime, cohortCollection.count(parseQuery(query, false)));
    }

    @Override
    public QueryResult<Long> count(Query query, String user, StudyAclEntry.StudyPermissions studyPermission)
            throws CatalogDBException, CatalogAuthorizationException {
        if (!query.containsKey(QueryParams.STATUS_NAME.key())) {
            query.append(QueryParams.STATUS_NAME.key(), "!=" + Status.TRASHED + ";!=" + Status.DELETED);
        }
        if (studyPermission == null) {
            studyPermission = StudyAclEntry.StudyPermissions.VIEW_COHORTS;
        }

        // Get the study document
        Document studyDocument = getStudyDocument(query);

        // Get the document query needed to check the permissions as well
        Document queryForAuthorisedEntries = getQueryForAuthorisedEntries(studyDocument, user, studyPermission.name(),
                studyPermission.getCohortPermission().name());
        Bson bson = parseQuery(query, false, queryForAuthorisedEntries);
        logger.debug("Cohort count: query : {}, dbTime: {}", bson.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry()));
        return cohortCollection.count(bson);
    }

    @Override
    public QueryResult distinct(Query query, String field) throws CatalogDBException {
        Bson bson = parseQuery(query, false);
        return cohortCollection.distinct(field, bson);
    }

    @Override
    public QueryResult stats(Query query) {
        return null;
    }

    @Override
    public QueryResult<Cohort> update(long id, ObjectMap parameters) throws CatalogDBException {
        long startTime = startQuery();
        update(new Query(QueryParams.ID.key(), id), parameters);
        return endQuery("Update cohort", startTime, get(id, new QueryOptions()));
    }

    @Override
    public QueryResult<Long> update(Query query, ObjectMap parameters) throws CatalogDBException {
        long startTime = startQuery();
        Map<String, Object> cohortParams = new HashMap<>();

        String[] acceptedParams = {QueryParams.DESCRIPTION.key(), QueryParams.NAME.key(), QueryParams.CREATION_DATE.key()};
        filterStringParams(parameters, cohortParams, acceptedParams);

        Map<String, Class<? extends Enum>> acceptedEnums = Collections.singletonMap(QueryParams.TYPE.key(), Study.Type.class);
        filterEnumParams(parameters, cohortParams, acceptedEnums);

        // Check if the samples exist.
        if (parameters.containsKey(QueryParams.SAMPLES.key())) {
            List<Long> objectSampleList = parameters.getAsLongList(QueryParams.SAMPLES.key());
            List<Sample> sampleList = new ArrayList<>();
            for (Long sampleId : objectSampleList) {
                if (!dbAdaptorFactory.getCatalogSampleDBAdaptor().exists((sampleId))) {
                    throw CatalogDBException.idNotFound("Sample", (sampleId));
                }
                sampleList.add(new Sample().setId(sampleId));

            }
            cohortParams.put(QueryParams.SAMPLES.key(), cohortConverter.convertSamplesToDocument(sampleList));
        }
        String[] acceptedMapParams = {QueryParams.ATTRIBUTES.key(), QueryParams.STATS.key()};
        filterMapParams(parameters, cohortParams, acceptedMapParams);

        //Map<String, Class<? extends Enum>> acceptedEnumParams = Collections.singletonMap(QueryParams.STATUS_NAME.key(),
        //        Cohort.CohortStatus.class);
        //filterEnumParams(parameters, cohortParams, acceptedEnumParams);
        if (parameters.containsKey(QueryParams.STATUS_NAME.key())) {
            cohortParams.put(QueryParams.STATUS_NAME.key(), parameters.get(QueryParams.STATUS_NAME.key()));
            cohortParams.put(QueryParams.STATUS_DATE.key(), TimeUtils.getTime());
        }
        if (parameters.containsKey("status")) {
            throw new CatalogDBException("Unable to modify cohort. Use parameter \"" + QueryParams.STATUS_NAME.key()
                    + "\" instead of \"status\"");
        }

        if (!cohortParams.isEmpty()) {
            QueryResult<UpdateResult> update = cohortCollection.update(parseQuery(query, false), new Document("$set", cohortParams), null);
            return endQuery("Update cohort", startTime, Arrays.asList(update.getNumTotalResults()));
        }

        return endQuery("Update cohort", startTime, new QueryResult<>());
    }

    @Override
    public void delete(long id) throws CatalogDBException {
        Query query = new Query(QueryParams.ID.key(), id);
        delete(query);
    }

    @Override
    public void delete(Query query) throws CatalogDBException {
        QueryResult<DeleteResult> remove = cohortCollection.remove(parseQuery(query, false), null);

        if (remove.first().getDeletedCount() == 0) {
            throw CatalogDBException.deleteError("Cohort");
        }
    }

    @Override
    public QueryResult<Cohort> delete(long id, QueryOptions queryOptions) throws CatalogDBException {
        long startTime = startQuery();

        checkId(id);
        // Check if the cohort is active
        Query query = new Query(QueryParams.ID.key(), id)
                .append(QueryParams.STATUS_NAME.key(), "!=" + Status.TRASHED + ";!=" + Status.DELETED);
        if (count(query).first() == 0) {
            query.put(QueryParams.STATUS_NAME.key(), Status.TRASHED + "," + Status.DELETED);
            QueryOptions options = new QueryOptions(MongoDBCollection.INCLUDE, QueryParams.STATUS_NAME.key());
            Cohort cohort = get(query, options).first();
            throw new CatalogDBException("The cohort {" + id + "} was already " + cohort.getStatus().getName());
        }

        // Change the status of the cohort to deleted
        setStatus(id, Status.TRASHED);

        query = new Query(QueryParams.ID.key(), id).append(QueryParams.STATUS_NAME.key(), Status.TRASHED);

        return endQuery("Delete cohort", startTime, get(query, null));
    }

    @Override
    public QueryResult<Long> delete(Query query, QueryOptions queryOptions) throws CatalogDBException {
        long startTime = startQuery();
        query.append(QueryParams.STATUS_NAME.key(), "!=" + Status.TRASHED + ";!=" + Status.DELETED);
        QueryResult<Cohort> cohortQueryResult = get(query, new QueryOptions(MongoDBCollection.INCLUDE, QueryParams.ID.key()));
        for (Cohort cohort : cohortQueryResult.getResult()) {
            delete(cohort.getId(), queryOptions);
        }
        return endQuery("Delete cohort", startTime, Collections.singletonList(cohortQueryResult.getNumTotalResults()));
    }

    QueryResult<Cohort> setStatus(long cohortId, String status) throws CatalogDBException {
        return update(cohortId, new ObjectMap(QueryParams.STATUS_NAME.key(), status));
    }

    QueryResult<Long> setStatus(Query query, String status) throws CatalogDBException {
        return update(query, new ObjectMap(QueryParams.STATUS_NAME.key(), status));
    }

    @Override
    public QueryResult<Cohort> remove(long id, QueryOptions queryOptions) throws CatalogDBException {
        throw new UnsupportedOperationException("Operation not yet supported.");
    }

    @Override
    public QueryResult<Long> remove(Query query, QueryOptions queryOptions) throws CatalogDBException {
        throw new UnsupportedOperationException("Operation not yet supported.");
    }

    @Override
    public QueryResult<Cohort> restore(long id, QueryOptions queryOptions) throws CatalogDBException {
        long startTime = startQuery();

        checkId(id);
        // Check if the cohort is active
        Query query = new Query(QueryParams.ID.key(), id)
                .append(QueryParams.STATUS_NAME.key(), Status.TRASHED);
        if (count(query).first() == 0) {
            throw new CatalogDBException("The cohort {" + id + "} is not deleted");
        }

        // Change the status of the cohort to deleted
        setStatus(id, Cohort.CohortStatus.NONE);
        query = new Query(QueryParams.ID.key(), id);

        return endQuery("Restore cohort", startTime, get(query, null));
    }

    @Override
    public QueryResult<Long> restore(Query query, QueryOptions queryOptions) throws CatalogDBException {
        long startTime = startQuery();
        query.put(QueryParams.STATUS_NAME.key(), Status.TRASHED);
        return endQuery("Restore cohorts", startTime, setStatus(query, Cohort.CohortStatus.NONE));
    }

    @Override
    public QueryResult<Cohort> get(long cohortId, QueryOptions options) throws CatalogDBException {
        Query query = new Query()
                .append(QueryParams.ID.key(), cohortId)
                .append(QueryParams.STATUS_NAME.key(), "!=" + Status.DELETED);
        return get(query, options);
    }

    @Override
    public QueryResult<Cohort> get(Query query, QueryOptions options, String user)
            throws CatalogDBException, CatalogAuthorizationException {
        long startTime = startQuery();
        List<Cohort> documentList = new ArrayList<>();
        QueryResult<Cohort> queryResult;
        try (DBIterator<Cohort> dbIterator = iterator(query, options, user)) {
            while (dbIterator.hasNext()) {
                documentList.add(dbIterator.next());
            }
        }
        queryResult = endQuery("Get", startTime, documentList);

        // We only count the total number of results if the actual number of results equals the limit established for performance purposes.
        if (options != null && options.getInt(QueryOptions.LIMIT, 0) == queryResult.getNumResults()) {
            QueryResult<Long> count = count(query, user, StudyAclEntry.StudyPermissions.VIEW_COHORTS);
            queryResult.setNumTotalResults(count.first());
        }
        return queryResult;
    }

    @Override
    public QueryResult<Cohort> get(Query query, QueryOptions options) throws CatalogDBException {
        long startTime = startQuery();
        List<Cohort> documentList = new ArrayList<>();
        try (DBIterator<Cohort> dbIterator = iterator(query, options)) {
            while (dbIterator.hasNext()) {
                documentList.add(dbIterator.next());
            }
        }
        QueryResult<Cohort> queryResult = endQuery("Get", startTime, documentList);

        // We only count the total number of results if the actual number of results equals the limit established for performance purposes.
        if (options != null && options.getInt(QueryOptions.LIMIT, 0) == queryResult.getNumResults()) {
            QueryResult<Long> count = count(query);
            queryResult.setNumTotalResults(count.first());
        }
        return queryResult;
    }

    @Override
    public QueryResult nativeGet(Query query, QueryOptions options) throws CatalogDBException {
        long startTime = startQuery();
        List<Document> documentList = new ArrayList<>();
        QueryResult<Document> queryResult;
        try (DBIterator<Document> dbIterator = nativeIterator(query, options)) {
            while (dbIterator.hasNext()) {
                documentList.add(dbIterator.next());
            }
        }
        queryResult = endQuery("Native get", startTime, documentList);

        // We only count the total number of results if the actual number of results equals the limit established for performance purposes.
        if (options != null && options.getInt(QueryOptions.LIMIT, 0) == queryResult.getNumResults()) {
            QueryResult<Long> count = count(query);
            queryResult.setNumTotalResults(count.first());
        }
        return queryResult;
    }

    @Override
    public DBIterator<Cohort> iterator(Query query, QueryOptions options) throws CatalogDBException {
        MongoCursor<Document> mongoCursor = getMongoCursor(query, options);
        return new MongoDBIterator<>(mongoCursor, cohortConverter);
    }

    @Override
    public DBIterator<Document> nativeIterator(Query query, QueryOptions options) throws CatalogDBException {
        MongoCursor<Document> mongoCursor = getMongoCursor(query, options);
        return new MongoDBIterator<>(mongoCursor);
    }

    @Override
    public DBIterator<Cohort> iterator(Query query, QueryOptions options, String user)
            throws CatalogDBException, CatalogAuthorizationException {
        Document studyDocument = getStudyDocument(query);
        MongoCursor<Document> mongoCursor = getMongoCursor(query, options, studyDocument, user);
        Function<Document, Document> iteratorFilter = (d) ->  filterAnnotationSets(studyDocument, d, user,
                StudyAclEntry.StudyPermissions.VIEW_COHORT_ANNOTATIONS.name(), CohortAclEntry.CohortPermissions.VIEW_ANNOTATIONS.name());

        return new MongoDBIterator<>(mongoCursor, cohortConverter, iteratorFilter);
    }

    @Override
    public DBIterator<Document> nativeIterator(Query query, QueryOptions options, String user)
            throws CatalogDBException, CatalogAuthorizationException {
        Document studyDocument = getStudyDocument(query);
        MongoCursor<Document> mongoCursor = getMongoCursor(query, options, studyDocument, user);
        Function<Document, Document> iteratorFilter = (d) ->  filterAnnotationSets(studyDocument, d, user,
                StudyAclEntry.StudyPermissions.VIEW_COHORT_ANNOTATIONS.name(), CohortAclEntry.CohortPermissions.VIEW_ANNOTATIONS.name());

        return new MongoDBIterator<>(mongoCursor, iteratorFilter);
    }

    private MongoCursor<Document> getMongoCursor(Query query, QueryOptions options) throws CatalogDBException {
        MongoCursor<Document> documentMongoCursor;
        try {
            documentMongoCursor = getMongoCursor(query, options, null, null);
        } catch (CatalogAuthorizationException e) {
            throw new CatalogDBException(e);
        }
        return documentMongoCursor;
    }

    private MongoCursor<Document> getMongoCursor(Query query, QueryOptions options, Document studyDocument, String user)
            throws CatalogDBException, CatalogAuthorizationException {
        Document queryForAuthorisedEntries = null;
        if (studyDocument != null && user != null) {
            // Get the document query needed to check the permissions as well
            queryForAuthorisedEntries = getQueryForAuthorisedEntries(studyDocument, user,
                    StudyAclEntry.StudyPermissions.VIEW_COHORTS.name(), CohortAclEntry.CohortPermissions.VIEW.name());
        }

        if (!query.containsKey(QueryParams.STATUS_NAME.key())) {
            query.append(QueryParams.STATUS_NAME.key(), "!=" + Status.TRASHED + ";!=" + Status.DELETED);
        }
        Bson bson = parseQuery(query, false, queryForAuthorisedEntries);
        QueryOptions qOptions;
        if (options != null) {
            qOptions = options;
        } else {
            qOptions = new QueryOptions();
        }
        qOptions = filterOptions(qOptions, FILTER_ROUTE_COHORTS);

        return cohortCollection.nativeQuery().find(bson, qOptions).iterator();
    }

    private Document getStudyDocument(Query query) throws CatalogDBException {
        // Get the study document
        Query studyQuery = new Query(StudyDBAdaptor.QueryParams.ID.key(), query.getLong(QueryParams.STUDY_ID.key()));
        QueryResult<Document> queryResult = dbAdaptorFactory.getCatalogStudyDBAdaptor().nativeGet(studyQuery, QueryOptions.empty());
        if (queryResult.getNumResults() == 0) {
            throw new CatalogDBException("Study " + query.getLong(QueryParams.STUDY_ID.key()) + " not found");
        }
        return queryResult.first();
    }

    @Override
    public QueryResult rank(Query query, String field, int numResults, boolean asc) throws CatalogDBException {
        Bson bsonQuery = parseQuery(query, false);
        return rank(cohortCollection, bsonQuery, field, "name", numResults, asc);
    }

    @Override
    public QueryResult groupBy(Query query, String field, QueryOptions options) throws CatalogDBException {
        Bson bsonQuery = parseQuery(query, false);
        return groupBy(cohortCollection, bsonQuery, field, "name", options);
    }

    @Override
    public QueryResult groupBy(Query query, List<String> fields, QueryOptions options) throws CatalogDBException {
        Bson bsonQuery = parseQuery(query, false);
        return groupBy(cohortCollection, bsonQuery, fields, "name", options);
    }

    @Override
    public void forEach(Query query, Consumer<? super Object> action, QueryOptions options) throws CatalogDBException {
        Objects.requireNonNull(action);
        try (DBIterator<Cohort> catalogDBIterator = iterator(query, options)) {
            while (catalogDBIterator.hasNext()) {
                action.accept(catalogDBIterator.next());
            }
        }
    }

    private void checkCohortNameExists(long studyId, String cohortName) throws CatalogDBException {
        QueryResult<Long> count = cohortCollection.count(Filters.and(
                Filters.eq(PRIVATE_STUDY_ID, studyId), Filters.eq(QueryParams.NAME.key(), cohortName)));
        if (count.getResult().get(0) > 0) {
            throw CatalogDBException.alreadyExists("Cohort", "name", cohortName);
        }
    }

    private Bson parseQuery(Query query, boolean isolated) throws CatalogDBException {
        return parseQuery(query, isolated, null);
    }

    private Bson parseQuery(Query query, boolean isolated, Document authorisation) throws CatalogDBException {
        List<Bson> andBsonList = new ArrayList<>();
        List<Bson> annotationList = new ArrayList<>();
        // We declare variableMap here just in case we have different annotation queries
        Map<String, Variable> variableMap = null;

        if (isolated) {
            andBsonList.add(new Document("$isolated", 1));
        }

        if (query.containsKey(QueryParams.ANNOTATION.key())) {
            fixAnnotationQuery(query);
        }

        for (Map.Entry<String, Object> entry : query.entrySet()) {
            String key = entry.getKey().split("\\.")[0];
            QueryParams queryParam = QueryParams.getParam(entry.getKey()) != null ? QueryParams.getParam(entry.getKey())
                    : QueryParams.getParam(key);
            if (queryParam == null) {
                continue;
            }
            try {
                switch (queryParam) {
                    case ID:
                        addOrQuery(PRIVATE_ID, queryParam.key(), query, queryParam.type(), andBsonList);
                        break;
                    case STUDY_ID:
                        addOrQuery(PRIVATE_STUDY_ID, queryParam.key(), query, queryParam.type(), andBsonList);
                        break;
                    case ATTRIBUTES:
                        addAutoOrQuery(entry.getKey(), entry.getKey(), query, queryParam.type(), andBsonList);
                        break;
                    case BATTRIBUTES:
                        String mongoKey = entry.getKey().replace(QueryParams.BATTRIBUTES.key(), QueryParams.ATTRIBUTES.key());
                        addAutoOrQuery(mongoKey, entry.getKey(), query, queryParam.type(), andBsonList);
                        break;
                    case NATTRIBUTES:
                        mongoKey = entry.getKey().replace(QueryParams.NATTRIBUTES.key(), QueryParams.ATTRIBUTES.key());
                        addAutoOrQuery(mongoKey, entry.getKey(), query, queryParam.type(), andBsonList);
                        break;
                    case VARIABLE_SET_ID:
                        addOrQuery(queryParam.key(), queryParam.key(), query, queryParam.type(), annotationList);
                        break;
                    case ANNOTATION:
                        if (variableMap == null) {
                            long variableSetId = query.getLong(QueryParams.VARIABLE_SET_ID.key());
                            if (variableSetId > 0) {
                                variableMap = dbAdaptorFactory.getCatalogStudyDBAdaptor().getVariableSet(variableSetId, null).first()
                                        .getVariables().stream().collect(Collectors.toMap(Variable::getName, Function.identity()));
                            }
                        }
                        addAnnotationQueryFilter(entry.getKey(), query, variableMap, annotationList);
                        break;
                    case ANNOTATION_SET_NAME:
                        addOrQuery("name", queryParam.key(), query, queryParam.type(), annotationList);
                        break;
                    case SAMPLE_IDS:
                        addQueryFilter(queryParam.key(), queryParam.key(), query, queryParam.type(),
                                MongoDBQueryUtils.ComparisonOperator.IN, MongoDBQueryUtils.LogicalOperator.OR, andBsonList);
                        break;
                    case NAME:
                    case TYPE:
                    case CREATION_DATE:
                    case RELEASE:
                    case STATUS_NAME:
                    case STATUS_MSG:
                    case STATUS_DATE:
                    case DESCRIPTION:
                    case ANNOTATION_SETS:
                    case VARIABLE_NAME:
                        addAutoOrQuery(queryParam.key(), queryParam.key(), query, queryParam.type(), andBsonList);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                throw new CatalogDBException(e);
            }
        }

        if (annotationList.size() > 0) {
            Bson projection = Projections.elemMatch(QueryParams.ANNOTATION_SETS.key(), Filters.and(annotationList));
            andBsonList.add(projection);
        }
        if (authorisation != null && authorisation.size() > 0) {
            andBsonList.add(authorisation);
        }
        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
    }

    public MongoDBCollection getCohortCollection() {
        return cohortCollection;
    }

    public QueryResult<Long> extractSamplesFromCohorts(Query query, List<Long> sampleIds) throws CatalogDBException {
        long startTime = startQuery();
        QueryResult<Cohort> cohortQueryResult = get(query, new QueryOptions(QueryOptions.INCLUDE, QueryParams.ID.key()));
        if (cohortQueryResult.getNumResults() > 0) {
            Bson bsonQuery = parseQuery(query, false);
            Bson update = new Document("$pull", new Document(QueryParams.SAMPLES.key(),
                    new Document("id", new Document("$in", sampleIds))));
            QueryOptions multi = new QueryOptions(MongoDBCollection.MULTI, true);
            QueryResult<UpdateResult> updateQueryResult = cohortCollection.update(bsonQuery, update, multi);

            // Now we set all the cohorts where a sample has been taken out to status INVALID
            List<Long> ids = cohortQueryResult.getResult().stream().map(Cohort::getId).collect(Collectors.toList());
            setStatus(new Query(QueryParams.ID.key(), ids), Cohort.CohortStatus.INVALID);

            return endQuery("Extract samples from cohorts", startTime,
                    Collections.singletonList(updateQueryResult.first().getModifiedCount()));
        }
        return endQuery("Extract samples from cohorts", startTime, Collections.singletonList(0L));
    }

}
