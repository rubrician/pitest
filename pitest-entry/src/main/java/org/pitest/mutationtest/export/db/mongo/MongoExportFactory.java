package org.pitest.mutationtest.export.db.mongo;

import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.plugin.Feature;

public class MongoExportFactory implements MutationInterceptorFactory {

  @Override
  public String description() {
    return "Mutant export to mongo";
  }

  @Override
  public MongoExportInterceptor createInterceptor(InterceptorParameters params) {
    return new MongoExportInterceptor(params.source(), params.data());
  }

  @Override
  public Feature provides() {
    return Feature.named("EXPORT")
        .withDescription("Exports mutants bytecode and other details to mongo")
        .withOnByDefault(true);
  }

}
