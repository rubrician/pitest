package org.pitest.mutationtest.report.db.mongo;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
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

/**
 * Created by Talal Ahmed on 23/11/2017
 */
public class MongoReportListener implements MutationResultListener {

    private static final Logger LOG = Log.getLogger();

    public static final String MONGO_DEFAULT_HOST = "localhost";
    public static final Integer MONGO_DEFAULT_PORT = 27017;

    public static final String DEFAULT_DB_NAME = "pitest";
    public static final String DEFAULT_COLLECTION_NAME = "result";

    private final MongoClient mongo;
    private final MongoDatabase db;
    private final MongoCollection<Document> coll;

    private final SourceLocator locator;
    private final MutationEngine engine;
    private final CoverageDatabase coverage;

    public MongoReportListener(Properties props, ListenerArguments args) {
        this(args);
    }

    public MongoReportListener(ListenerArguments args) {
        LOG.info("Connecting to db...");

        mongo = new MongoClient(MONGO_DEFAULT_HOST, MONGO_DEFAULT_PORT);
        db = mongo.getDatabase(DEFAULT_DB_NAME);
        coll = db.getCollection(DEFAULT_COLLECTION_NAME);

        coverage = args.getCoverage();
        engine = args.getEngine();
        locator = args.getLocator();
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
        document.append("file", metaData.getFileName());
        document.append("package", metaData.getPackageName());
        document.append("mutatedClass", metaData.getMutatedClass().asJavaName());
        document.append("content", getClassContent(metaData.getMutatedClass().asJavaName(), metaData.getFileName()));

        List<Document> mutations = new ArrayList<Document>();

        for (final MutationResult mutation : metaData.getMutations()) {
            Document mdoc = new Document();
            mdoc.append("class", mutation.getDetails().getClassName().asJavaName());
            mdoc.append("classLine", mutation.getDetails().getClassLine().getClassName() + ":" + mutation.getDetails().getClassLine().getLineNumber());
            mdoc.append("method", mutation.getDetails().getMethod().name());
            mdoc.append("line", mutation.getDetails().getLineNumber());
            mdoc.append("description", mutation.getDetails().getDescription());
            mdoc.append("status", mutation.getStatus().isDetected() ? "detected" : "undetected");
            mdoc.append("statusDetail", mutation.getStatusDescription());
            mdoc.append("killingTest", mutation.getKillingTestDescription());
            mdoc.append("testRuns", mutation.getNumberOfTestsRun());
            mutations.add(mdoc);
        }

        document.append("createdAt", System.currentTimeMillis());
        document.append("mutators", engine.getMutatorNames());
        document.append("mutations", mutations);

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
