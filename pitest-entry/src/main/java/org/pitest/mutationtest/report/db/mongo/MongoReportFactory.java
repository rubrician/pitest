package org.pitest.mutationtest.report.db.mongo;

import org.pitest.mutationtest.ListenerArguments;
import org.pitest.mutationtest.MutationResultListener;
import org.pitest.mutationtest.MutationResultListenerFactory;
import org.pitest.util.Log;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * Created by Talal Ahmed on 23/11/2017
 */
public class MongoReportFactory implements MutationResultListenerFactory {

    private static final Logger LOG = Log.getLogger();

    @Override
    public MutationResultListener getListener(Properties props, final ListenerArguments args) {
        LOG.info("MongoReportFactory props: " + props);
        LOG.info("MongoReportFactory args: " + args);

        return new MongoReportListener(props, args);
    }

    @Override
    public String name() {
        return "mongodb";
    }

    @Override
    public String description() {
        return "Default mongodb report plugin";
    }
}
