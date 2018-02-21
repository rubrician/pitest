package org.pitest.mutationtest.report.db.mongo;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.functional.Option;
import org.pitest.mutationtest.ClassMutationResults;
import org.pitest.mutationtest.ListenerArguments;
import org.pitest.mutationtest.MutationResultListener;
import org.pitest.mutationtest.SourceLocator;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.MutationResult;
import org.pitest.util.Log;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.Arrays;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

/**
 * Created by Talal Ahmed on 23/11/2017
 */
public class MongoReportListener implements MutationResultListener {

    private static final Logger LOG = Log.getLogger();

    public static final String MONGO_DEFAULT_HOST = "localhost";
    public static final Integer MONGO_DEFAULT_PORT = 27017;

    public static final String DEFAULT_DB_NAME = "pitest";
    public static final String DEFAULT_COLLECTION_NAME = "result-src";
    public static final String DEFAULT_DC_COLLECTION_NAME = "result-dc";

    private final MongoClient mongo;
    private final MongoDatabase db;
    private final MongoCollection<Document> coll;
    private final MongoCollection<Document> collDC;

    private final SourceLocator locator;
    private final MutationEngine engine;
    private final CoverageDatabase coverage;

    public MongoReportListener(Properties props, ListenerArguments args) {
        this(args);
    }

    public MongoReportListener(ListenerArguments args) {
        LOG.info("Connecting to db...");

        this.mongo = new MongoClient(MONGO_DEFAULT_HOST, MONGO_DEFAULT_PORT);
        this.db = mongo.getDatabase(DEFAULT_DB_NAME);
        this.coll = db.getCollection(DEFAULT_COLLECTION_NAME);
        this.collDC = db.getCollection(DEFAULT_DC_COLLECTION_NAME);
        this.coverage = args.getCoverage();
        this.engine = args.getEngine();
        this.locator = args.getLocator();
    }

    @Override
    public void runStart() {

    }

    @Override
    public void runEnd() {
        LOG.info("Closing db connection...");
        mongo.close();
    }

    @Override
    public void handleMutationResult(final ClassMutationResults metaData) {
        LOG.info("Handling Mutation Result...");

        Document document = new Document();

        List<Document> mutations = new ArrayList<>();
        List<String> decompiledSrc = null;
        String project = "unknown";

        for (final MutationResult mutationResult : metaData.getMutations()) {
            if ("KILLED".equals(mutationResult.getStatusDescription())) {
                int lineNumber = mutationResult.getDetails().getLineNumber();
                String method = mutationResult.getDetails().getMethod().name();
                String description = mutationResult.getDetails().getDescription();
                String className = metaData.getMutatedClass().asJavaName();

                Document doc = findDcDocument(className, lineNumber, method, description);
                Document mutation = doc.get("mutation", Document.class);

                decompiledSrc = doc.get("decompiledSrc", List.class);
                project = doc.getString("project");

                updateKilledMutations(mutation, mutationResult);
                mutations.add(mutation);
            }
        }

        addFileDetails(document, project, metaData);
        document.append("decompiledSrc", decompiledSrc);
        document.append("mutations", mutations);
        document.append("updatedAt", System.currentTimeMillis());
        document.append("mutators", engine.getMutatorNames());

        insertDocument(document);
    }

    private void addFileDetails(Document document, String project, ClassMutationResults metaData) {
        document.append("project", project);
        document.append("file", metaData.getFileName());
        document.append("package", metaData.getPackageName());
        document.append("className", metaData.getMutatedClass().asJavaName());
        document.append("source", getClassContent(metaData.getMutatedClass().asJavaName(), metaData.getFileName()));
    }

    private void updateKilledMutations(Document doc, MutationResult result) {
        doc.append("status", result.getStatusDescription());
        doc.append("killingTest", result.getKillingTestDescription());
        doc.append("stacktrace", result.getKillingTestStacktrace().hasSome() ? result.getKillingTestStacktrace().value() : "none");
        doc.append("testRuns", result.getNumberOfTestsRun());
    }

    private Document findDcDocument(String className, int lineNumber, String method, String description) {
        Bson query = and(
                eq("className", className),
                eq("mutation.line", lineNumber),
                eq("mutation.method", method),
                eq("mutation.description", description)
        );
        return collDC.find(query).first();
    }

    private Document findDocument(String className) {
        Bson query = eq("className", className);
        return coll.find(query).first();
    }

    private void updateDocument(Document document) {
        coll.replaceOne(eq("_id", document.get("_id")), document);
    }

    private void insertDocument(Document document) {
        coll.insertOne(document);
    }

    private String getClassContent(String className, String fileName) {
        Option<Reader> reader = locator.locate(Arrays.asList(className), fileName);
        StringBuilder buffer = new StringBuilder();
        if (reader.hasSome()) {
            try {
                char[] arr = new char[8 * 1024];
                int numCharsRead;
                while ((numCharsRead = reader.value().read(arr, 0, arr.length)) != -1) {
                    buffer.append(arr, 0, numCharsRead);
                }
                reader.value().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return buffer.toString();
    }
}
