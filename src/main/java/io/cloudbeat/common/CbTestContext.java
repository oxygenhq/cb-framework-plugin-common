package io.cloudbeat.common;

import io.cloudbeat.common.config.CbConfig;
import io.cloudbeat.common.reporter.CbTestReporter;
import io.cloudbeat.common.config.PropertiesLoader;

import java.util.Properties;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class CbTestContext {
    //private static final Logger LOGGER = LoggerFactory.getLogger(CbTestContext.class);
    private static final ThreadLocal<CbTestContext> CURRENT_CONTEXT = new InheritableThreadLocal<>();

            //InheritableThreadLocal.withInitial(() -> new CbTestContext());
    /*new ThreadLocal<CbTestContext>() {
        @Override
        protected CbTestContext initialValue() {
            return new CbTestContext();
        }
    };*/
            //new InheritableThreadLocal<>();

    private final CbTestReporter reporter;
    private final CbConfig config;

    public CbTestContext() {
        config = new CbConfig(PropertiesLoader.load());
        reporter = new CbTestReporter(config);
    }
    /**
     * Returns a current test context linked to the current thread.
     *
     * @return test context instance
     */
    public static CbTestContext getInstance() {
        if (CURRENT_CONTEXT.get() == null)
            CURRENT_CONTEXT.set(new CbTestContext());
        return CURRENT_CONTEXT.get();
    }

    public static CbTestReporter getReporter() {
        return getInstance().reporter;
    }

    public CbConfig getConfig() { return config; }
}
