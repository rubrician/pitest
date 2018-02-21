package org.pitest.mutationtest.export.db.mongo;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.jboss.windup.decompiler.api.DecompilationResult;
import org.jboss.windup.decompiler.fernflower.FernflowerDecompiler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.util.Log;

import java.io.CharArrayWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Talal Ahmed on 09/12/2017
 */
public class MongoExportInterceptor implements MutationInterceptor {

    private static final Logger LOG = Log.getLogger();

    public static final String MONGO_DEFAULT_HOST = "localhost";
    public static final Integer MONGO_DEFAULT_PORT = 27017;

    public static final String DEFAULT_DB_NAME = "pitest";
    public static final String DEFAULT_COLLECTION_NAME = "result-dc";

    private final String reportDir;
    private final ClassByteArraySource source;
    private ClassName srcClass;

    private final MongoClient mongo;
    private final MongoDatabase db;
    private final MongoCollection<Document> coll;


    public MongoExportInterceptor(ClassByteArraySource source, ReportOptions data) {
        LOG.info("Connecting to db...");

        this.reportDir = data.getReportDir().substring(data.getReportDir().lastIndexOf("/") + 1);
        this.mongo = new MongoClient(MONGO_DEFAULT_HOST, MONGO_DEFAULT_PORT);
        this.db = mongo.getDatabase(DEFAULT_DB_NAME);
        this.coll = db.getCollection(DEFAULT_COLLECTION_NAME);
        this.source = source;
    }

    @Override
    public InterceptorType type() {
        return InterceptorType.REPORT;
    }

    @Override
    public void begin(ClassTree clazz) {
        srcClass = clazz.name();
    }

    @Override
    public Collection<MutationDetails> intercept(Collection<MutationDetails> mutations, Mutater m) {
        List<MutationDetails> indexable = new ArrayList<MutationDetails>(mutations);

        try {
            List<String> decompiledSrc = decompileSrc(source.getBytes(srcClass.asJavaName()).value());

            for (int i = 0; i != indexable.size(); i++) {
                Document document = new Document();

                MutationDetails md = indexable.get(i);
                Mutant mutant = m.getMutation(md.getId());

                addFileDetails(document, reportDir, decompiledSrc, mutant.getDetails());

                document.append("mutation", createMutationDetails(mutant.getDetails(), mutant.getBytes()));
                document.append("createdAt", System.currentTimeMillis());

                insertDocument(document);
            }

        } catch (IOException ex) {
            throw new RuntimeException("Error exporting mutants for report", ex);
        }

        return mutations;
    }

    @Override
    public void end() {
    }

    private void addFileDetails(Document document, String project, List<String> decompiledSrc, MutationDetails mutationDetails) {
        document.append("project", project);
        document.append("file", mutationDetails.getFilename());
        document.append("package", mutationDetails.getClassName().getPackage().asJavaName());
        document.append("className", mutationDetails.getClassName().asJavaName());
        document.append("decompiledSrc", decompiledSrc);
    }

    private Document createMutationDetails(MutationDetails mutationDetails, byte[] bytes) throws IOException {
        Document details = new Document();
        details.append("method", mutationDetails.getMethod().name());
        details.append("classLine", mutationDetails.getClassLine().getClassName() + ":" + mutationDetails.getClassLine().getLineNumber());
        details.append("description", mutationDetails.getDescription());
        details.append("mutator", mutationDetails.getMutator());
        details.append("index", mutationDetails.getFirstIndex());
        details.append("line", mutationDetails.getLineNumber());
        details.append("decompiled", decompileMutation(bytes));
        return details;
    }

    private List<String> decompileSrc(byte[] srcClass) throws IOException {
        Path classPath = Paths.get("Source.class");
        Path outPath = Paths.get("decompiled_source_classes");
        Files.write(classPath, srcClass, StandardOpenOption.CREATE);

        FernflowerDecompiler decompiler = new FernflowerDecompiler();
        DecompilationResult decompilationResult = decompiler.decompileClassFile(classPath.getRoot(), classPath, outPath);
        Map<String, String> decompiledFiles = decompilationResult.getDecompiledFiles();

        List<String> decompiledContent = new ArrayList<>();

        for (String decompiledFile : decompiledFiles.values()) {
            decompiledContent.add(readFile(decompiledFile));
        }
        return decompiledContent;
    }

    private List<String> decompileMutation(byte[] bytes) throws IOException {
        Path classPath = Paths.get("Mutated.class");
        Path outPath = Paths.get("decompiled_mutated_classes");
        Files.write(classPath, bytes, StandardOpenOption.CREATE);

        FernflowerDecompiler decompiler = new FernflowerDecompiler();
        DecompilationResult decompilationResult = decompiler.decompileClassFile(classPath.getRoot(), classPath, outPath);
        Map<String, String> decompiledFiles = decompilationResult.getDecompiledFiles();

        List<String> decompiledContent = new ArrayList<>();

        for (String decompiledFile : decompiledFiles.values()) {
            decompiledContent.add(readFile(decompiledFile));
        }

        return decompiledContent;
    }

    private void insertDocument(Document document) {
        coll.insertOne(document);
    }

    private String readFile(String path) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(path)) {
            return IOUtils.toString(inputStream);
        }
    }

    private void addByteCode(Document doc, final byte[] clazz) {
        final ClassReader reader = new ClassReader(clazz);
        CharArrayWriter buffer = new CharArrayWriter();
        reader.accept(new TraceClassVisitor(null, new Textifier(), new PrintWriter(buffer)), ClassReader.EXPAND_FRAMES);
        Set<String> byteCode = Collections.singleton(buffer.toString());

        doc.append("bytecode", byteCode);
    }

}
